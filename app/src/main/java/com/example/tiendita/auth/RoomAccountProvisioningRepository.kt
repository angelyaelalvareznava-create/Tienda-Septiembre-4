package com.example.tiendita.auth

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.dao.AccountDao
import com.example.tiendita.data.local.dao.AuthMetadataDao
import com.example.tiendita.data.local.entity.AuthMetadataEntity
import com.example.tiendita.data.local.entity.UserAccountEntity
import com.example.tiendita.database.AppDatabase
import kotlinx.coroutines.CancellationException

/** All supplied DAOs must belong to the supplied database. No session preference grants authorization. */
class RoomAccountProvisioningRepository(
    private val database: AppDatabase,
    private val accounts: AccountDao,
    private val metadata: AuthMetadataDao,
    private val hasher: PasswordHasher
) : AccountProvisioningRepository {
    init {
        require(accounts === database.accountDao() && metadata === database.authMetadataDao()) {
            "Provisioning DAOs must belong to the transaction database"
        }
    }

    override suspend fun createInitialAdmin(request: InitialAdminRequest): AccountProvisioningResult = guarded {
        val input = validated(request.username, request.password, request.displayName, request.email, request.phone)
        withHash(request.password) { hash ->
            database.withTransaction {
                if (metadata.getState()?.initialAdminCreated == true || accounts.countAccounts() != 0)
                    reject(AccountProvisioningResult.BootstrapUnavailable)
                val now = System.currentTimeMillis()
                if (metadata.getState() == null) metadata.insertInitialState(AuthMetadataEntity(createdAt = now))
                val id = insert(input, hash, AccountRole.ADMIN, now)
                if (metadata.markInitialAdminCreated(now) != 1) reject(AccountProvisioningResult.PersistenceError)
                AccountProvisioningResult.Success(id)
            }
        }
    }

    override suspend fun createAccount(actorAccountId: Long, request: AccountRegistrationRequest): AccountProvisioningResult = guarded {
        requireAdministrator(actorAccountId)
        val input = validated(request.username, request.password, request.displayName, request.email, request.phone, request.employeeId)
        withHash(request.password) { hash ->
            database.withTransaction {
                requireAdministrator(actorAccountId)
                AccountProvisioningResult.Success(insert(input, hash, request.role, System.currentTimeMillis()))
            }
        }
    }

    private suspend fun requireAdministrator(id: Long) {
        val actor = if (id > 0) accounts.getAccountById(id) else null
        if (actor == null || !actor.active || actor.role != AccountRole.ADMIN)
            reject(AccountProvisioningResult.Unauthorized)
    }

    private fun validated(username: String, password: CharArray, displayName: String,
        email: String?, phone: String?, employeeId: Long? = null): ValidatedAccountInput =
        when (val result = AccountProvisioningValidator.validate(username, password, displayName, email, phone, employeeId)) {
            is AccountInputValidation.Valid -> result.input
            is AccountInputValidation.Invalid -> reject(AccountProvisioningResult.ValidationError(result.code))
        }

    private suspend fun <T> withHash(password: CharArray, block: suspend (PasswordHash) -> T): T {
        val copy = password.copyOf()
        try {
            // Pbkdf2PasswordHasher performs CPU work on Dispatchers.Default, before any Room transaction.
            val hash = hasher.hash(copy)
            if (hash.algorithm != Pbkdf2PasswordHasher.ALGORITHM ||
                hash.iterations != Pbkdf2PasswordHasher.ITERATIONS_V1 || !hasher.isSupported(hash))
                reject(AccountProvisioningResult.PersistenceError)
            return block(hash)
        } finally { copy.fill('\u0000') }
    }

    private suspend fun insert(input: ValidatedAccountInput, hash: PasswordHash, role: AccountRole, now: Long): Long {
        checkConflicts(input)
        try {
            return accounts.insertAccount(UserAccountEntity(
                employeeId = input.employeeId, username = input.username, normalizedUsername = input.normalizedUsername,
                passwordHash = hash.hash, salt = hash.salt, passwordAlgorithm = hash.algorithm,
                passwordIterations = hash.iterations, passwordParametersVersion = Pbkdf2PasswordHasher.PARAMETERS_VERSION,
                role = role, displayName = input.displayName, email = input.email, phone = input.phone,
                active = true, createdAt = now, updatedAt = now
            ))
        } catch (_: SQLiteConstraintException) {
            // Classify using typed queries, never exception messages or SQL text. Throw to roll back.
            checkConflicts(input)
            reject(AccountProvisioningResult.PersistenceError)
        }
    }

    // Always called inside the write transaction, including constraint-error classification.
    private suspend fun checkConflicts(input: ValidatedAccountInput) {
        if (accounts.getAccountByNormalizedUsername(input.normalizedUsername) != null)
            reject(AccountProvisioningResult.UsernameTaken)
        input.employeeId?.let {
            if (database.employeeDao().getEmployeeById(it) == null)
                reject(AccountProvisioningResult.InvalidEmployeeReference)
            if (accounts.getAccountByEmployeeId(it) != null)
                reject(AccountProvisioningResult.EmployeeAlreadyLinked)
        }
    }

    private class Rollback(val result: AccountProvisioningResult) : RuntimeException()
    private fun reject(result: AccountProvisioningResult): Nothing = throw Rollback(result)

    private suspend fun guarded(block: suspend () -> AccountProvisioningResult): AccountProvisioningResult = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Rollback) {
        e.result
    } catch (_: Exception) {
        AccountProvisioningResult.PersistenceError
    }
}
