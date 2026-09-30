package com.example.tiendita.auth

/** Contains presentation/contact data only, never a password or credential hash. */
data class ValidatedAccountInput(
    val username: String,
    val normalizedUsername: String,
    val displayName: String,
    val email: String?,
    val phone: String?,
    val employeeId: Long?
)

sealed interface AccountInputValidation {
    data class Valid(val input: ValidatedAccountInput) : AccountInputValidation
    data class Invalid(val code: ProvisioningValidationCode) : AccountInputValidation
}

object AccountProvisioningValidator {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$")
    // Canonical input: optional leading '+' followed by 7..15 ASCII digits.
    // No inferred country code, stripping of arbitrary separators, or person matching.
    private val phonePattern = Regex("\\+?[0-9]{7,15}")

    fun validate(username: String, password: CharArray, displayName: String,
        email: String? = null, phone: String? = null, employeeId: Long? = null): AccountInputValidation {
        val visible = username.trim()
        val normalized = normalizeUsername(visible)
        val name = displayName.trim()
        val mail = email?.trim()?.takeIf { it.isNotEmpty() }
        val telephone = phone?.trim()?.takeIf { it.isNotEmpty() }
        val error = when {
            normalized.length !in 3..32 -> ProvisioningValidationCode.USERNAME_LENGTH
            !normalized.codePoints().allMatch { Character.isLetterOrDigit(it) || it == 46 || it == 45 || it == 95 } ->
                ProvisioningValidationCode.USERNAME_CHARACTERS
            password.size !in 8..128 -> ProvisioningValidationCode.PASSWORD_LENGTH
            name.length !in 3..80 -> ProvisioningValidationCode.DISPLAY_NAME_LENGTH
            mail != null && (mail.length > 254 || !emailPattern.matches(mail)) -> ProvisioningValidationCode.EMAIL_FORMAT
            telephone != null && !phonePattern.matches(telephone) -> ProvisioningValidationCode.PHONE_FORMAT
            employeeId != null && employeeId <= 0 -> ProvisioningValidationCode.EMPLOYEE_ID
            else -> null
        }
        return if (error != null) AccountInputValidation.Invalid(error)
        else AccountInputValidation.Valid(ValidatedAccountInput(visible, normalized, name, mail, telephone, employeeId))
    }
}
