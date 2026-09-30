package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tiendita.session.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class AuthMessage { INVALID_CREDENTIALS, STORAGE_ERROR, MISMATCH, VALIDATION, USERNAME_TAKEN, UNAUTHORIZED, BOOTSTRAP_UNAVAILABLE, EMPLOYEE_ERROR, SUCCESS }
data class AuthFormState(val busy: Boolean = false, val message: AuthMessage? = null)

class LoginViewModel(private val session: SessionActions) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthFormState())
    val state = mutableState.asStateFlow()
    fun submit(username: String, password: CharArray) {
        if (mutableState.value.busy) return
        if (username.isBlank() || password.isEmpty()) { mutableState.value = AuthFormState(message = AuthMessage.INVALID_CREDENTIALS); return }
        val copy = password.copyOf()
        mutableState.value = AuthFormState(busy = true)
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                session.login(username, copy)
                val error = when (val current = session.state.value) {
                    is SessionState.Authenticated -> null
                    is SessionState.Error -> if (current.reason == SessionError.INVALID_CREDENTIALS) AuthMessage.INVALID_CREDENTIALS else AuthMessage.STORAGE_ERROR
                    else -> AuthMessage.STORAGE_ERROR
                }
                mutableState.value = AuthFormState(message = error)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.value = AuthFormState(message = AuthMessage.STORAGE_ERROR) }
            finally { copy.fill('\u0000'); mutableState.update { it.copy(busy = false) } }
        }
    }
}
class LoginViewModelFactory(private val session: SessionActions) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == LoginViewModel::class.java)
        return LoginViewModel(session) as T
    }
}
