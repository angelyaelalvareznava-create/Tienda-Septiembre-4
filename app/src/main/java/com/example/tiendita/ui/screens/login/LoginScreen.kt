package com.example.tiendita.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.*
import com.example.tiendita.viewmodel.LoginViewModel

@Composable fun LoginScreen(viewModel: LoginViewModel, onGuest: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            NexoTopBar(stringResource(R.string.title_login), showUserStatus = false)
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                AdminCard(Modifier.widthIn(max = 620.dp)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        NexoTextField(username, { username = it }, stringResource(R.string.label_username),
                            stringResource(R.string.auth_username_hint), Modifier.testTag("login.username"))
                        NexoPasswordTextField(password, { password = it }, stringResource(R.string.label_password),
                            stringResource(R.string.placeholder_password), Modifier.testTag("login.password"))
                        state.message?.let { Text(authMessage(it), color = MaterialTheme.colorScheme.error) }
                        if (state.busy) CircularProgressIndicator()
                        AdminButton(stringResource(R.string.auth_login), enabled = !state.busy, onClick = {
                            val secret = password.toCharArray()
                            try { viewModel.submit(username, secret) } finally { secret.fill('\u0000'); password = "" }
                        })
                        AdminButton(stringResource(R.string.auth_guest), enabled = !state.busy, onClick = { password = ""; onGuest() })
                    }
                }
            }
        }
    }
}
