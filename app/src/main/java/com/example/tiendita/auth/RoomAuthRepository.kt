package com.example.tiendita.auth

import com.example.tiendita.data.local.dao.AccountDao
import com.example.tiendita.data.local.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAuthRepository(
    private val accountDao: AccountDao,
    private val passwordHasher: PasswordHasher
) : AuthRepository {
    override suspend fun authenticate(username: String, password: String): AuthenticationResult {
        val normalized = normalizeUsername(username)
        if (normalized.isEmpty()) return AuthenticationResult.InvalidCredentials
        // Until V10 migration, normalize V9 names in Kotlin, including Unicode.
        val matches = accountDao.getAccountsForAuthentication()
            .filter { normalizeUsername(it.username) == normalized }
        if (matches.size > 1) return AuthenticationResult.UsernameConflict
        val account = matches.singleOrNull() ?: return AuthenticationResult.InvalidCredentials
        val identity = account.toValidIdentity() ?: return AuthenticationResult.InvalidCredentials
        val characters = password.toCharArray()
        return try {
            if (passwordHasher.verify(characters, account.credentials())) {
                val latest = accountDao.getAccountById(identity.accountId)
                val latestIdentity = latest?.toValidIdentity()
                if (latestIdentity != null && latest.credentials() == account.credentials() &&
                    normalizeUsername(latest.username) == normalized) {
                    AuthenticationResult.Success(latestIdentity)
                } else {
                    AuthenticationResult.InvalidCredentials
                }
            } else {
                AuthenticationResult.InvalidCredentials
            }
        } finally {
            characters.fill('\u0000')
        }
    }

    override suspend fun getAccount(accountId: Long): AuthenticatedAccount? =
        accountDao.getAccountById(accountId)?.toValidIdentity()

    override fun observeAccount(accountId: Long): Flow<AuthenticatedAccount?> =
        accountDao.observeAccountById(accountId).map { it?.toValidIdentity() }

    private fun UserAccountEntity.toValidIdentity(): AuthenticatedAccount? {
        if (id <= 0 || !active || normalizeUsername(username).isEmpty() || !passwordHasher.isSupported(credentials())) return null
        return AuthenticatedAccount(id, username, displayName, role)
    }

    private fun UserAccountEntity.credentials() =
        PasswordHash(passwordAlgorithm, passwordIterations, salt, passwordHash)
}
