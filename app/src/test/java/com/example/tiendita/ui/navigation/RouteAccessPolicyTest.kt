package com.example.tiendita.ui.navigation

import com.example.tiendita.auth.AuthenticatedAccount
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.viewmodel.AppStartupState
import org.junit.Assert.*
import org.junit.Test

class RouteAccessPolicyTest {
    private fun state(role: AccountRole) = AppStartupState.Authenticated(AuthenticatedAccount(1, "admin", null, role))
    @Test fun guestAndConsultaCannotOpenAdministrativeRoutes() {
        for (s in listOf(AppStartupState.Guest, state(AccountRole.CONSULTA))) {
            assertFalse(RouteAccessPolicy.allows(NexoDestination.DatabaseInspection, s))
            assertFalse(RouteAccessPolicy.allows(NexoDestination.Registration, s))
        }
    }
    @Test fun almacenHasInventoryButNotDatabaseAccess() {
        val s = state(AccountRole.ALMACEN)
        assertTrue(RouteAccessPolicy.allows(NexoDestination.Inventory, s))
        assertTrue(RouteAccessPolicy.allows(NexoDestination.Movements, s))
        assertFalse(RouteAccessPolicy.allows(NexoDestination.DatabaseInspection, s))
    }
    @Test fun administratorHasAdministrativeAccess() {
        assertTrue(RouteAccessPolicy.allows(NexoDestination.DatabaseInspection, state(AccountRole.ADMIN)))
        assertTrue(RouteAccessPolicy.allows(NexoDestination.Registration, state(AccountRole.ADMIN)))
    }
    @Test fun initializingAndErrorAreClosed() {
        for (s in listOf(AppStartupState.Initializing, AppStartupState.Error))
            for (r in listOf(NexoDestination.Home, NexoDestination.DatabaseInspection, NexoDestination.InitialAdmin))
                assertFalse(RouteAccessPolicy.allows(r, s))
    }
    @Test fun bootstrapAndMaintenanceAreRestricted() {
        assertTrue(RouteAccessPolicy.allows(NexoDestination.InitialAdmin, AppStartupState.NeedsInitialAdmin))
        assertFalse(RouteAccessPolicy.allows(NexoDestination.InitialAdmin, state(AccountRole.ADMIN)))
        assertFalse(RouteAccessPolicy.allows(NexoDestination.EditProfile, state(AccountRole.ADMIN)))
    }
    @Test fun directoriesRequireIdentityAndCalendarIsPublic() {
        assertFalse(RouteAccessPolicy.allows(NexoDestination.Employees, AppStartupState.Guest))
        assertTrue(RouteAccessPolicy.allows(NexoDestination.Employees, state(AccountRole.CONSULTA)))
        assertTrue(RouteAccessPolicy.allows(NexoDestination.Calendar, AppStartupState.Guest))
    }
}
