package com.example.tiendita.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.NexoPasswordTextField
import com.example.tiendita.ui.components.NexoTextField
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme
import com.example.tiendita.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val usernameError = when {
        username.isEmpty() -> null
        username.length < 5 -> "El usuario debe tener al menos 5 caracteres"
        username.length >= 20 -> "El usuario debe tener menos de 20 caracteres"
        !username.all { it.isLetterOrDigit() || it == '-' || it == '_' } -> "El usuario solo puede contener letras, números, guiones y guiones bajos"
        else -> null
    }

    val passwordError = when {
        password.isEmpty() -> null
        password.any { it.isWhitespace() } -> stringResource(R.string.error_password_space)
        password.length < 8 -> stringResource(R.string.error_password_min_length)
        password.none { it.isDigit() } -> stringResource(R.string.error_password_digit)
        password.none { it.isUpperCase() } -> stringResource(R.string.error_password_uppercase)
        password.none { !it.isLetterOrDigit() } -> stringResource(R.string.error_password_special)
        else -> null
    }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NexoTopBar(
                title = stringResource(R.string.title_login),
                showUserStatus = false
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdminCard {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.title_nexostock),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        NexoTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = stringResource(R.string.label_username),
                            placeholder = stringResource(R.string.placeholder_username),
                            errorMessage = usernameError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        NexoPasswordTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(R.string.label_password),
                            placeholder = stringResource(R.string.placeholder_password),
                            errorMessage = passwordError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AdminButton(
                            text = stringResource(R.string.btn_login),
                            onClick = {
                                val errorMsg = when {
                                    username.isBlank() && password.isBlank() -> "Por favor ingresa usuario y contraseña"
                                    username.isBlank() -> "El usuario es obligatorio"
                                    password.isBlank() -> "La contraseña es obligatoria"
                                    usernameError != null -> usernameError
                                    passwordError != null -> passwordError
                                    else -> null
                                }
                                if (errorMsg != null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = errorMsg,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    coroutineScope.launch {
                                        try {
                                            val database = AppDatabase.getDatabase(context.applicationContext)
                                            val dbUser = withContext(Dispatchers.IO) {
                                                database.userDao().getUserByCredentials(username.trim(), password.trim())
                                            }
                                            val isDefaultAdmin = (username.trim().lowercase() == "admin" && password == "Admin123!")
                                            if (dbUser != null) {
                                                val isAdmin = (dbUser.userType == "Admin" || dbUser.username.lowercase() == "admin")
                                                SessionManager.login(dbUser.username, admin = isAdmin)
                                                onLogin()
                                            } else if (isDefaultAdmin) {
                                                SessionManager.login("admin", admin = true)
                                                onLogin()
                                            } else {
                                                username = ""
                                                password = ""
                                                snackbarHostState.showSnackbar(
                                                    message = "Usuario o contraseña no encontrados en la base de datos",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } catch (e: Exception) {
                                            username = ""
                                            password = ""
                                            snackbarHostState.showSnackbar(
                                                message = "Usuario o contraseña no encontrados en la base de datos",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        AdminButton(
                            text = "Acceso Rápido (Inicio)",
                            onClick = {
                                // Guest access without login credentials -> Session remains logged out (false)
                                onLogin()
                            }
                        )

                        Text(
                            text = stringResource(R.string.btn_register_link),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onRegister)
                                .padding(8.dp)
                        )

                        Text(
                            text = stringResource(R.string.msg_academic_login),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Top pop-up message (Snackbar) so it is never obscured by the keyboard
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NexoStockTheme {
        LoginScreen(onLogin = {}, onRegister = {})
    }
}
