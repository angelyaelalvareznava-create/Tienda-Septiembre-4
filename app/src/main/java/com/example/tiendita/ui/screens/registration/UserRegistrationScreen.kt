package com.example.tiendita.ui.screens.registration

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
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.ui.components.*
import com.example.tiendita.viewmodel.*

@Composable fun AccountFormScreen(viewModel: AccountCreationViewModel, onBack: (() -> Unit)? = null) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(AccountRole.CONSULTA) }
    val prefix = if (viewModel.initial) "initial" else "registration"
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            NexoTopBar(stringResource(if (viewModel.initial) R.string.auth_initial_title else R.string.auth_register_title),
                onBackClick = onBack, showUserStatus = !viewModel.initial)
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                AdminCard(Modifier.widthIn(max = 620.dp)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (viewModel.initial) Text(stringResource(R.string.auth_initial_intro))
                        NexoTextField(name, { name = it }, stringResource(R.string.auth_display_name), stringResource(R.string.auth_name_hint), Modifier.testTag("$prefix.name"))
                        NexoTextField(username, { username = it }, stringResource(R.string.label_username), stringResource(R.string.auth_username_hint), Modifier.testTag("$prefix.username"))
                        NexoTextField(email, { email = it }, stringResource(R.string.auth_email_optional), "", Modifier.testTag("$prefix.email"))
                        NexoTextField(phone, { phone = it }, stringResource(R.string.auth_phone_optional), stringResource(R.string.auth_phone_hint), Modifier.testTag("$prefix.phone"))
                        NexoPasswordTextField(password, { password = it }, stringResource(R.string.label_password), stringResource(R.string.auth_password_hint), Modifier.testTag("$prefix.password"))
                        NexoPasswordTextField(confirm, { confirm = it }, stringResource(R.string.label_confirm_password), stringResource(R.string.auth_password_hint), Modifier.testTag("$prefix.confirm"))
                        if (!viewModel.initial) {
                            Text(stringResource(R.string.auth_role_label))
                            AccountRole.entries.forEach { option ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(role == option, onClick = { role = option }, enabled = !state.busy, modifier = Modifier.testTag("role.${option.name}"))
                                    Text(roleLabel(option))
                                }
                            }
                        }
                        state.message?.let { Text(authMessage(it), color = if (it == AuthMessage.SUCCESS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                        if (state.busy) { CircularProgressIndicator(); Text(stringResource(R.string.auth_loading)) }
                        AdminButton(stringResource(if (viewModel.initial) R.string.auth_create_admin else R.string.auth_create_account), enabled = !state.busy, onClick = {
                            val secret = password.toCharArray(); val confirmation = confirm.toCharArray()
                            try { viewModel.submit(username, secret, confirmation, name, email, phone, role) }
                            finally { secret.fill('\u0000'); confirmation.fill('\u0000'); password = ""; confirm = "" }
                        })
                        onBack?.let { AdminButton(stringResource(R.string.auth_back), onClick = it) }
                    }
                }
            }
        }
    }
}
