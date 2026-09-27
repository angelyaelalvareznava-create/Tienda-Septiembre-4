package com.example.tiendita.ui.screens.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidationTest {

    private fun isValidUsername(username: String): Boolean {
        return username.length >= 2 && username.all { it.isLetter() }
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() } &&
                password.any { !it.isLetterOrDigit() } &&
                password.none { it.isWhitespace() }
    }

    @Test
    fun `username validation rules`() {
        // Too short
        assertFalse(isValidUsername("a"))
        // Contains numbers
        assertFalse(isValidUsername("admin1"))
        // Contains special characters
        assertFalse(isValidUsername("admin!"))
        // Contains spaces
        assertFalse(isValidUsername("ad min"))
        // Valid username
        assertTrue(isValidUsername("admin"))
        assertTrue(isValidUsername("Angel"))
    }

    @Test
    fun `password validation rules`() {
        // Too short (< 8 chars)
        assertFalse(isValidPassword("Ab1!"))
        // Missing number
        assertFalse(isValidPassword("AdminPass!"))
        // Missing uppercase letter
        assertFalse(isValidPassword("admin123!"))
        // Missing special character
        assertFalse(isValidPassword("AdminPass1"))
        // Contains spaces
        assertFalse(isValidPassword("Admin 1!"))
        // Valid password
        assertTrue(isValidPassword("Admin123!"))
        assertTrue(isValidPassword("P@ssw0rd2026"))
    }
}
