package com.example.tiendita.auth

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.entity.AuthMetadataEntity
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.session.DataStoreSessionStore
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountProvisioningAndroidTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: AppDatabase
    private lateinit var name: String
    private val hasher = Pbkdf2PasswordHasher()
    private val password get() = " significant 密码 ".toCharArray()
    private fun repository(using: PasswordHasher = hasher) = RoomAccountProvisioningRepository(
        database, database.accountDao(), database.authMetadataDao(), using)
    private fun initial(username: String = "founder", secret: CharArray = password) =
        InitialAdminRequest(username, secret, " Initial Administrator ", " person@example.org ", " +521234567890 ")
    private fun registration(username: String = "new.account", role: AccountRole = AccountRole.CONSULTA,
        employeeId: Long? = null) = AccountRegistrationRequest(username, password, " New Account ", role, employeeId = employeeId)
    private fun id(result: AccountProvisioningResult): Long {
        assertTrue(result.toString(), result is AccountProvisioningResult.Success)
        return (result as AccountProvisioningResult.Success).accountId
    }
    private suspend fun administrator() = id(repository().createInitialAdmin(initial()))
    private suspend fun assertNoAccountsOrMetadata() {
        assertEquals(0, database.accountDao().countAccounts())
        assertNull(database.authMetadataDao().getState())
    }

    @Before fun open() {
        name = "provisioning-${UUID.randomUUID()}"
        database = AppDatabase.buildDatabase(context, name)
    }
    @After fun close() { database.close(); context.deleteDatabase(name) }

    @Test fun initialAdministratorHasVerifiableHashAndNoLegacyWrites() = runBlocking {
        val caller = password
        val snapshot = caller.copyOf()
        try {
            val accountId = id(repository().createInitialAdmin(initial(" Custom.Founder ", caller)))
            val row = database.accountDao().getAccountById(accountId)!!
            assertEquals("Custom.Founder", row.username)
            assertEquals("custom.founder", row.normalizedUsername)
            assertEquals("Initial Administrator", row.displayName)
            assertEquals("person@example.org", row.email)
            assertEquals("+521234567890", row.phone)
            assertEquals(AccountRole.ADMIN, row.role)
            assertTrue(row.active)
            assertNull(row.employeeId)
            assertTrue(hasher.verify(caller, PasswordHash(row.passwordAlgorithm, row.passwordIterations, row.salt, row.passwordHash)))
            assertFalse(hasher.verify("significant 密码".toCharArray(), PasswordHash(row.passwordAlgorithm, row.passwordIterations, row.salt, row.passwordHash)))
            assertNotEquals(String(caller), row.passwordHash)
            assertArrayEquals(snapshot, caller)
            assertTrue(database.authMetadataDao().getState()!!.initialAdminCreated)
            assertEquals(1, row.passwordParametersVersion)
            assertEquals("PBKDF2WithHmacSHA256", row.passwordAlgorithm)
            assertEquals(600_000, row.passwordIterations)
            assertTrue(database.userDao().getAllUsers().isEmpty())
            for (table in listOf("admins", "employees", "clients", "suppliers")) {
                database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use {
                    assertTrue(it.moveToFirst()); assertEquals(0, it.getInt(0))
                }
            }
        } finally { caller.fill('\u0000'); snapshot.fill('\u0000') }
    }

    @Test fun secondBootstrapIsUnavailable() = runBlocking {
        administrator()
        assertEquals(AccountProvisioningResult.BootstrapUnavailable, repository().createInitialAdmin(initial("another")))
        assertEquals(1, database.accountDao().countAccounts())
    }

    @Test fun concurrentBootstrapsHaveExactlyOneWinner() = runBlocking {
        val ready = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val hashed = AtomicInteger()
        val coordinated = object : PasswordHasher by hasher {
            override suspend fun hash(password: CharArray): PasswordHash {
                val result = hasher.hash(password)
                if (hashed.incrementAndGet() == 2) ready.complete(Unit)
                release.await()
                return result
            }
        }
        val repo = repository(coordinated)
        val results = coroutineScope {
            val a = async(Dispatchers.Default) { repo.createInitialAdmin(initial("first")) }
            val b = async(Dispatchers.Default) { repo.createInitialAdmin(initial("second")) }
            withTimeout(30_000) { ready.await() }
            release.complete(Unit)
            listOf(a.await(), b.await())
        }
        assertEquals(1, results.count { it is AccountProvisioningResult.Success })
        assertEquals(1, results.count { it == AccountProvisioningResult.BootstrapUnavailable })
        assertEquals(1, database.accountDao().countAccounts())
        assertEquals(1, database.accountDao().countActiveAdmins())
        assertTrue(database.authMetadataDao().getState()!!.initialAdminCreated)
    }

    @Test fun deletingIsolatedDataStoreAndAllAccountsCannotReopenConsumedBootstrap() = runBlocking {
        administrator()
        database.openHelper.writableDatabase.execSQL("DELETE FROM user_accounts")
        val file = File(context.cacheDir, "provisioning-session-${UUID.randomUUID()}.preferences_pb")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = DataStoreSessionStore(PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }))
            store.saveAccountId(17); store.clear()
        } finally { scope.coroutineContext.job.cancelAndJoin(); file.delete() }
        assertEquals(AccountProvisioningResult.BootstrapUnavailable, repository().createInitialAdmin(initial("retry")))
        assertTrue(database.authMetadataDao().getState()!!.initialAdminCreated)
        assertEquals(0, database.accountDao().countAccounts())
    }

    @Test fun insertionFailureRollsBackNewMetadataAndAccount() = runBlocking {
        database.openHelper.writableDatabase.execSQL("CREATE TEMP TRIGGER reject_account BEFORE INSERT ON user_accounts " +
            "BEGIN SELECT RAISE(ABORT, 'isolated test failure'); END")
        assertEquals(AccountProvisioningResult.PersistenceError, repository().createInitialAdmin(initial()))
        assertNoAccountsOrMetadata()
    }

    @Test fun failedMetadataConsumptionRollsBackInsertedAccount() = runBlocking {
        database.openHelper.writableDatabase.execSQL("CREATE TEMP TRIGGER ignore_consumption BEFORE UPDATE ON auth_metadata " +
            "BEGIN SELECT RAISE(IGNORE); END")
        assertEquals(AccountProvisioningResult.PersistenceError, repository().createInitialAdmin(initial()))
        assertNoAccountsOrMetadata()
    }

    @Test fun activeAdministratorCanCreateEveryRoleWithVersionOneHashes() = runBlocking {
        val actor = administrator()
        for (role in AccountRole.entries) {
            val created = id(repository().createAccount(actor, registration("created.${role.name}", role)))
            val row = database.accountDao().getAccountById(created)!!
            assertEquals(role, row.role)
            assertEquals(1, row.passwordParametersVersion)
            assertEquals(600_000, row.passwordIterations)
            assertTrue(hasher.verify(password, PasswordHash(row.passwordAlgorithm, row.passwordIterations, row.salt, row.passwordHash)))
        }
        assertEquals(4, database.accountDao().countAccounts())
        assertTrue(database.userDao().getAllUsers().isEmpty())
    }

    @Test fun consultaActorCannotCreateAccountsEvenWhenNamedAdmin() = runBlocking {
        val founder = administrator()
        val actor = id(repository().createAccount(founder, registration("admin", AccountRole.CONSULTA)))
        assertUnauthorizedWithoutHash(actor)
    }

    @Test fun almacenActorCannotCreateAccounts() = runBlocking {
        val founder = administrator()
        val actor = id(repository().createAccount(founder, registration("warehouse", AccountRole.ALMACEN)))
        assertUnauthorizedWithoutHash(actor)
    }

    @Test fun inactiveAdministratorCannotCreateAccounts() = runBlocking {
        val actor = administrator()
        database.accountDao().updateAccount(database.accountDao().getAccountById(actor)!!.copy(active = false))
        assertUnauthorizedWithoutHash(actor)
    }

    @Test fun missingActorCannotCreateAccounts() = runBlocking { assertUnauthorizedWithoutHash(999) }

    private suspend fun assertUnauthorizedWithoutHash(actor: Long) {
        val count = database.accountDao().countAccounts()
        val forbidden = object : PasswordHasher by hasher {
            override suspend fun hash(password: CharArray): PasswordHash = throw AssertionError("Unauthorized hashing")
        }
        assertEquals(AccountProvisioningResult.Unauthorized, repository(forbidden).createAccount(actor, registration()))
        assertEquals(count, database.accountDao().countAccounts())
    }

    @Test fun duplicateAndEquivalentUsernamesAreControlledWithoutReplacement() = runBlocking {
        val actor = administrator()
        val original = id(repository().createAccount(actor, registration("Mixed.User")))
        for (name in listOf("Mixed.User", "MIXED.USER", " mixed.user "))
            assertEquals(AccountProvisioningResult.UsernameTaken, repository().createAccount(actor, registration(name)))
        assertEquals(original, database.accountDao().getAccountByNormalizedUsername("mixed.user")!!.id)
        assertEquals(2, database.accountDao().countAccounts())
    }

    @Test fun actorDeactivatedAfterHashCannotWrite() = runBlocking { revocationDuringHash(active = false, role = AccountRole.ADMIN) }
    @Test fun actorDemotedAfterHashCannotWrite() = runBlocking { revocationDuringHash(active = true, role = AccountRole.CONSULTA) }

    private suspend fun revocationDuringHash(active: Boolean, role: AccountRole) = coroutineScope {
        val actor = administrator()
        val ready = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val paused = object : PasswordHasher by hasher {
            override suspend fun hash(password: CharArray): PasswordHash {
                val result = hasher.hash(password)
                ready.complete(Unit); release.await(); return result
            }
        }
        val operation = async(Dispatchers.Default) { repository(paused).createAccount(actor, registration()) }
        withTimeout(30_000) { ready.await() }
        database.accountDao().updateAccount(database.accountDao().getAccountById(actor)!!.copy(active = active, role = role))
        release.complete(Unit)
        assertEquals(AccountProvisioningResult.Unauthorized, operation.await())
        assertEquals(1, database.accountDao().countAccounts())
    }

    @Test fun nonexistentEmployeeIsControlledWithoutPartialWrites() = runBlocking {
        val actor = administrator()
        assertEquals(AccountProvisioningResult.InvalidEmployeeReference,
            repository().createAccount(actor, registration(employeeId = 999)))
        assertEquals(1, database.accountDao().countAccounts())
    }

    @Test fun repeatedEmployeeLinkIsControlledWithoutPartialWrites() = runBlocking {
        val actor = administrator()
        val employee = database.employeeDao().insertEmployee(EmployeeEntity(firstName = "Test", lastName = "Employee",
            position = "Warehouse", email = null, phone = null, address = null, hireDate = null))
        val linked = id(repository().createAccount(actor, registration("linked", employeeId = employee)))
        assertEquals(employee, database.accountDao().getAccountById(linked)!!.employeeId)
        assertEquals(AccountProvisioningResult.EmployeeAlreadyLinked,
            repository().createAccount(actor, registration("another", employeeId = employee)))
        assertEquals(2, database.accountDao().countAccounts())
    }

    @Test fun cryptoFailureIsClosedAndInternalCopyIsWiped() = runBlocking {
        var borrowed: CharArray? = null
        val broken = object : PasswordHasher by hasher {
            override suspend fun hash(password: CharArray): PasswordHash {
                borrowed = password
                throw IllegalStateException("sensitive exception marker")
            }
        }
        val caller = password
        val original = caller.copyOf()
        try {
            val result = repository(broken).createInitialAdmin(initial(secret = caller))
            assertEquals(AccountProvisioningResult.PersistenceError, result)
            assertFalse(result.toString().contains("sensitive exception marker"))
            assertTrue(borrowed!!.all { it == '\u0000' })
            assertArrayEquals(original, caller)
            assertNoAccountsOrMetadata()
        } finally { caller.fill('\u0000'); original.fill('\u0000') }
    }

    @Test fun successfulHashCopyIsWipedAndRunsOutsideTransaction() = runBlocking {
        var borrowed: CharArray? = null
        val observing = object : PasswordHasher by hasher {
            override suspend fun hash(password: CharArray): PasswordHash {
                assertFalse(database.inTransaction())
                borrowed = password
                return hasher.hash(password)
            }
        }
        id(repository(observing).createInitialAdmin(initial()))
        assertTrue(borrowed!!.all { it == '\u0000' })
    }

    @Test fun invalidInputIsRejectedBeforeHashAndPersistence() = runBlocking {
        val forbidden = object : PasswordHasher by hasher {
            override suspend fun hash(password: CharArray): PasswordHash = throw AssertionError("Invalid input hashed")
        }
        assertEquals(AccountProvisioningResult.ValidationError(ProvisioningValidationCode.PASSWORD_LENGTH),
            repository(forbidden).createInitialAdmin(initial(secret = CharArray(7) { 'x' })))
        assertNoAccountsOrMetadata()
    }

    @Test fun existingAccountsBlockBootstrapEvenWithoutMetadata() = runBlocking {
        val actor = administrator()
        database.openHelper.writableDatabase.execSQL("DELETE FROM auth_metadata")
        assertEquals(AccountProvisioningResult.BootstrapUnavailable, repository().createInitialAdmin(initial("retry")))
        assertEquals(actor, database.accountDao().getAccountByNormalizedUsername("founder")!!.id)
        assertNull(database.authMetadataDao().getState())
    }
}
