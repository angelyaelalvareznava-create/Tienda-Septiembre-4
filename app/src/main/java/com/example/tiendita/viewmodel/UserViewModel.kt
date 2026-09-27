package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            is UserFormEvent.OnUserTypeChanged -> {
                _state.update { it.copy(userType = event.userType) }
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

    private fun submitData() {
        val currentState = _state.value
        
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

        val errors = listOfNotNull(nombreError, apellidosError, direccionError, telefonoError, emailError)

        if (errors.isNotEmpty()) {
            _state.update { 
                it.copy(
                    errorNombre = nombreError,
                    errorApellidos = apellidosError,
                    errorDireccion = direccionError,
                    errorTelefono = telefonoError,
                    errorEmail = emailError,
                    errorGeneral = errors.first()
                ) 
            }
            return
        }

        if (currentState.guardando) return

        _state.update { it.copy(guardando = true) }
        
        viewModelScope.launch {
            try {
                // 1. Insert into users table
                repository.insertUser(
                    User(
                        nombre = currentState.nombre.trim(),
                        apellidos = currentState.apellidos.trim(),
                        direccion = currentState.direccion.trim(),
                        telefono = currentState.telefono.trim(),
                        email = currentState.email.trim()
                    )
                )

                // 2. Insert into the selected database table (Employees, Clients, or Suppliers)
                when (currentState.userType) {
                    "Empleado" -> {
                        repository.insertEmployee(
                            EmployeeEntity(
                                firstName = currentState.nombre.trim(),
                                lastName = currentState.apellidos.trim(),
                                position = "General",
                                email = currentState.email.trim(),
                                phone = currentState.telefono.trim(),
                                address = currentState.direccion.trim(),
                                hireDate = System.currentTimeMillis(),
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
                        errorGeneral = "No fue posible guardar el usuario"
                    ) 
                }
            }
        }
    }
}
