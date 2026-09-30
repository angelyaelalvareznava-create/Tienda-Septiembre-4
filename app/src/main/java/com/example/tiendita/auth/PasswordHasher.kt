package com.example.tiendita.auth

/** Matches V9's credential columns; lengths are encoded by the Base64 values. */
data class PasswordHash(
    val algorithm: String,
    val iterations: Int,
    val salt: String,
    val hash: String
)

interface PasswordHasher {
    suspend fun hash(password: CharArray): PasswordHash
    suspend fun verify(password: CharArray, stored: PasswordHash): Boolean
    fun isSupported(stored: PasswordHash): Boolean
}
