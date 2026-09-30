package com.example.tiendita.auth

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class AccountProvisioningValidatorTest {
    private fun validate(username: String = "Account.123-test_", password: CharArray = "password".toCharArray(),
        name: String = "Display Name", email: String? = null, phone: String? = null, employeeId: Long? = null) =
        AccountProvisioningValidator.validate(username, password, name, email, phone, employeeId)
    private fun invalid(code: ProvisioningValidationCode, value: AccountInputValidation) =
        assertEquals(AccountInputValidation.Invalid(code), value)

    @Test fun validUsernameAndPresentationAreTrimmed() {
        val input = (validate(username = " Mixed.User-1_ ", name = " Name ") as AccountInputValidation.Valid).input
        assertEquals("Mixed.User-1_", input.username)
        assertEquals("mixed.user-1_", input.normalizedUsername)
        assertEquals("Name", input.displayName)
    }

    @Test fun normalizationUsesRootLocale() {
        val old = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val input = (validate(username = " IDENTITY ") as AccountInputValidation.Valid).input
            assertEquals("identity", input.normalizedUsername)
        } finally { Locale.setDefault(old) }
    }

    @Test fun invalidUsernameCharactersAreRejected() {
        for (value in listOf("ab cd", "abc@def", "abc/def", "abc\nxyz"))
            invalid(ProvisioningValidationCode.USERNAME_CHARACTERS, validate(username = value))
        assertTrue(validate(username = "álvaro.123") is AccountInputValidation.Valid)
    }

    @Test fun usernameLengthBoundaries() {
        for (size in listOf(2, 33)) invalid(ProvisioningValidationCode.USERNAME_LENGTH, validate(username = "a".repeat(size)))
        for (size in listOf(3, 32)) assertTrue(validate(username = "a".repeat(size)) is AccountInputValidation.Valid)
    }

    @Test fun passwordLengthBoundaries() {
        for (size in listOf(7, 129)) invalid(ProvisioningValidationCode.PASSWORD_LENGTH, validate(password = CharArray(size) { 'x' }))
        for (size in listOf(8, 128)) assertTrue(validate(password = CharArray(size) { 'x' }) is AccountInputValidation.Valid)
    }

    @Test fun meaningfulPasswordSpacesAreNotTrimmedOrChanged() {
        val password = "  abc   ".toCharArray()
        val original = password.copyOf()
        assertTrue(validate(password = password) is AccountInputValidation.Valid)
        assertArrayEquals(original, password)
        assertTrue(validate(password = CharArray(8) { ' ' }) is AccountInputValidation.Valid)
    }

    @Test fun unicodePasswordNeedsNoUppercaseOrSymbols() {
        assertTrue(validate(password = "密码密码密码密码".toCharArray()) is AccountInputValidation.Valid)
        assertTrue(validate(password = "lowercase".toCharArray()) is AccountInputValidation.Valid)
    }

    @Test fun displayNameBoundariesAfterTrim() {
        for (size in listOf(2, 81)) invalid(ProvisioningValidationCode.DISPLAY_NAME_LENGTH, validate(name = "x".repeat(size)))
        for (size in listOf(3, 80)) assertTrue(validate(name = " " + "x".repeat(size) + " ") is AccountInputValidation.Valid)
    }

    @Test fun optionalEmailIsTrimmedAndValidated() {
        for (email in listOf(null, "", " ")) assertNull((validate(email = email) as AccountInputValidation.Valid).input.email)
        assertEquals("person@example.org", (validate(email = " person@example.org ") as AccountInputValidation.Valid).input.email)
        for (email in listOf("missing-at", "x@localhost", "x y@example.org", "x@@example.org"))
            invalid(ProvisioningValidationCode.EMAIL_FORMAT, validate(email = email))
    }

    @Test fun optionalPhoneUsesDocumentedCanonicalRepresentation() {
        for (phone in listOf(null, "", " ")) assertNull((validate(phone = phone) as AccountInputValidation.Valid).input.phone)
        for (phone in listOf("1234567", "+521234567890", "1".repeat(15)))
            assertEquals(phone, (validate(phone = " $phone ") as AccountInputValidation.Valid).input.phone)
        for (phone in listOf("123456", "1".repeat(16), "123-456-7890", "++12345678", "１２３４５６７８"))
            invalid(ProvisioningValidationCode.PHONE_FORMAT, validate(phone = phone))
    }

    @Test fun employeeReferenceMustBePositive() {
        invalid(ProvisioningValidationCode.EMPLOYEE_ID, validate(employeeId = 0))
        invalid(ProvisioningValidationCode.EMPLOYEE_ID, validate(employeeId = -1))
        assertTrue(validate(employeeId = 1) is AccountInputValidation.Valid)
    }

    @Test fun adminUsernameIsOrdinaryValidatedDataWithoutRole() {
        val input = (validate(username = "admin") as AccountInputValidation.Valid).input
        assertEquals("admin", input.normalizedUsername)
        assertFalse(ValidatedAccountInput::class.java.declaredFields.any { it.name == "role" || it.name == "password" })
    }

    @Test fun errorsAndRequestsDoNotExposeSecrets() {
        val secret = "sensitive-password-marker"
        val request = InitialAdminRequest("abc", secret.toCharArray(), "Name")
        assertFalse(request.toString().contains(secret))
        val outcomes = listOf(AccountProvisioningResult.PersistenceError, AccountProvisioningResult.Unauthorized,
            AccountProvisioningResult.UsernameTaken, AccountProvisioningResult.ValidationError(ProvisioningValidationCode.PASSWORD_LENGTH))
        for (outcome in outcomes) {
            assertFalse(outcome.toString().contains(secret))
            assertFalse(outcome.toString().contains("hash-marker"))
            assertFalse(outcome.toString().contains("salt-marker"))
        }
    }
}
