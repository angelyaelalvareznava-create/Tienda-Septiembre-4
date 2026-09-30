package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.tiendita.auth.*
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.repository.*
import com.example.tiendita.session.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthIntegrationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    @Before fun main() { Dispatchers.setMain(dispatcher) }
    @After fun close() { store.clear(); Dispatchers.resetMain() }
    private fun <T : androidx.lifecycle.ViewModel> owned(vm: T): T { store.put(vm.javaClass.name, vm); return vm }
    private val account = AuthenticatedAccount(1, "admin", "Ordinary", AccountRole.CONSULTA)
    private class Session : SessionActions {
        override val state = MutableStateFlow<SessionState>(SessionState.Guest)
        var loginCalls = 0
        var loginBlock: suspend (String, CharArray) -> Unit = { _, _ -> state.value = SessionState.Error(SessionError.INVALID_CREDENTIALS) }
        override suspend fun login(username: String, password: CharArray) { loginCalls++; loginBlock(username, password) }
        override suspend fun logout() { state.value = SessionState.Guest }
        override suspend fun startAsGuest() = logout()
        override suspend fun restore() { state.value = SessionState.Guest }
    }
    private class Provisioning : AccountProvisioningRepository {
        var calls = 0
        var actor: Long? = null
        var answer: AccountProvisioningResult = AccountProvisioningResult.Success(1)
        override suspend fun createInitialAdmin(request: InitialAdminRequest): AccountProvisioningResult { calls++; return answer }
        override suspend fun createAccount(actorAccountId: Long, request: AccountRegistrationRequest): AccountProvisioningResult {
            actor = actorAccountId; calls++; return answer
        }
    }

    @Test fun loginErrorsAreGenericAndPasswordCopyIsCleared() = runTest {
        val session = Session(); var received: CharArray? = null
        session.loginBlock = { _, password -> received = password; session.state.value = SessionState.Error(SessionError.INVALID_CREDENTIALS) }
        val vm = owned(LoginViewModel(session)); val caller = "password".toCharArray()
        vm.submit("missing", caller); runCurrent()
        assertEquals(AuthMessage.INVALID_CREDENTIALS, vm.state.value.message)
        assertTrue(received!!.all { it == '\u0000' }); assertEquals("password", String(caller))
    }
    @Test fun duplicateLoginSubmissionIsPrevented() = runTest {
        val session = Session(); val release = CompletableDeferred<Unit>()
        session.loginBlock = { _, _ -> release.await(); session.state.value = SessionState.Authenticated(account) }
        val vm = owned(LoginViewModel(session))
        vm.submit("admin", "password".toCharArray()); vm.submit("other", "password".toCharArray())
        assertEquals(1, session.loginCalls); assertTrue(vm.state.value.busy)
        release.complete(Unit); runCurrent(); assertFalse(vm.state.value.busy)
    }
    @Test fun loginStorageFailureIsClosed() = runTest {
        val session = Session(); session.loginBlock = { _, _ -> throw IllegalStateException("private") }
        val vm = owned(LoginViewModel(session)); vm.submit("name", "password".toCharArray()); runCurrent()
        assertEquals(AuthMessage.STORAGE_ERROR, vm.state.value.message)
    }
    @Test fun blankLoginDoesNotAuthenticate() = runTest {
        val session = Session(); val vm = owned(LoginViewModel(session))
        vm.submit("", charArrayOf()); assertEquals(0, session.loginCalls)
    }
    @Test fun firstAccountCreationAuthenticatesThroughSession() = runTest {
        val session = Session(); session.loginBlock = { _, _ -> session.state.value = SessionState.Authenticated(account.copy(role = AccountRole.ADMIN)) }
        val repo = Provisioning(); val vm = owned(AccountCreationViewModel(repo, session, true))
        vm.submit("owner", "password".toCharArray(), "password".toCharArray(), "Owner", null, null); runCurrent()
        assertEquals(1, repo.calls); assertEquals(1, session.loginCalls); assertEquals(AuthMessage.SUCCESS, vm.state.value.message)
    }
    @Test fun confirmationMismatchDoesNotCreateAccount() = runTest {
        val repo = Provisioning(); val vm = owned(AccountCreationViewModel(repo, Session(), true))
        vm.submit("owner", "password".toCharArray(), "different".toCharArray(), "Owner", null, null)
        assertEquals(0, repo.calls); assertEquals(AuthMessage.MISMATCH, vm.state.value.message)
    }
    @Test fun registrationRequiresValidatedAdministrator() = runTest {
        val session = Session(); session.state.value = SessionState.Authenticated(account)
        val repo = Provisioning(); val vm = owned(AccountCreationViewModel(repo, session, false))
        vm.submit("new", "password".toCharArray(), "password".toCharArray(), "New Name", null, null); runCurrent()
        assertEquals(0, repo.calls); assertEquals(AuthMessage.UNAUTHORIZED, vm.state.value.message)
    }
    @Test fun registrationForwardsAccountIdAndControlledError() = runTest {
        val session = Session(); session.state.value = SessionState.Authenticated(account.copy(role = AccountRole.ADMIN))
        val repo = Provisioning(); repo.answer = AccountProvisioningResult.UsernameTaken
        val vm = owned(AccountCreationViewModel(repo, session, false))
        vm.submit("new", "password".toCharArray(), "password".toCharArray(), "New Name", null, null); runCurrent()
        assertEquals(1L, repo.actor); assertEquals(AuthMessage.USERNAME_TAKEN, vm.state.value.message)
    }
    @Test fun startupSelectsBootstrapLoginAndRestoredIdentity() = runTest {
        val session = Session(); val boot = MutableStateFlow(BootstrapStatus(false, 0))
        val vm = owned(AppStartupViewModel(object : BootstrapRepository { override fun observe() = boot }, session))
        assertEquals(AppStartupState.Initializing, vm.state.value); runCurrent()
        assertEquals(AppStartupState.NeedsInitialAdmin, vm.state.value)
        boot.value = BootstrapStatus(true, 1); runCurrent(); assertEquals(AppStartupState.NeedsLogin, vm.state.value)
        session.state.value = SessionState.Authenticated(account); runCurrent()
        assertEquals(AppStartupState.Authenticated(account), vm.state.value)
    }
    @Test fun startupStorageErrorNeverPublishesIdentity() = runTest {
        val session = Session(); session.state.value = SessionState.Authenticated(account)
        val vm = owned(AppStartupViewModel(object : BootstrapRepository {
            override fun observe(): Flow<BootstrapStatus> = flow { throw IllegalStateException("private") }
        }, session)); runCurrent(); assertEquals(AppStartupState.Error, vm.state.value)
    }
    @Test fun guestAndLogoutReturnDifferentPublicStates() = runTest {
        val session = Session(); val vm = owned(AppStartupViewModel(object : BootstrapRepository {
            override fun observe() = flowOf(BootstrapStatus(true, 1))
        }, session)); runCurrent()
        vm.guest(); runCurrent(); assertEquals(AppStartupState.Guest, vm.state.value)
        vm.logout(); runCurrent(); assertEquals(AppStartupState.NeedsLogin, vm.state.value)
    }
    @Test fun initializingAndErrorRevokeStartupAccess() = runTest {
        val session = Session(); session.state.value = SessionState.Authenticated(account)
        val vm = owned(AppStartupViewModel(object : BootstrapRepository { override fun observe() = flowOf(BootstrapStatus(true, 1)) }, session))
        runCurrent(); session.state.value = SessionState.Initializing; runCurrent(); assertEquals(AppStartupState.Initializing, vm.state.value)
        session.state.value = SessionState.Error(SessionError.STORAGE_OR_ACCOUNT_UNAVAILABLE); runCurrent(); assertEquals(AppStartupState.Error, vm.state.value)
    }
}
