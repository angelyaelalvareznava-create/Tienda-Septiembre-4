package com.example.tiendita.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.tiendita.R
import com.example.tiendita.di.AppDependencies
import com.example.tiendita.repository.DirectoryKind
import com.example.tiendita.ui.components.LocalSessionState
import com.example.tiendita.ui.screens.auth.InitialAdminScreen
import com.example.tiendita.ui.screens.calendar.CalendarScreen
import com.example.tiendita.ui.screens.clients.ClientsScreen
import com.example.tiendita.ui.screens.common.*
import com.example.tiendita.ui.screens.database.DatabaseDataScreen
import com.example.tiendita.ui.screens.employees.EmployeesScreen
import com.example.tiendita.ui.screens.home.HomeScreen
import com.example.tiendita.ui.screens.login.LoginScreen
import com.example.tiendita.ui.screens.profile.EditProfileScreen
import com.example.tiendita.ui.screens.registration.AccountFormScreen
import com.example.tiendita.ui.screens.suppliers.SuppliersScreen
import com.example.tiendita.ui.screens.welcome.WelcomeScreen
import com.example.tiendita.viewmodel.*

@Composable fun NexoApp(dependencies: AppDependencies, navController: NavHostController = rememberNavController()) {
    val startup: AppStartupViewModel = viewModel(factory = ExplicitViewModelFactory(AppStartupViewModel::class.java) {
        AppStartupViewModel(dependencies.bootstrap, dependencies.session) })
    val login: LoginViewModel = viewModel(factory = LoginViewModelFactory(dependencies.session))
    // Activity-owned: metadata consumption must not cancel the automatic login when the route changes.
    val initial: AccountCreationViewModel = viewModel(key = "initial-admin", factory = ExplicitViewModelFactory(AccountCreationViewModel::class.java) {
        AccountCreationViewModel(dependencies.provisioning, dependencies.session, true) })
    val startupState by startup.state.collectAsState()
    val session by dependencies.session.state.collectAsState()
    val state = when {
        session is com.example.tiendita.session.SessionState.Initializing -> AppStartupState.Initializing
        startupState is AppStartupState.Authenticated && session is com.example.tiendita.session.SessionState.Authenticated ->
            AppStartupState.Authenticated((session as com.example.tiendita.session.SessionState.Authenticated).account)
        startupState is AppStartupState.Authenticated -> AppStartupState.Initializing
        else -> startupState
    }
    Box(Modifier.fillMaxSize().navigationBarsPadding()) {
      CompositionLocalProvider(LocalSessionState provides session) {
        when (state) {
            AppStartupState.Initializing -> PublicStatusScreen(stringResource(R.string.auth_loading_title), loading = true)
            AppStartupState.Error -> PublicStatusScreen(stringResource(R.string.auth_error_title), message = stringResource(R.string.auth_error), onRetry = startup::retry)
            else -> NexoNavGraph(dependencies, state, startup, login, initial, navController)
        }
      }
    }
}

