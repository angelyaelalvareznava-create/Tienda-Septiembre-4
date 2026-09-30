package com.example.tiendita.auth

import com.example.tiendita.data.local.converter.AccountRole
import java.util.Locale

/** Public identity. Credential material never leaves the repository. */
data class AuthenticatedAccount(
    val accountId: Long,
    val username: String,
    val displayName: String?,
    val role: AccountRole
)

sealed interface AuthenticationResult {
    data class Success(val account: AuthenticatedAccount) : AuthenticationResult
    data object InvalidCredentials : AuthenticationResult
    data object UsernameConflict : AuthenticationResult
}

fun normalizeUsername(username: String): String = username.trim().lowercase(Locale.ROOT)
