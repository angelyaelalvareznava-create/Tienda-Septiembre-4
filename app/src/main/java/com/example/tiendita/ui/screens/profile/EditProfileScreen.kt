package com.example.tiendita.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.model.User
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.AvatarIcon
import com.example.tiendita.ui.components.NexoPasswordTextField
import com.example.tiendita.ui.components.NexoTextField
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme
import com.example.tiendita.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
    val appliedMsg = stringResource(R.string.msg_profile_updated)

    // Profile edit state
    var currentUser: User? by remember { mutableStateOf(null) }
    var editNombre by rememberSaveable { mutableStateOf("") }
    var editApellidos by rememberSaveable { mutableStateOf("") }
    var editTelefono by rememberSaveable { mutableStateOf("") }
    var editEmail by rememberSaveable { mutableStateOf("") }
    var editPassword by rememberSaveable { mutableStateOf("") }
    var editConfirmPassword by rememberSaveable { mutableStateOf("") }
    var editAddressOrPosition by rememberSaveable { mutableStateOf("") }
    var isEmployee by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(SessionManager.loggedInUsername) {
        if (SessionManager.isLoggedIn && SessionManager.loggedInUsername.isNotEmpty()) {
            val db = AppDatabase.getDatabase(context)
            val user = withContext(Dispatchers.IO) {
                db.userDao().getUserByUsername(SessionManager.loggedInUsername)
            }
            if (user != null) {
                currentUser = user
                editNombre = user.nombre
                editApellidos = user.apellidos
                editTelefono = user.telefono
                editEmail = user.email
                editPassword = user.password
                editConfirmPassword = user.password
                editAddressOrPosition = user.direccion
                isEmployee = user.direccion in listOf("Encargado", "Auxiliar", "Administración") || user.userType == "Empleado" || user.userType == "Admin"
            }
        }
    }

    val passwordsMatch = editPassword == editConfirmPassword
    val confirmPasswordError = if (!passwordsMatch && editConfirmPassword.isNotEmpty()) "Las contraseñas no coinciden" else null

    ScreenBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoTopBar(title = stringResource(R.string.title_edit_profile), onBackClick = onBack)

            if (!SessionManager.isLoggedIn) {
                // Not logged in message
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No has iniciado sesión",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Por favor inicia sesión para ver y editar tu perfil.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AdminButton(
                                text = "Ir a Iniciar Sesión",
                                onClick = onBack
                            )
                        }
                    }
                }
            } else {
                // Edit Profile View (Direct access without re-login)
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
                        text = "Editando perfil de: ${SessionManager.loggedInUsername}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    AdminCard {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AvatarIcon(
                                initials = editNombre.split(" ").let { parts ->
                                    if (parts.isNotEmpty() && parts[0].isNotEmpty()) parts[0].take(2).uppercase() else "U"
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            NexoTextField(
                                value = editNombre,
                                onValueChange = { editNombre = it },
                                label = stringResource(R.string.label_name),
                                placeholder = stringResource(R.string.placeholder_name)
                            )

                            NexoTextField(
                                value = editApellidos,
                                onValueChange = { editApellidos = it },
                                label = stringResource(R.string.label_last_name),
                                placeholder = stringResource(R.string.placeholder_last_name)
                            )

                            if (currentUser?.userType == "Admin") {
                                // Admin has no position and no address
                            } else if (isEmployee) {
                                EditPositionDropdown(
                                    selectedPosition = editAddressOrPosition,
                                    onPositionSelected = { editAddressOrPosition = it }
                                )
                            } else {
                                NexoTextField(
                                    value = editAddressOrPosition,
                                    onValueChange = { editAddressOrPosition = it },
                                    label = stringResource(R.string.label_address),
                                    placeholder = stringResource(R.string.placeholder_address)
                                )
                            }

                            NexoTextField(
                                value = editTelefono,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        editTelefono = it
                                    }
                                },
                                label = stringResource(R.string.label_phone),
                                placeholder = stringResource(R.string.placeholder_phone),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            NexoTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = stringResource(R.string.label_email),
                                placeholder = stringResource(R.string.placeholder_email),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            NexoPasswordTextField(
                                value = editPassword,
                                onValueChange = { editPassword = it },
                                label = stringResource(R.string.label_password),
                                placeholder = stringResource(R.string.placeholder_password),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            NexoPasswordTextField(
                                value = editConfirmPassword,
                                onValueChange = { editConfirmPassword = it },
                                label = stringResource(R.string.label_confirm_password),
                                placeholder = stringResource(R.string.placeholder_confirm_password),
                                errorMessage = confirmPasswordError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            AdminButton(
                                text = stringResource(R.string.btn_apply_changes),
                                enabled = passwordsMatch,
                                onClick = {
                                    currentUser?.let { user ->
                                        val updatedUser = user.copy(
                                            nombre = editNombre.trim(),
                                            apellidos = editApellidos.trim(),
                                            telefono = editTelefono.trim(),
                                            email = editEmail.trim(),
                                            direccion = if (user.userType == "Admin") "Administrador" else editAddressOrPosition.trim(),
                                            password = editPassword.trim()
                                        )
                                        coroutineScope.launch {
                                            val db = AppDatabase.getDatabase(context)
                                            withContext(Dispatchers.IO) {
                                                // 1. Update user table
                                                db.userDao().insertUser(updatedUser)

                                                // 2. Update corresponding entity in employees, clients, or suppliers
                                                val employees = db.employeeDao().getActiveEmployeesFlow().first()
                                                val emp = employees.find { it.email == user.email || it.phone == user.telefono }
                                                if (emp != null) {
                                                    db.employeeDao().updateEmployee(
                                                        emp.copy(
                                                            firstName = editNombre.trim(),
                                                            lastName = editApellidos.trim(),
                                                            position = if (user.userType == "Admin") "Administrador" else editAddressOrPosition.trim(),
                                                            phone = editTelefono.trim(),
                                                            email = editEmail.trim()
                                                        )
                                                    )
                                                }

                                                val clients = db.clientDao().getActiveClientsFlow().first()
                                                val cli = clients.find { it.email == user.email || it.phone == user.telefono }
                                                if (cli != null) {
                                                    db.clientDao().updateClient(
                                                        cli.copy(
                                                            name = "${editNombre.trim()} ${editApellidos.trim()}",
                                                            phone = editTelefono.trim(),
                                                            email = editEmail.trim(),
                                                            address = editAddressOrPosition.trim()
                                                        )
                                                    )
                                                }

                                                val suppliers = db.supplierDao().getActiveSuppliersFlow().first()
                                                val sup = suppliers.find { it.email == user.email || it.phone == user.telefono }
                                                if (sup != null) {
                                                    db.supplierDao().updateSupplier(
                                                        sup.copy(
                                                            companyName = "${editNombre.trim()} ${editApellidos.trim()}",
                                                            phone = editTelefono.trim(),
                                                            email = editEmail.trim(),
                                                            address = editAddressOrPosition.trim()
                                                        )
                                                    )
                                                }
                                            }
                                            snackbarHostState.showSnackbar(
                                                message = appliedMsg,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AdminButton(
                                text = "Cerrar sesión (Sign out)",
                                onClick = {
                                    SessionManager.logout()
                                    onBack()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPositionDropdown(
    selectedPosition: String,
    onPositionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val positions = listOf("Encargado", "Auxiliar", "Administración")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedPosition,
            onValueChange = {},
            readOnly = true,
            label = { Text("Posición / Puesto") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            positions.forEach { pos ->
                DropdownMenuItem(
                    text = { Text(pos) },
                    onClick = {
                        onPositionSelected(pos)
                        expanded = false
                    }
                )
            }
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
