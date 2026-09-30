package com.example.tiendita.session

import com.example.tiendita.auth.*
import com.example.tiendita.data.local.converter.AccountRole
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionCoordinatorTest {
    private val account = AuthenticatedAccount(7, "admin", "User", AccountRole.CONSULTA)
    private class Store(var id: Long? = null) : SessionStore {
        var failWrites = false
        var failSave = false
        var failRead = false
        override suspend fun readAccountId(): Long? {
            if (failRead) throw IOException()
            return id
        }
        override suspend fun saveAccountId(accountId: Long) {
            if (failWrites || failSave) throw IOException()
            id = accountId
        }
        override suspend fun clear() {
            if (failWrites) throw IOException()
            id = null
        }
    }
    private class Repository(val rows: MutableStateFlow<AuthenticatedAccount?>) : AuthRepository {
        var readGate: CompletableDeferred<Unit>? = null
        var loginGate: CompletableDeferred<Unit>? = null
        var loginResult: AuthenticationResult = rows.value?.let(AuthenticationResult::Success)
            ?: AuthenticationResult.InvalidCredentials
        var observationFailure = false
        override suspend fun authenticate(username: String, password: String): AuthenticationResult {
            loginGate?.await()
            return loginResult
        }
        override suspend fun getAccount(accountId: Long): AuthenticatedAccount? {
            val snapshot = rows.value
            readGate?.await()
            return snapshot
        }
        override fun observeAccount(accountId: Long): Flow<AuthenticatedAccount?> =
            if (observationFailure) flow { throw IOException() } else rows
    }

    @Test fun initialAndErrorStatesHaveNoPermissions() = runTest {
        val store = Store(7).apply { failWrites = true }
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(null)), store, backgroundScope)
        assertEquals(SessionState.Initializing, coordinator.state.value)
        assertFalse(coordinator.state.value.hasRole(AccountRole.ADMIN))
        runCurrent()
        assertTrue(coordinator.state.value is SessionState.Error)
        assertFalse(coordinator.state.value.hasRole(AccountRole.ADMIN))
    }

    @Test fun restoresUsingCurrentRoomRole() = runTest {
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(account)), Store(7), backgroundScope)
        runCurrent()
        assertEquals(SessionState.Authenticated(account), coordinator.state.value)
        assertFalse(coordinator.state.value.hasRole(AccountRole.ADMIN))
    }

    @Test fun missingOrInactiveAccountClearsPersistedSession() = runTest {
        val store = Store(7)
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(null)), store, backgroundScope)
        runCurrent()
        assertNull(store.id)
        assertEquals(SessionState.Guest, coordinator.state.value)
    }

    @Test fun roleChangesAndDeletionPropagateWithoutRestart() = runTest {
        val rows = MutableStateFlow<AuthenticatedAccount?>(account)
        val store = Store(7)
        val coordinator = SessionCoordinator(Repository(rows), store, backgroundScope)
        runCurrent()
        rows.value = account.copy(role = AccountRole.ADMIN)
        runCurrent()
        assertTrue(coordinator.state.value.hasRole(AccountRole.ADMIN))
        rows.value = null
        runCurrent()
        assertEquals(SessionState.Guest, coordinator.state.value)
        assertNull(store.id)
        rows.value = account
        runCurrent()
        assertEquals(SessionState.Guest, coordinator.state.value)
    }

    @Test fun lateRestorationCannotUndoLogout() = runTest {
        val repository = Repository(MutableStateFlow(account)).apply { readGate = CompletableDeferred() }
        val store = Store(7)
        val coordinator = SessionCoordinator(repository, store, backgroundScope)
        runCurrent()
        coordinator.logout()
        repository.readGate!!.complete(Unit)
        runCurrent()
        assertEquals(SessionState.Guest, coordinator.state.value)
        assertNull(store.id)
    }

    @Test fun lateAuthenticationCannotUndoLogout() = runTest {
        val repository = Repository(MutableStateFlow(account)).apply { loginGate = CompletableDeferred() }
        val store = Store()
        val coordinator = SessionCoordinator(repository, store, backgroundScope)
        runCurrent()
        val login = launch { coordinator.login("admin", "password") }
        runCurrent()
        coordinator.logout()
        repository.loginGate!!.complete(Unit)
        login.join()
        runCurrent()
        assertEquals(SessionState.Guest, coordinator.state.value)
        assertNull(store.id)
    }

    @Test fun guestEntryClearsPreviousIdentity() = runTest {
        val store = Store(7)
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(account)), store, backgroundScope)
        runCurrent()
        coordinator.startAsGuest()
        assertNull(store.id)
        assertEquals(SessionState.Guest, coordinator.state.value)
    }

    @Test fun loginPersistsOnlyAccountIdAndRecreationRestores() = runTest {
        val store = Store()
        val repository = Repository(MutableStateFlow(account))
        val coordinator = SessionCoordinator(repository, store, backgroundScope)
        runCurrent()
        coordinator.login("admin", "password")
        runCurrent()
        assertEquals(7L, store.id)
        val recreated = SessionCoordinator(repository, store, backgroundScope)
        runCurrent()
        assertEquals(SessionState.Authenticated(account), recreated.state.value)
    }

    @Test fun storageFailureOnLoginNeverPublishesAuthenticated() = runTest {
        val store = Store()
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(account)), store, backgroundScope)
        runCurrent()
        store.failSave = true
        coordinator.login("admin", "password")
        assertTrue(coordinator.state.value is SessionState.Error)
        assertNull(store.id)
    }

    @Test fun wrongPasswordAndCollisionHaveExplicitErrors() = runTest {
        val repository = Repository(MutableStateFlow(account))
        val coordinator = SessionCoordinator(repository, Store(), backgroundScope)
        runCurrent()
        repository.loginResult = AuthenticationResult.InvalidCredentials
        coordinator.login("admin", "wrong")
        assertEquals(SessionState.Error(SessionError.INVALID_CREDENTIALS), coordinator.state.value)
        repository.loginResult = AuthenticationResult.UsernameConflict
        coordinator.login("admin", "password")
        assertEquals(SessionState.Error(SessionError.USERNAME_CONFLICT), coordinator.state.value)
    }

    @Test fun roomObservationFailureRevokesAccess() = runTest {
        val repository = Repository(MutableStateFlow(account)).apply { observationFailure = true }
        val coordinator = SessionCoordinator(repository, Store(7), backgroundScope)
        runCurrent()
        assertTrue(coordinator.state.value is SessionState.Error)
        assertFalse(coordinator.state.value.hasRole(AccountRole.ADMIN))
    }

    @Test fun logoutBeforeStartupIsScheduledCannotReactivateSession() = runTest {
        val store = Store(7)
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(account)), store, backgroundScope)
        coordinator.logout()
        runCurrent()
        assertEquals(SessionState.Guest, coordinator.state.value)
        assertNull(store.id)
    }

    @Test fun readFailureIsErrorInsteadOfGuestOrAuthenticated() = runTest {
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(account)),
            Store(7).apply { failRead = true }, backgroundScope)
        runCurrent()
        assertEquals(SessionState.Error(SessionError.STORAGE_OR_ACCOUNT_UNAVAILABLE), coordinator.state.value)
    }

    @Test fun failedLogoutRevokesInMemoryAccessAndReportsPersistenceFailure() = runTest {
        val store = Store(7)
        val rows = MutableStateFlow<AuthenticatedAccount?>(account.copy(role = AccountRole.ADMIN))
        val coordinator = SessionCoordinator(Repository(rows), store, backgroundScope)
        runCurrent()
        store.failWrites = true
        coordinator.logout()
        rows.value = account
        runCurrent()
        assertEquals(SessionState.Error(SessionError.STORAGE_OR_ACCOUNT_UNAVAILABLE), coordinator.state.value)
        assertFalse(coordinator.state.value.hasRole(AccountRole.ADMIN))
    }

    @Test fun invalidStoredIdIsCleared() = runTest {
        val store = Store(-1)
        val coordinator = SessionCoordinator(Repository(MutableStateFlow(account)), store, backgroundScope)
        runCurrent()
        assertEquals(SessionState.Guest, coordinator.state.value)
        assertNull(store.id)
    }
}
