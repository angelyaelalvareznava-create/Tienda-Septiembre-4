package com.example.tiendita.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tiendita.R
import com.example.tiendita.ui.demo.NexoSampleData
import com.example.tiendita.ui.screens.calendar.CalendarScreen
import com.example.tiendita.ui.screens.profile.EditProfileScreen
import com.example.tiendita.ui.screens.common.EntityDirectoryScreen
import com.example.tiendita.ui.screens.common.FeaturePlaceholderScreen
import com.example.tiendita.ui.screens.home.HomeScreen
import com.example.tiendita.ui.screens.login.LoginScreen
import com.example.tiendita.ui.screens.registration.UserFormScreen
import com.example.tiendita.ui.screens.welcome.WelcomeScreen
import com.example.tiendita.viewmodel.UserViewModel

@Composable
fun NexoNavGraph(
    viewModel: UserViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NexoDestination.Welcome
    ) {
        composable(NexoDestination.Welcome) {
            WelcomeScreen(
                onStart = {
                    navController.navigate(NexoDestination.Login) {
                        popUpTo(NexoDestination.Welcome) { inclusive = true }
                    }
                }
            )
        }

        composable(NexoDestination.Login) {
            LoginScreen(
                onLogin = {
                    navController.navigate(NexoDestination.Home) {
                        popUpTo(NexoDestination.Login) { inclusive = true }
                    }
                },
                onRegister = {
                    navController.navigate(NexoDestination.Registration)
                }
            )
        }

        composable(NexoDestination.Home) {
            HomeScreen(
                onNavigateToCalendar = { navController.navigate(NexoDestination.Calendar) },
                onNavigateToSuppliers = { navController.navigate(NexoDestination.Suppliers) },
                onNavigateToEmployees = { navController.navigate(NexoDestination.Employees) },
                onNavigateToClients = { navController.navigate(NexoDestination.Clients) },
                onNavigateToRegistration = { navController.navigate(NexoDestination.Registration) },
                onNavigateToEditProfile = { navController.navigate(NexoDestination.EditProfile) },
                onNavigateToInventory = { navController.navigate(NexoDestination.Inventory) },
                onNavigateToMovements = { navController.navigate(NexoDestination.Movements) },
                onLogout = {
                    navController.navigate(NexoDestination.Login) {
                        popUpTo(NexoDestination.Home) { inclusive = true }
                    }
                }
            )
        }

        composable(NexoDestination.Registration) {
            UserFormScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NexoDestination.Calendar) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(NexoDestination.Suppliers) {
            EntityDirectoryScreen(
                title = stringResource(R.string.title_suppliers),
                items = NexoSampleData.suppliers,
                emptyMessage = stringResource(R.string.msg_empty_suppliers),
                loadMoreLabel = stringResource(R.string.btn_load_more_suppliers),
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.Employees) {
            EntityDirectoryScreen(
                title = stringResource(R.string.title_employees),
                items = NexoSampleData.employees,
                emptyMessage = stringResource(R.string.msg_empty_employees),
                loadMoreLabel = stringResource(R.string.btn_load_more_employees),
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.Clients) {
            EntityDirectoryScreen(
                title = stringResource(R.string.title_clients),
                items = NexoSampleData.clients,
                emptyMessage = stringResource(R.string.msg_empty_clients),
                loadMoreLabel = stringResource(R.string.btn_load_more_clients),
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.EditProfile) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(NexoDestination.Inventory) {
            FeaturePlaceholderScreen(
                title = "Inventario",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.Movements) {
            FeaturePlaceholderScreen(
                title = "Movimientos",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
