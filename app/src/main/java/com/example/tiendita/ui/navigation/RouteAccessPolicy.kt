package com.example.tiendita.ui.navigation

import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.viewmodel.AppStartupState
object RouteAccessPolicy {
    fun allows(route: String, state: AppStartupState): Boolean {
        if (state is AppStartupState.Initializing || state is AppStartupState.Error) return false
        if (route == NexoDestination.InitialAdmin) return state is AppStartupState.NeedsInitialAdmin
        if (state is AppStartupState.NeedsInitialAdmin) return false
        val account = (state as? AppStartupState.Authenticated)?.account
        return when (route) {
            NexoDestination.Welcome, NexoDestination.Login, NexoDestination.Home, NexoDestination.Calendar, NexoDestination.GuestRestriction -> true
            NexoDestination.Clients, NexoDestination.Suppliers, NexoDestination.Employees -> account != null
            NexoDestination.Inventory, NexoDestination.Movements -> account?.role in listOf(AccountRole.ADMIN, AccountRole.ALMACEN)
            NexoDestination.Registration, NexoDestination.DatabaseInspection -> account?.role == AccountRole.ADMIN
            else -> false
        }
    }
}
