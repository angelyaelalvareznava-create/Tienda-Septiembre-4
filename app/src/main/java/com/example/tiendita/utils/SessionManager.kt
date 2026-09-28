package com.example.tiendita.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var isLoggedIn by mutableStateOf(false)
        private set

    var loggedInUsername by mutableStateOf("")
        private set

    var isAdmin by mutableStateOf(false)
        private set

    fun login(username: String, admin: Boolean = false) {
        isLoggedIn = true
        loggedInUsername = username.ifBlank { "admin" }
        isAdmin = admin || loggedInUsername.lowercase() == "admin"
    }

    fun logout() {
        isLoggedIn = false
        loggedInUsername = ""
        isAdmin = false
    }
}
