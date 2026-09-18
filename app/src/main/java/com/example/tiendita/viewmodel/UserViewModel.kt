package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                val error = if (event.nombre.isBlank()) "El nombre es obligatorio" else null
                _state.update { it.copy(nombre = event.nombre, errorNombre = error) }
            }
            is UserFormEvent.OnApellidosChanged -> {
                val error = if (event.apellidos.isBlank()) "Los apellidos son obligatorios" else null
                _state.update { it.copy(apellidos = event.apellidos, errorApellidos = error) }
            }
            is UserFormEvent.OnDireccionChanged -> {
                val error = if (event.direccion.isBlank()) "La dirección es obligatoria" else null
                _state.update { it.copy(direccion = event.direccion, errorDireccion = error) }
            }
            is UserFormEvent.OnTelefonoChanged -> {
                if (event.telefono.length <= 10 && event.telefono.all { it.isDigit() }) {
                    val error = if (event.telefono.length != 10) "El teléfono debe tener 10 dígitos" else null
                    _state.update { it.copy(telefono = event.telefono, errorTelefono = error) }
                }
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

    private fun submitData() {
        val currentState = _state.value
        
        val nombreError = if (currentState.nombre.isBlank()) "El nombre es obligatorio" else null
        val apellidosError = if (currentState.apellidos.isBlank()) "Los apellidos son obligatorios" else null
        val direccionError = if (currentState.direccion.isBlank()) "La dirección es obligatoria" else null
        val telefonoError = when {
            currentState.telefono.isBlank() -> "El teléfono es obligatorio"
            currentState.telefono.length != 10 -> "El teléfono debe tener 10 dígitos"
            else -> null
        }

        val hasError = listOf(nombreError, apellidosError, direccionError, telefonoError).any { it != null }

        if (hasError) {
            _state.update { 
                it.copy(
                    errorNombre = nombreError,
                    errorApellidos = apellidosError,
                    errorDireccion = direccionError,
                    errorTelefono = telefonoError
                ) 
            }
            return
        }

        if (currentState.guardando) return

        _state.update { it.copy(guardando = true) }
        
        viewModelScope.launch {
            try {
                repository.insertUser(
                    User(
                        nombre = currentState.nombre.trim(),
                        apellidos = currentState.apellidos.trim(),
                        direccion = currentState.direccion.trim(),
                        telefono = currentState.telefono.trim()
                    )
                )
                _state.update { 
                    UserFormState(registroExitoso = true) // Reset form and show success
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
