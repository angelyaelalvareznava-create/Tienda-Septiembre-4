package com.example.tiendita.viewmodel

data class UserFormState(
    val roleSelected: Boolean = false,
    val userType: String = "", // "Empleado", "Cliente", "Proveedor"
    val username: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val direccion: String = "",
    val position: String = "Encargado", // "Encargado", "Auxiliar", "Administración"
    val telefono: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorUsername: String? = null,
    val errorNombre: String? = null,
    val errorApellidos: String? = null,
    val errorDireccion: String? = null,
    val errorTelefono: String? = null,
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val errorConfirmPassword: String? = null,
    val guardando: Boolean = false,
    val registroExitoso: Boolean = false,
    val errorGeneral: String? = null
) {
    val isFormValid: Boolean
        get() = roleSelected &&
                username.isNotBlank() &&
                nombre.isNotBlank() &&
                apellidos.isNotBlank() &&
                (userType == "Empleado" || direccion.isNotBlank()) &&
                telefono.length == 10 &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                errorUsername == null &&
                errorNombre == null &&
                errorApellidos == null &&
                errorDireccion == null &&
                errorTelefono == null &&
                errorEmail == null &&
                errorPassword == null &&
                errorConfirmPassword == null
}

sealed class UserFormEvent {
    data class OnSelectUserType(val userType: String) : UserFormEvent()
    data class OnPositionChanged(val position: String) : UserFormEvent()
    object OnBackToRoleSelection : UserFormEvent()
    data class OnUsernameChanged(val username: String) : UserFormEvent()
    data class OnNombreChanged(val nombre: String) : UserFormEvent()
    data class OnApellidosChanged(val apellidos: String) : UserFormEvent()
    data class OnDireccionChanged(val direccion: String) : UserFormEvent()
    data class OnTelefonoChanged(val telefono: String) : UserFormEvent()
    data class OnEmailChanged(val email: String) : UserFormEvent()
    data class OnPasswordChanged(val password: String) : UserFormEvent()
    data class OnConfirmPasswordChanged(val confirmPassword: String) : UserFormEvent()
    object OnSubmit : UserFormEvent()
    object ResetSuccessState : UserFormEvent()
    object ResetErrorState : UserFormEvent()
}
