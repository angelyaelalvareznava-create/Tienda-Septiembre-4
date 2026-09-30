package com.example.tiendita.session

/** Only SessionCoordinator writes this store; it contains no role or credentials. */
interface SessionStore {
    suspend fun readAccountId(): Long?
    suspend fun saveAccountId(accountId: Long)
    suspend fun clear()
}
