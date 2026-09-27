package com.example.tiendita.viewmodel

data class UserFormState(
    val nombre: String = "",
    val apellidos: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val userType: String = "Empleado", // "Empleado", "Cliente", "Proveedor"
    val errorNombre: String? = null,
    val errorApellidos: String? = null,
    val errorDireccion: String? = null,
    val errorTelefono: String? = null,
    val errorEmail: String? = null,
    val guardando: Boolean = false,
    val registroExitoso: Boolean = false,
    val errorGeneral: String? = null
) {
    val isFormValid: Boolean
        get() = nombre.isNotBlank() &&
                apellidos.isNotBlank() &&
                direccion.isNotBlank() &&
                telefono.length == 10 &&
                email.isNotBlank() &&
                errorNombre == null &&
                errorApellidos == null &&
                errorDireccion == null &&
                errorTelefono == null &&
                errorEmail == null
}

sealed class UserFormEvent {
    data class OnNombreChanged(val nombre: String) : UserFormEvent()
    data class OnApellidosChanged(val apellidos: String) : UserFormEvent()
    data class OnDireccionChanged(val direccion: String) : UserFormEvent()
    data class OnTelefonoChanged(val telefono: String) : UserFormEvent()
    data class OnEmailChanged(val email: String) : UserFormEvent()
    data class OnUserTypeChanged(val userType: String) : UserFormEvent()
    object OnSubmit : UserFormEvent()
    object ResetSuccessState : UserFormEvent()
    object ResetErrorState : UserFormEvent()
}
