package com.example.tiendita.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var isLoggedIn by mutableStateOf(false)
        private set

    var loggedInUsername by mutableStateOf("")
        private set

    fun login(username: String) {
        isLoggedIn = true
        loggedInUsername = username.ifBlank { "admin" }
    }

    fun logout() {
        isLoggedIn = false
        loggedInUsername = ""
    }
}
