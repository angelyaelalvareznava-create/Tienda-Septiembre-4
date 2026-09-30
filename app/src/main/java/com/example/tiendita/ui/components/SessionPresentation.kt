package com.example.tiendita.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.tiendita.R
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.session.SessionState
import com.example.tiendita.viewmodel.AuthMessage

val LocalSessionState = staticCompositionLocalOf<SessionState> { SessionState.Guest }
@Composable fun roleLabel(role: AccountRole): String = stringResource(when (role) {
    AccountRole.ADMIN -> R.string.auth_role_admin
    AccountRole.ALMACEN -> R.string.auth_role_warehouse
    AccountRole.CONSULTA -> R.string.auth_role_read
})
@Composable fun authMessage(message: AuthMessage): String = stringResource(when (message) {
    AuthMessage.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials
    AuthMessage.MISMATCH -> R.string.auth_mismatch
    AuthMessage.VALIDATION -> R.string.auth_validation
    AuthMessage.USERNAME_TAKEN -> R.string.auth_username_taken
    AuthMessage.UNAUTHORIZED -> R.string.auth_unauthorized
    AuthMessage.BOOTSTRAP_UNAVAILABLE -> R.string.auth_bootstrap_unavailable
    AuthMessage.EMPLOYEE_ERROR -> R.string.auth_employee_error
    AuthMessage.SUCCESS -> R.string.auth_created
    else -> R.string.auth_error
})
