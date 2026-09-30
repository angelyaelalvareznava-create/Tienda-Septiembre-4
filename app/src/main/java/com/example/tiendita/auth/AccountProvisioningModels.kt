package com.example.tiendita.auth

import com.example.tiendita.data.local.converter.AccountRole

/** Password arrays are borrowed. Callers must wipe them and not mutate them during a request. */
class InitialAdminRequest(
    val username: String,
    val password: CharArray,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null
)

class AccountRegistrationRequest(
    val username: String,
    val password: CharArray,
    val displayName: String,
    val role: AccountRole,
    val email: String? = null,
    val phone: String? = null,
    val employeeId: Long? = null
)

enum class ProvisioningValidationCode {
    USERNAME_LENGTH, USERNAME_CHARACTERS, PASSWORD_LENGTH, DISPLAY_NAME_LENGTH,
    EMAIL_FORMAT, PHONE_FORMAT, EMPLOYEE_ID
}

sealed interface AccountProvisioningResult {
    data class Success(val accountId: Long) : AccountProvisioningResult
    data class ValidationError(val code: ProvisioningValidationCode) : AccountProvisioningResult
    data object UsernameTaken : AccountProvisioningResult
    data object Unauthorized : AccountProvisioningResult
    data object BootstrapUnavailable : AccountProvisioningResult
    data object EmployeeAlreadyLinked : AccountProvisioningResult
    data object InvalidEmployeeReference : AccountProvisioningResult
    data object PersistenceError : AccountProvisioningResult
}