@Composable fun NexoNavGraph(dependencies: AppDependencies, state: AppStartupState, startup: AppStartupViewModel,
    login: LoginViewModel, initial: AccountCreationViewModel, navController: NavHostController) {
    val target = when (state) {
        AppStartupState.NeedsInitialAdmin -> NexoDestination.InitialAdmin
        AppStartupState.NeedsLogin -> NexoDestination.Login
        else -> NexoDestination.Home
    }
    val identityKey = when (state) {
        is AppStartupState.Authenticated -> "account:${state.account.accountId}"
        else -> state.toString()
    }
    fun navigate(route: String) { navController.navigate(route) { launchSingleTop = true } }
    fun back() { if (!navController.popBackStack()) navigate(NexoDestination.Home) }
    LaunchedEffect(identityKey) {
        navController.navigate(target) { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
    }
    @Composable fun guarded(route: String, content: @Composable () -> Unit) {
        if (RouteAccessPolicy.allows(route, state)) content()
        else GuestRestrictionScreen({ navigate(NexoDestination.Login) }, ::back)
    }
    NavHost(navController, startDestination = target) {
        composable(NexoDestination.InitialAdmin) { guarded(NexoDestination.InitialAdmin) { InitialAdminScreen(initial) } }
        composable(NexoDestination.Welcome) { guarded(NexoDestination.Welcome) { WelcomeScreen { navigate(NexoDestination.Login) } } }
        composable(NexoDestination.Login) { guarded(NexoDestination.Login) {
            LoginScreen(login, startup::guest)
        } }
        composable(NexoDestination.Home) { guarded(NexoDestination.Home) {
            HomeScreen({ navigate(NexoDestination.Calendar) }, { navigate(NexoDestination.Suppliers) },
                { navigate(NexoDestination.Employees) }, { navigate(NexoDestination.Clients) },
                { navigate(NexoDestination.Registration) }, { navigate(NexoDestination.EditProfile) },
                { navigate(NexoDestination.Inventory) }, { navigate(NexoDestination.Movements) },
                { navigate(NexoDestination.DatabaseInspection) }, startup::logout,
                { navigate(NexoDestination.Login) })
        } }
        composable(NexoDestination.Registration) { guarded(NexoDestination.Registration) {
            val vm: AccountCreationViewModel = viewModel(factory = ExplicitViewModelFactory(AccountCreationViewModel::class.java) {
                AccountCreationViewModel(dependencies.provisioning, dependencies.session, false) })
            AccountFormScreen(vm, ::back)
        } }
        composable(NexoDestination.DatabaseInspection) { guarded(NexoDestination.DatabaseInspection) {
            val id = (state as AppStartupState.Authenticated).account.accountId
            val vm: DatabaseStatusViewModel = viewModel(factory = ExplicitViewModelFactory(DatabaseStatusViewModel::class.java) { DatabaseStatusViewModel(dependencies.status, id) })
            DatabaseDataScreen(vm, ::back)
        } }
        for ((route, kind) in listOf(NexoDestination.Employees to DirectoryKind.EMPLOYEES,
            NexoDestination.Clients to DirectoryKind.CLIENTS, NexoDestination.Suppliers to DirectoryKind.SUPPLIERS)) {
            composable(route) { guarded(route) {
                val id = (state as AppStartupState.Authenticated).account.accountId
                val vm: DirectoryViewModel = viewModel(factory = ExplicitViewModelFactory(DirectoryViewModel::class.java) { DirectoryViewModel(dependencies.directories, kind, id) })
                val rows by vm.state.collectAsState()
                when (val current = rows) {
                    DirectoryState.Loading -> PublicStatusScreen(stringResource(R.string.auth_loading_title), loading = true, onBack = ::back)
                    DirectoryState.Error -> PublicStatusScreen(stringResource(R.string.auth_error_title), onBack = ::back, message = stringResource(R.string.auth_error), onRetry = vm::refresh)
                    is DirectoryState.Data -> when (kind) {
                        DirectoryKind.EMPLOYEES -> EmployeesScreen(::back, current.items)
                        DirectoryKind.CLIENTS -> ClientsScreen(::back, current.items)
                        DirectoryKind.SUPPLIERS -> SuppliersScreen(::back, current.items)
                    }
                }
            } }
        }
        composable(NexoDestination.Calendar) { guarded(NexoDestination.Calendar) { CalendarScreen(::back) } }
        composable(NexoDestination.Inventory) { guarded(NexoDestination.Inventory) { FeaturePlaceholderScreen(
            stringResource(R.string.auth_inventory_title), stringResource(R.string.auth_feature_pending), ::back) } }
        composable(NexoDestination.Movements) { guarded(NexoDestination.Movements) { FeaturePlaceholderScreen(
            stringResource(R.string.auth_movements_title), stringResource(R.string.auth_feature_pending), ::back) } }
        composable(NexoDestination.EditProfile) { EditProfileScreen(::back) }
        composable(NexoDestination.GuestRestriction) { GuestRestrictionScreen({ navigate(NexoDestination.Login) }, ::back) }
    }
}
