package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendita.auth.*
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.session.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AccountCreationViewModel(private val repository: AccountProvisioningRepository,
    private val session: SessionActions, val initial: Boolean) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthFormState())
    val state = mutableState.asStateFlow()
    fun submit(username: String, password: CharArray, confirm: CharArray, displayName: String,
        email: String?, phone: String?, role: AccountRole = AccountRole.CONSULTA) {
        if (mutableState.value.busy) return
        if (!password.contentEquals(confirm)) { mutableState.value = AuthFormState(message = AuthMessage.MISMATCH); return }
        val copy = password.copyOf()
        mutableState.value = AuthFormState(busy = true)
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val actor = (session.state.value as? SessionState.Authenticated)?.account
                val result = if (initial) repository.createInitialAdmin(InitialAdminRequest(username, copy, displayName, email, phone))
                else if (actor?.role != AccountRole.ADMIN) AccountProvisioningResult.Unauthorized
                else repository.createAccount(actor.accountId, AccountRegistrationRequest(username, copy, displayName, role, email, phone))
                if (result is AccountProvisioningResult.Success && initial) session.login(username, copy)
                val message = when (result) {
                    is AccountProvisioningResult.Success -> if (initial && session.state.value !is SessionState.Authenticated) AuthMessage.STORAGE_ERROR else AuthMessage.SUCCESS
                    is AccountProvisioningResult.ValidationError -> AuthMessage.VALIDATION
                    AccountProvisioningResult.UsernameTaken -> AuthMessage.USERNAME_TAKEN
                    AccountProvisioningResult.Unauthorized -> AuthMessage.UNAUTHORIZED
                    AccountProvisioningResult.BootstrapUnavailable -> AuthMessage.BOOTSTRAP_UNAVAILABLE
                    AccountProvisioningResult.EmployeeAlreadyLinked, AccountProvisioningResult.InvalidEmployeeReference -> AuthMessage.EMPLOYEE_ERROR
                    else -> AuthMessage.STORAGE_ERROR
                }
                mutableState.value = AuthFormState(message = message)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.value = AuthFormState(message = AuthMessage.STORAGE_ERROR) }
            finally { copy.fill('\u0000'); mutableState.update { it.copy(busy = false) } }
        }
    }
}
