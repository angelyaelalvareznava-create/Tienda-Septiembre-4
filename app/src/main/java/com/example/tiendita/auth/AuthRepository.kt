package com.example.tiendita.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun authenticate(username: String, password: String): AuthenticationResult
    /** Null means absent, inactive, or invalid credential metadata. */
    suspend fun getAccount(accountId: Long): AuthenticatedAccount?
    fun observeAccount(accountId: Long): Flow<AuthenticatedAccount?>
}
