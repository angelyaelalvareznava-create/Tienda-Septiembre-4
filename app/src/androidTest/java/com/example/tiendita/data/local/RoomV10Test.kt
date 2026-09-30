package com.example.tiendita.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.entity.AuthMetadataEntity
import com.example.tiendita.data.local.entity.UserAccountEntity
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.session.DataStoreSessionStore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomV10Test {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private suspend fun withDatabase(block: suspend (AppDatabase, String) -> Unit) {
        val name = "room-v10-${UUID.randomUUID()}"
        val database = AppDatabase.buildDatabase(context, name)
        try { block(database, name) } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    private fun account(username: String, role: AccountRole = AccountRole.CONSULTA, active: Boolean = true) =
        UserAccountEntity(employeeId = null, username = username, passwordHash = "test-placeholder",
            salt = "test-placeholder", passwordAlgorithm = "PBKDF2WithHmacSHA256", passwordIterations = 600_000,
            role = role, displayName = null, email = null, phone = null, active = active)

    @Test fun cleanV10AndIndexedLookup() = runBlocking {
        withDatabase { db, _ ->
            assertEquals(10, db.openHelper.writableDatabase.version)
            assertEquals(0, db.accountDao().countAccounts())
            assertNull(db.authMetadataDao().getState())
            val id = db.accountDao().insertAccount(account(" DisplayName "))
            val loaded = db.accountDao().getAccountByNormalizedUsername("displayname")!!
            assertEquals(id, loaded.id)
            assertEquals(" DisplayName ", loaded.username)
            assertEquals("displayname", loaded.normalizedUsername)
            assertEquals(1, loaded.passwordParametersVersion)
            db.openHelper.readableDatabase.query(
                "EXPLAIN QUERY PLAN SELECT * FROM user_accounts WHERE normalized_username = 'displayname'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                val plan = cursor.getString(cursor.getColumnIndexOrThrow("detail"))
                assertTrue(plan, plan.contains("index_user_accounts_normalized_username"))
                assertFalse(plan, plan.contains("SCAN user_accounts"))
            }
        }
    }

    @Test fun equivalentNamesAreRejectedByUniqueIndexWithoutReplacement() = runBlocking {
        withDatabase { db, _ ->
            val id = db.accountDao().insertAccount(account("Admin"))
            for (name in listOf("ADMIN", " admin ")) {
                try {
                    db.accountDao().insertAccount(account(name))
                    fail("Equivalent username accepted")
                } catch (_: SQLiteConstraintException) { }
            }
            assertEquals(1, db.accountDao().countAccounts())
            assertEquals(id, db.accountDao().getAccountByNormalizedUsername("admin")!!.id)
        }
    }

    @Test fun distinctNamesAndActiveAdminCounts() = runBlocking {
        withDatabase { db, _ ->
            db.accountDao().insertAccount(account("admin"))
            db.accountDao().insertAccount(account("other", AccountRole.ADMIN))
            db.accountDao().insertAccount(account("inactive", AccountRole.ADMIN, active = false))
            assertEquals(3, db.accountDao().countAccounts())
            assertEquals(1, db.accountDao().countActiveAdmins())
            assertEquals(AccountRole.CONSULTA, db.accountDao().getAccountByNormalizedUsername("admin")!!.role)
        }
    }

    @Test fun metadataSingletonRejectsDuplicateAndInvalidRawIds() = runBlocking {
        withDatabase { db, _ ->
            db.authMetadataDao().insertInitialState(AuthMetadataEntity(createdAt = 10))
            try {
                db.authMetadataDao().insertInitialState(AuthMetadataEntity())
                fail("Duplicate singleton accepted")
            } catch (_: SQLiteConstraintException) { }
            try {
                db.openHelper.writableDatabase.execSQL(
                    "INSERT INTO auth_metadata VALUES (2, 0, 10, 10)"
                )
                fail("Invalid singleton ID accepted")
            } catch (_: SQLiteConstraintException) { }
            try {
                db.openHelper.writableDatabase.execSQL("UPDATE auth_metadata SET id = 2 WHERE id = 1")
                fail("Invalid singleton update accepted")
            } catch (_: SQLiteConstraintException) { }
            assertEquals(1, db.authMetadataDao().getState()!!.id)
        }
    }

    @Test fun metadataConsumptionIsObservableAndCannotBeRepeated() = runBlocking {
        withDatabase { db, _ ->
            assertEquals(0, db.authMetadataDao().markInitialAdminCreated(20))
            db.authMetadataDao().insertInitialState(AuthMetadataEntity(createdAt = 10))
            assertFalse(db.authMetadataDao().observeState().first()!!.initialAdminCreated)
            assertEquals(1, db.authMetadataDao().markInitialAdminCreated(20))
            assertEquals(0, db.authMetadataDao().markInitialAdminCreated(30))
            val state = db.authMetadataDao().observeState().first()!!
            assertTrue(state.initialAdminCreated)
            assertEquals(10L, state.createdAt)
            assertEquals(20L, state.updatedAt)
        }
    }

    @Test fun metadataSurvivesRoomRecreation() = runBlocking {
        withDatabase { db, name ->
            db.authMetadataDao().insertInitialState(AuthMetadataEntity(createdAt = 10))
            db.authMetadataDao().markInitialAdminCreated(20)
            db.close()
            val reopened = AppDatabase.buildDatabase(context, name)
            try { assertTrue(reopened.authMetadataDao().getState()!!.initialAdminCreated) }
            finally { reopened.close() }
        }
    }

    @Test fun clearingDeletingAndRecreatingIsolatedDataStoreDoesNotChangeMetadata() = runBlocking {
        withDatabase { db, _ ->
            db.authMetadataDao().insertInitialState(AuthMetadataEntity(createdAt = 10))
            db.authMetadataDao().markInitialAdminCreated(20)
            val expected = db.authMetadataDao().getState()
            val file = File(context.cacheDir, "metadata-session-${UUID.randomUUID()}.preferences_pb")
            try {
                repeat(2) {
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    try {
                        val store = DataStoreSessionStore(PreferenceDataStoreFactory.create(
                            scope = scope, produceFile = { file }))
                        store.saveAccountId(17)
                        store.clear()
                        assertNull(store.readAccountId())
                        assertEquals(expected, db.authMetadataDao().getState())
                    } finally { scope.coroutineContext.job.cancelAndJoin() }
                    assertTrue(file.delete())
                }
            } finally { file.delete() }
            assertEquals(expected, db.authMetadataDao().getState())
        }
    }
}
