package com.example.tiendita.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.screens.calendar.CalendarScreen
import com.example.tiendita.ui.screens.clients.ClientsScreen
import com.example.tiendita.ui.screens.database.DatabaseDataScreen
import com.example.tiendita.ui.screens.employees.EmployeesScreen
import com.example.tiendita.ui.screens.profile.EditProfileScreen
import com.example.tiendita.ui.screens.common.FeaturePlaceholderScreen
import com.example.tiendita.ui.screens.common.GuestRestrictionScreen
import com.example.tiendita.ui.screens.home.HomeScreen
import com.example.tiendita.ui.screens.login.LoginScreen
import com.example.tiendita.ui.screens.registration.UserFormScreen
import com.example.tiendita.ui.screens.suppliers.SuppliersScreen
import com.example.tiendita.ui.screens.welcome.WelcomeScreen
import com.example.tiendita.utils.SessionManager
import com.example.tiendita.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@Composable
fun NexoNavGraph(
    viewModel: UserViewModel,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

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
                onNavigateToRegistration = {
                    val userCount = runBlocking(Dispatchers.IO) { database.userDao().countUsers() }
                    if (userCount == 0 || (SessionManager.isLoggedIn && SessionManager.isAdmin)) {
                        navController.navigate(NexoDestination.Registration)
                    } else {
                        navController.navigate(NexoDestination.GuestRestriction)
                    }
                },
                onNavigateToEditProfile = {
                    if (SessionManager.isLoggedIn) {
                        navController.navigate(NexoDestination.EditProfile)
                    } else {
                        navController.navigate(NexoDestination.GuestRestriction)
                    }
                },
                onNavigateToInventory = { navController.navigate(NexoDestination.Inventory) },
                onNavigateToMovements = { navController.navigate(NexoDestination.Movements) },
                onNavigateToDatabase = { navController.navigate(NexoDestination.DatabaseInspection) },
                onLogout = {
                    SessionManager.logout()
                    navController.navigate(NexoDestination.Login) {
                        popUpTo(NexoDestination.Home) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
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

        composable(NexoDestination.GuestRestriction) {
            GuestRestrictionScreen(
                onNavigateToLogin = {
                    navController.navigate(NexoDestination.Login) {
                        popUpTo(NexoDestination.Home) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NexoDestination.Calendar) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(NexoDestination.Suppliers) {
            SuppliersScreen(onBack = { navController.popBackStack() })
        }
        composable(NexoDestination.Employees) {
            EmployeesScreen(onBack = { navController.popBackStack() })
        }
        composable(NexoDestination.Clients) {
            ClientsScreen(onBack = { navController.popBackStack() })
        }
        composable(NexoDestination.DatabaseInspection) {
            DatabaseDataScreen(onBack = { navController.popBackStack() })
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
