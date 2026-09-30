package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendita.auth.AuthenticatedAccount
import com.example.tiendita.repository.*
import com.example.tiendita.session.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed interface AppStartupState {
    data object Initializing : AppStartupState
    data object NeedsInitialAdmin : AppStartupState
    data object NeedsLogin : AppStartupState
    data class Authenticated(val account: AuthenticatedAccount) : AppStartupState
    data object Guest : AppStartupState
    data object Error : AppStartupState
}

class AppStartupViewModel(private val bootstrap: BootstrapRepository, private val session: SessionActions) : ViewModel() {
    private val mutableState = MutableStateFlow<AppStartupState>(AppStartupState.Initializing)
    val state = mutableState.asStateFlow()
    private var observation: Job? = null
    private val guestRequested = MutableStateFlow(false)
    init { observe() }
    private fun observe() {
        observation?.cancel()
        mutableState.value = AppStartupState.Initializing
        observation = viewModelScope.launch {
            combine(bootstrap.observe(), session.state, guestRequested) { boot, current, guest ->
                when {
                    current is SessionState.Initializing -> AppStartupState.Initializing
                    current is SessionState.Error -> if (current.reason == SessionError.INVALID_CREDENTIALS) AppStartupState.NeedsLogin else AppStartupState.Error
                    !boot.consumed && boot.accounts == 0 -> AppStartupState.NeedsInitialAdmin
                    !boot.consumed -> AppStartupState.Error
                    current is SessionState.Authenticated -> AppStartupState.Authenticated(current.account)
                    guest -> AppStartupState.Guest
                    else -> AppStartupState.NeedsLogin
                }
            }.catch { if (it is CancellationException) throw it; emit(AppStartupState.Error) }
                .collect { mutableState.value = it }
        }
    }
    fun guest() { viewModelScope.launch {
        try { guestRequested.value = true; session.startAsGuest() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutableState.value = AppStartupState.Error }
    } }
    fun logout() { viewModelScope.launch {
        try { guestRequested.value = false; session.logout() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutableState.value = AppStartupState.Error }
    } }
    fun retry() {
        guestRequested.value = false
        observe()
        viewModelScope.launch { session.restore() }
    }
}
