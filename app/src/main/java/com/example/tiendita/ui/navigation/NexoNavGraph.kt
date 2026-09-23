package com.example.tiendita.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            FeaturePlaceholderScreen(
                title = "Calendario",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.Suppliers) {
            FeaturePlaceholderScreen(
                title = "Proveedores",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.Employees) {
            FeaturePlaceholderScreen(
                title = "Empleados",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.Clients) {
            FeaturePlaceholderScreen(
                title = "Clientes",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
        }
        composable(NexoDestination.EditProfile) {
            FeaturePlaceholderScreen(
                title = "Editar Perfil",
                description = "Esta funcionalidad estará disponible en la próxima versión.",
                onBack = { navController.popBackStack() }
            )
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
