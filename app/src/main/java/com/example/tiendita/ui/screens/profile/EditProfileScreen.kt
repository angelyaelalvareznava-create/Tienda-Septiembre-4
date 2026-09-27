package com.example.tiendita.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.AvatarIcon
import com.example.tiendita.ui.components.NexoTextField
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme
import com.example.tiendita.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoMsg = stringResource(R.string.msg_photo_future_phase)
    val appliedMsg = stringResource(R.string.msg_changes_applied)

    var isLoggedIn by remember { mutableStateOf(SessionManager.isLoggedIn) }

    // Login state (if not logged in)
    var loginUsername by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }

    val usernameError = when {
        loginUsername.isEmpty() -> null
        loginUsername.length < 5 -> "El usuario debe tener al menos 5 caracteres"
        loginUsername.length >= 20 -> "El usuario debe tener menos de 20 caracteres"
        !loginUsername.all { it.isLetterOrDigit() || it == '-' || it == '_' } -> "El usuario solo puede contener letras, números, guiones y guiones bajos"
        else -> null
    }

    val passwordError = when {
        loginPassword.isEmpty() -> null
        loginPassword.any { it.isWhitespace() } -> stringResource(R.string.error_password_space)
        loginPassword.length < 8 -> stringResource(R.string.error_password_min_length)
        loginPassword.none { it.isDigit() } -> stringResource(R.string.error_password_digit)
        loginPassword.none { it.isUpperCase() } -> stringResource(R.string.error_password_uppercase)
        loginPassword.none { !it.isLetterOrDigit() } -> stringResource(R.string.error_password_special)
        else -> null
    }

    // Applied state (mock persistency for profile)
    var appliedName by rememberSaveable { mutableStateOf("Administrador NexoStock") }
    var appliedEmail by rememberSaveable { mutableStateOf("administrador@nexostock.local") }
    var appliedPhone by rememberSaveable { mutableStateOf("3312345678") }

    // Form state
    var name by rememberSaveable { mutableStateOf(appliedName) }
    var email by rememberSaveable { mutableStateOf(appliedEmail) }
    var phone by rememberSaveable { mutableStateOf(appliedPhone) }

    val isNameValid = name.trim().length >= 3
    val isEmailValid = email.isNotBlank() && email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }

    val hasChanges = name != appliedName || email != appliedEmail || phone != appliedPhone
    val canApply = isNameValid && isEmailValid && isPhoneValid && hasChanges

    ScreenBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoTopBar(title = stringResource(R.string.title_edit_profile), onBackClick = onBack)

            if (!isLoggedIn) {
                // Login Prompt View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
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
                                text = "Autenticación Requerida",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Debes iniciar sesión para editar tu perfil.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            NexoTextField(
                                value = loginUsername,
                                onValueChange = { loginUsername = it },
                                label = stringResource(R.string.label_username),
                                placeholder = stringResource(R.string.placeholder_username),
                                errorMessage = usernameError,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            NexoTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = stringResource(R.string.label_password),
                                placeholder = stringResource(R.string.placeholder_password),
                                errorMessage = passwordError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = PasswordVisualTransformation()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AdminButton(
                                text = stringResource(R.string.btn_login),
                                onClick = {
                                    val errorMsg = when {
                                        loginUsername.isBlank() && loginPassword.isBlank() -> "Por favor ingresa usuario y contraseña"
                                        loginUsername.isBlank() -> "El usuario es obligatorio"
                                        loginPassword.isBlank() -> "La contraseña es obligatoria"
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
                                                    database.userDao().getUserByCredentials(loginUsername.trim(), loginPassword.trim())
                                                }
                                                val isDefaultAdmin = (loginUsername.trim().lowercase() == "admin" && loginPassword == "Admin123!")
                                                if (dbUser != null || isDefaultAdmin) {
                                                    SessionManager.login(loginUsername)
                                                    isLoggedIn = true
                                                } else {
                                                    loginUsername = ""
                                                    loginPassword = ""
                                                    snackbarHostState.showSnackbar(
                                                        message = "Usuario o contraseña no encontrados en la base de datos",
                                                        duration = SnackbarDuration.Long
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                loginUsername = ""
                                                loginPassword = ""
                                                snackbarHostState.showSnackbar(
                                                    message = "Usuario o contraseña no encontrados en la base de datos",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            AdminButton(
                                text = "Acceso Rápido (Login)",
                                onClick = {
                                    SessionManager.login("admin")
                                    isLoggedIn = true
                                }
                            )
                        }
                    }
                }
            } else {
                // Edit Profile View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sesión iniciada como: ${SessionManager.loggedInUsername}\n${stringResource(R.string.msg_demo_mode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    AdminCard {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarIcon(
                                initials = name.split(" ").let { parts ->
                                    if (parts.size > 1) "${parts[0].first()}${parts[1].first()}"
                                    else parts[0].take(2)
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            AdminButton(
                                text = stringResource(R.string.btn_change_photo),
                                onClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(photoMsg)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            NexoTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = stringResource(R.string.label_name),
                                placeholder = "",
                                errorMessage = if (!isNameValid && name.isNotEmpty()) stringResource(R.string.error_name_min_length) else null,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NexoTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = stringResource(R.string.label_email),
                                placeholder = "",
                                errorMessage = if (!isEmailValid && email.isNotEmpty()) stringResource(R.string.error_email_invalid) else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NexoTextField(
                                value = phone,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        phone = it
                                    }
                                },
                                label = stringResource(R.string.label_phone),
                                placeholder = "",
                                errorMessage = if (!isPhoneValid && phone.isNotEmpty()) stringResource(R.string.error_phone_length) else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            AdminButton(
                                text = stringResource(R.string.btn_apply_changes),
                                enabled = canApply,
                                onClick = {
                                    appliedName = name
                                    appliedEmail = email
                                    appliedPhone = phone
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(appliedMsg)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            AdminButton(
                                text = "Cerrar sesión de perfil",
                                onClick = {
                                    SessionManager.logout()
                                    isLoggedIn = false
                                }
                            )
                        }
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
private fun EditProfileScreenPreview() {
    NexoStockTheme {
        EditProfileScreen(onBack = {})
    }
}
