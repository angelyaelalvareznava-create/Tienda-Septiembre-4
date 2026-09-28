package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendita.data.local.entity.AdminEntity
import com.example.tiendita.data.local.entity.ClientEntity
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.data.local.entity.SupplierEntity
import com.example.tiendita.model.User
import com.example.tiendita.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(UserFormState())
    val state: StateFlow<UserFormState> = _state.asStateFlow()

    fun onEvent(event: UserFormEvent) {
        when (event) {
            is UserFormEvent.OnSelectUserType -> {
                _state.update { it.copy(roleSelected = true, userType = event.userType) }
            }
            is UserFormEvent.OnPositionChanged -> {
                _state.update { it.copy(position = event.position) }
            }
            UserFormEvent.OnBackToRoleSelection -> {
                _state.update { UserFormState() }
            }
            is UserFormEvent.OnUsernameChanged -> {
                val formatError = validateUsernameFormat(event.username)
                _state.update { it.copy(username = event.username, errorUsername = formatError) }
                if (formatError == null && event.username.isNotBlank()) {
                    viewModelScope.launch {
                        try {
                            val existing = repository.getUserByUsername(event.username.trim())
                            val uniqueError = if (existing != null) "Este nombre de usuario ya está registrado. Por favor elige otro." else null
                            if (_state.value.username == event.username) {
                                _state.update { it.copy(errorUsername = uniqueError) }
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            }
            is UserFormEvent.OnNombreChanged -> {
                val error = when {
                    event.nombre.isBlank() -> "El nombre es obligatorio"
                    event.nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
                    !event.nombre.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo debe contener letras"
                    else -> null
                }
                _state.update { it.copy(nombre = event.nombre, errorNombre = error) }
            }
            is UserFormEvent.OnApellidosChanged -> {
                val error = when {
                    event.apellidos.isBlank() -> "Los apellidos son obligatorios"
                    event.apellidos.length < 2 -> "Los apellidos deben tener al menos 2 caracteres"
                    !event.apellidos.all { it.isLetter() || it.isWhitespace() } -> "Los apellidos solo deben contener letras"
                    else -> null
                }
                _state.update { it.copy(apellidos = event.apellidos, errorApellidos = error) }
            }
            is UserFormEvent.OnDireccionChanged -> {
                val error = when {
                    _state.value.userType == "Empleado" || _state.value.userType == "Admin" -> null
                    event.direccion.isBlank() -> "La dirección es obligatoria"
                    event.direccion.length <= 10 -> "La dirección debe tener más de 10 caracteres"
                    event.direccion.length >= 150 -> "La dirección debe tener menos de 150 caracteres"
                    !event.direccion.any { it.isWhitespace() } -> "La dirección debe contener al menos un espacio"
                    !event.direccion.any { it.isDigit() } -> "La dirección debe incluir al menos un número"
                    else -> null
                }
                _state.update { it.copy(direccion = event.direccion, errorDireccion = error) }
            }
            is UserFormEvent.OnTelefonoChanged -> {
                if (event.telefono.length <= 10 && event.telefono.all { it.isDigit() }) {
                    val error = if (event.telefono.isNotEmpty() && event.telefono.length != 10) "El teléfono debe tener 10 dígitos" else null
                    _state.update { it.copy(telefono = event.telefono, errorTelefono = error) }
                }
            }
            is UserFormEvent.OnEmailChanged -> {
                val error = validateEmail(event.email)
                _state.update { it.copy(email = event.email, errorEmail = error) }
            }
            is UserFormEvent.OnPasswordChanged -> {
                val error = validatePassword(event.password)
                val confirmError = if (_state.value.confirmPassword.isNotEmpty() && _state.value.confirmPassword != event.password) {
                    "Las contraseñas no coinciden"
                } else {
                    null
                }
                _state.update { it.copy(password = event.password, errorPassword = error, errorConfirmPassword = confirmError) }
            }
            is UserFormEvent.OnConfirmPasswordChanged -> {
                val error = if (event.confirmPassword != _state.value.password) {
                    "Las contraseñas no coinciden"
                } else {
                    null
                }
                _state.update { it.copy(confirmPassword = event.confirmPassword, errorConfirmPassword = error) }
            }
            UserFormEvent.OnSubmit -> {
                submitData()
            }
            UserFormEvent.ResetSuccessState -> {
                _state.update { it.copy(registroExitoso = false) }
            }
            UserFormEvent.ResetErrorState -> {
                _state.update { it.copy(errorGeneral = null) }
            }
        }
    }

    private fun validateUsernameFormat(username: String): String? {
        return when {
            username.isBlank() -> "El nombre de usuario es obligatorio"
            username.length < 5 -> "El usuario debe tener al menos 5 caracteres"
            username.length >= 20 -> "El usuario debe tener menos de 20 caracteres"
            !username.all { it.isLetterOrDigit() || it == '-' || it == '_' } -> "El usuario solo puede contener letras, números, guiones y guiones bajos"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es obligatorio"
            email.length < 5 -> "El correo electrónico debe tener al menos 5 caracteres"
            email.count { it == '@' } != 1 -> "El correo electrónico debe tener exactamente un símbolo @"
            else -> {
                val parts = email.split('@')
                if (parts.size != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                    "El correo electrónico debe contener texto antes y después del @"
                } else if (!parts[1].contains('.')) {
                    "El correo electrónico debe tener un punto después del @"
                } else {
                    null
                }
            }
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.any { it.isWhitespace() } -> "La contraseña no debe contener espacios"
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            password.none { it.isDigit() } -> "La contraseña debe incluir al menos un número"
            password.none { it.isUpperCase() } -> "La contraseña debe incluir al menos una letra mayúscula"
            password.none { !it.isLetterOrDigit() } -> "La contraseña debe incluir al menos un carácter especial"
            else -> null
        }
    }

    private fun submitData() {
        val currentState = _state.value
        
        val usernameFormatError = validateUsernameFormat(currentState.username)
        val nombreError = when {
            currentState.nombre.isBlank() -> "El nombre es obligatorio"
            currentState.nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            !currentState.nombre.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo debe contener letras"
            else -> null
        }
        val apellidosError = when {
            currentState.apellidos.isBlank() -> "Los apellidos son obligatorios"
            currentState.apellidos.length < 2 -> "Los apellidos deben tener al menos 2 caracteres"
            !currentState.apellidos.all { it.isLetter() || it.isWhitespace() } -> "Los apellidos solo deben contener letras"
            else -> null
        }
        val direccionError = when {
            currentState.userType == "Empleado" || currentState.userType == "Admin" -> null
            currentState.direccion.isBlank() -> "La dirección es obligatoria"
            currentState.direccion.length <= 10 -> "La dirección debe tener más de 10 caracteres"
            currentState.direccion.length >= 150 -> "La dirección debe tener menos de 150 caracteres"
            !currentState.direccion.any { it.isWhitespace() } -> "La dirección debe contener al menos un espacio"
            !currentState.direccion.any { it.isDigit() } -> "La dirección debe incluir al menos un número"
            else -> null
        }
        val telefonoError = when {
            currentState.telefono.isBlank() -> "El teléfono es obligatorio"
            currentState.telefono.length != 10 -> "El teléfono debe tener 10 dígitos"
            else -> null
        }
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val confirmPasswordError = when {
            currentState.confirmPassword.isBlank() -> "La confirmación de contraseña es obligatoria"
            currentState.confirmPassword != currentState.password -> "Las contraseñas no coinciden"
            else -> null
        }

        viewModelScope.launch {
            val existingUser = try {
                repository.getUserByUsername(currentState.username.trim())
            } catch (e: Exception) {
                null
            }
            val usernameUniqueError = if (existingUser != null) "Este nombre de usuario ya está registrado. Por favor elige otro." else null

            val errors = listOfNotNull(
                usernameFormatError,
                usernameUniqueError,
                nombreError,
                apellidosError,
                direccionError,
                telefonoError,
                emailError,
                passwordError,
                confirmPasswordError
            )

            if (errors.isNotEmpty()) {
                _state.update { 
                    it.copy(
                        errorUsername = usernameFormatError ?: usernameUniqueError,
                        errorNombre = nombreError,
                        errorApellidos = apellidosError,
                        errorDireccion = direccionError,
                        errorTelefono = telefonoError,
                        errorEmail = emailError,
                        errorPassword = passwordError,
                        errorConfirmPassword = confirmPasswordError,
                        errorGeneral = errors.first()
                    ) 
                }
                return@launch
            }

            if (currentState.guardando) return@launch

            _state.update { it.copy(guardando = true) }
            
            try {
                // 1. Insert into users table
                repository.insertUser(
                    User(
                        username = currentState.username.trim(),
                        nombre = currentState.nombre.trim(),
                        apellidos = currentState.apellidos.trim(),
                        direccion = when (currentState.userType) {
                            "Empleado" -> currentState.position
                            "Admin" -> "Administrador"
                            else -> currentState.direccion.trim()
                        },
                        telefono = currentState.telefono.trim(),
                        email = currentState.email.trim(),
                        password = currentState.password.trim(),
                        userType = currentState.userType
                    )
                )

                // 2. Insert into the selected database table (Employees, Admins, Clients, or Suppliers)
                when (currentState.userType) {
                    "Empleado" -> {
                        repository.insertEmployee(
                            EmployeeEntity(
                                firstName = currentState.nombre.trim(),
                                lastName = currentState.apellidos.trim(),
                                position = currentState.position,
                                email = currentState.email.trim(),
                                phone = currentState.telefono.trim(),
                                address = null,
                                hireDate = System.currentTimeMillis(),
                                active = true
                            )
                        )
                    }
                    "Admin" -> {
                        repository.insertAdmin(
                            AdminEntity(
                                firstName = currentState.nombre.trim(),
                                lastName = currentState.apellidos.trim(),
                                email = currentState.email.trim(),
                                phone = currentState.telefono.trim(),
                                active = true
                            )
                        )
                    }
                    "Cliente" -> {
                        repository.insertClient(
                            ClientEntity(
                                name = "${currentState.nombre.trim()} ${currentState.apellidos.trim()}",
                                contactName = "${currentState.nombre.trim()} ${currentState.apellidos.trim()}",
                                phone = currentState.telefono.trim(),
                                email = currentState.email.trim(),
                                address = currentState.direccion.trim(),
                                notes = "Registrado desde app",
                                active = true
                            )
                        )
                    }
                    "Proveedor" -> {
                        repository.insertSupplier(
                            SupplierEntity(
                                companyName = "${currentState.nombre.trim()} ${currentState.apellidos.trim()}",
                                contactName = "${currentState.nombre.trim()} ${currentState.apellidos.trim()}",
                                phone = currentState.telefono.trim(),
                                email = currentState.email.trim(),
                                address = currentState.direccion.trim(),
                                notes = "Registrado desde app",
                                active = true
                            )
                        )
                    }
                }

                _state.update { 
                    UserFormState(registroExitoso = true)
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        guardando = false, 
                        errorGeneral = "Error al guardar el usuario: ${e.localizedMessage ?: e.message ?: "Desconocido"}"
                    ) 
                }
            }
        }
    }
}
