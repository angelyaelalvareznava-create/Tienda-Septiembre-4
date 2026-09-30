package com.example.tiendita.session

import com.example.tiendita.auth.AuthRepository
import com.example.tiendita.auth.AuthenticatedAccount
import com.example.tiendita.auth.AuthenticationResult
import com.example.tiendita.data.local.converter.AccountRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SessionState {
    data object Initializing : SessionState
    data object Guest : SessionState
    data class Authenticated(val account: AuthenticatedAccount) : SessionState
    data class Error(val reason: SessionError) : SessionState
}

enum class SessionError { STORAGE_OR_ACCOUNT_UNAVAILABLE, INVALID_CREDENTIALS, USERNAME_CONFLICT }

/** A UI convenience, not authorization for writes: those must re-read Room. */
fun SessionState.hasRole(role: AccountRole): Boolean =
    this is SessionState.Authenticated && account.role == role

/** Scope must live as long as the application dependencies, not a composable. */
class SessionCoordinator(
    private val repository: AuthRepository,
    private val store: SessionStore,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private var generation = 0L
    private var accountObservation: Job? = null
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Initializing)
    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    init {
        // Reserve startup's ticket synchronously so a later logout/login wins
        // even if the application scope has not scheduled restoration yet.
        scope.launch { restoreWithTicket(0L) }
    }

    suspend fun restore() {
        restoreWithTicket(beginOperation())
    }

    private suspend fun restoreWithTicket(ticket: Long) {
        try {
            val id = store.readAccountId()
            val account = id?.takeIf { it > 0 }?.let { repository.getAccount(it) }
            mutex.withLock {
                if (ticket != generation) return@withLock
                if (account == null) {
                    store.clear()
                    mutableState.value = SessionState.Guest
                } else {
                    publishAndObserve(account, ticket)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fail(ticket)
        }
    }

    suspend fun login(username: String, password: String) {
        val ticket = beginOperation()
        try {
            mutex.withLock {
                if (ticket == generation) store.clear()
            }
            val result = repository.authenticate(username, password)
            // Re-read after hashing: account may have been disabled or its role changed.
            val current = (result as? AuthenticationResult.Success)?.let {
                repository.getAccount(it.account.accountId)
            }
            mutex.withLock {
                if (ticket != generation) return@withLock
                if (current != null) {
                    store.saveAccountId(current.accountId)
                    publishAndObserve(current, ticket)
                } else {
                    mutableState.value = SessionState.Error(
                        if (result == AuthenticationResult.UsernameConflict) SessionError.USERNAME_CONFLICT
                        else SessionError.INVALID_CREDENTIALS
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fail(ticket)
        }
    }

    suspend fun logout() {
        val ticket = beginOperation()
        try {
            mutex.withLock {
                if (ticket != generation) return@withLock
                store.clear()
                mutableState.value = SessionState.Guest
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fail(ticket)
        }
    }

    suspend fun startAsGuest() = logout()

    private suspend fun beginOperation(): Long = mutex.withLock {
        generation += 1
        accountObservation?.cancel()
        accountObservation = null
        mutableState.value = SessionState.Initializing
        generation
    }

    // Called with mutex held. Generation rejects late restoration/auth/Room results.
    private fun publishAndObserve(account: AuthenticatedAccount, ticket: Long) {
        mutableState.value = SessionState.Authenticated(account)
        accountObservation = scope.launch {
            try {
                repository.observeAccount(account.accountId).collect { current ->
                    mutex.withLock {
                        if (ticket != generation) return@withLock
                        if (current == null) {
                            generation += 1
                            mutableState.value = SessionState.Initializing
                            try {
                                store.clear()
                                mutableState.value = SessionState.Guest
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                mutableState.value = SessionState.Error(SessionError.STORAGE_OR_ACCOUNT_UNAVAILABLE)
                            }
                        } else {
                            mutableState.value = SessionState.Authenticated(current)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                fail(ticket)
            }
        }
    }

    private suspend fun fail(ticket: Long) = mutex.withLock {
        if (ticket == generation) {
            mutableState.value = SessionState.Error(SessionError.STORAGE_OR_ACCOUNT_UNAVAILABLE)
        }
    }
}
