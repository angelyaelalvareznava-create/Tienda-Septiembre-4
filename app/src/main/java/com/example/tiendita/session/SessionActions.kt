package com.example.tiendita.session

import kotlinx.coroutines.flow.StateFlow

interface SessionActions {
    val state: StateFlow<SessionState>
    suspend fun login(username: String, password: CharArray)
    suspend fun logout()
    suspend fun startAsGuest()
    suspend fun restore()
}

class CoordinatorSessionActions(private val coordinator: SessionCoordinator) : SessionActions {
    override val state = coordinator.state
    override suspend fun login(username: String, password: CharArray) = coordinator.login(username, String(password))
    override suspend fun logout() = coordinator.logout()
    override suspend fun startAsGuest() = coordinator.startAsGuest()
    override suspend fun restore() = coordinator.restore()
}
