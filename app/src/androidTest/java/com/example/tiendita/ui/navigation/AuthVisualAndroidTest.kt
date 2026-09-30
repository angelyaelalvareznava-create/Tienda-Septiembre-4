package com.example.tiendita.ui.navigation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tiendita.R
import com.example.tiendita.auth.*
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.di.AppDependencies
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.repository.*
import com.example.tiendita.session.*
import com.example.tiendita.ui.theme.NexoStockTheme
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthVisualAndroidTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDatabase
    private lateinit var name: String
    private lateinit var file: File
    private lateinit var appScope: CoroutineScope
    private lateinit var storeScope: CoroutineScope
    private lateinit var store: DataStoreSessionStore
    private lateinit var coordinator: SessionCoordinator
    private lateinit var deps: AppDependencies
    private lateinit var nav: NavHostController
    private val secret get() = "demonstration password".toCharArray()
    private fun text(id: Int) = context.getString(id)

    @Before fun prepare() {
        ui.activity.runOnUiThread { ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        name = "visual-${UUID.randomUUID()}"
        db = AppDatabase.buildDatabase(context, name)
        file = File(context.cacheDir, "$name.preferences_pb")
        connect()
    }
    private fun connect() {
        appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        store = DataStoreSessionStore(PreferenceDataStoreFactory.create(scope = storeScope, produceFile = { file }))
        val hasher = Pbkdf2PasswordHasher()
        val auth = RoomAuthRepository(db.accountDao(), hasher)
        coordinator = SessionCoordinator(auth, store, appScope)
        deps = object : AppDependencies {
            override val provisioning = RoomAccountProvisioningRepository(db, db.accountDao(), db.authMetadataDao(), hasher)
            override val session = CoordinatorSessionActions(coordinator)
            override val bootstrap = RoomBootstrapRepository(db)
            override val status = RoomDatabaseStatusRepository(db)
            override val directories = DirectoryRepository(db)
        }
    }
    @After fun clean() {
        ui.runOnIdle { ui.activity.viewModelStore.clear() }
        runBlocking { appScope.coroutineContext.job.cancelAndJoin(); storeScope.coroutineContext.job.cancelAndJoin() }
        db.close(); context.deleteDatabase(name); file.delete()
    }
    private fun show(size: DpSize? = null) {
        ui.setContent {
            val content: @Composable () -> Unit = {
                NexoStockTheme(dynamicColor = false) { nav = rememberNavController(); NexoApp(deps, nav) }
            }
            if (size == null) content()
            else DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size), content)
        }
    }
    private fun waitText(id: Int) { ui.waitUntil(30_000) { ui.onAllNodesWithText(text(id)).fetchSemanticsNodes().isNotEmpty() } }
    private fun route(value: String) { ui.runOnIdle { nav.navigate(value) } }
    private fun input(tag: String, value: String) { ui.onNodeWithTag(tag).performScrollTo().performTextInput(value) }
    private fun click(id: Int) {
        val matcher = hasText(text(id)) and hasClickAction()
        val node = ui.onNode(matcher)
        if (!node.isDisplayed()) {
            val lazy = ui.onAllNodes(hasScrollToNodeAction())
            if (lazy.fetchSemanticsNodes().isNotEmpty()) lazy[0].performScrollToNode(matcher)
            else node.performScrollTo()
        }
        node.assertIsDisplayed().performClick()
    }
    private fun assertTextFits(id: Int) {
        ui.onNodeWithText(text(id)).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            val layouts = mutableListOf<TextLayoutResult>()
            assertTrue(action(layouts))
            assertTrue(layouts.isNotEmpty())
            val layout = layouts.first()
            // Paragraph width may include unused space larger than the measured Text. Check
            // rendered lines and complete text instead; allow only one pixel of rounding.
            assertFalse("Clipped height: ${text(id)}", layout.didOverflowHeight)
            assertEquals(layout.layoutInput.text.length, layout.getLineEnd(layout.lineCount - 1))
            for (line in 0 until layout.lineCount) {
                assertFalse("Ellipsized text: ${text(id)}", layout.isLineEllipsized(line))
                assertTrue("Clipped left edge: ${text(id)}", layout.getLineLeft(line) >= -1f)
                assertTrue("Clipped right edge: ${text(id)}", layout.getLineRight(line) <= layout.size.width + 1f)
            }
        }
    }
    private fun authenticated(role: AccountRole): Long = runBlocking {
        val owner = (deps.provisioning.createInitialAdmin(InitialAdminRequest("owner", secret, "Demo Owner")) as AccountProvisioningResult.Success).accountId
        val username = if (role == AccountRole.ADMIN) "owner" else if (role == AccountRole.CONSULTA) "admin" else "warehouse"
        val id = if (role == AccountRole.ADMIN) owner else (deps.provisioning.createAccount(owner,
            AccountRegistrationRequest(username, secret, "Demo Account", role)) as AccountProvisioningResult.Success).accountId
        deps.session.login(username, secret)
        id
    }

    @Test fun freshStartupAndVisualBootstrapCreateRealAdministrator() {
        show(); waitText(R.string.auth_initial_title)
        input("initial.name", "Video Administrator")
        input("initial.username", "video.owner")
        input("initial.password", "demonstration password")
        input("initial.confirm", "demonstration password")
        click(R.string.auth_create_admin); waitText(R.string.title_home)
        runBlocking {
            val row = db.accountDao().getAccountByNormalizedUsername("video.owner")!!
            assertEquals(AccountRole.ADMIN, row.role)
            assertEquals(row.id, store.readAccountId())
            assertTrue(db.authMetadataDao().getState()!!.initialAdminCreated)
            assertTrue(db.userDao().getAllUsers().isEmpty())
        }
    }

    @Test fun guestCannotOpenDatabaseByDirectRoute() {
        runBlocking { deps.provisioning.createInitialAdmin(InitialAdminRequest("owner", secret, "Demo Owner")) }
        show(); waitText(R.string.title_login); click(R.string.auth_guest); waitText(R.string.title_home)
        route(NexoDestination.DatabaseInspection); waitText(R.string.auth_restricted_title)
        ui.onNodeWithText(text(R.string.db_version)).assertDoesNotExist()
        route(NexoDestination.Registration); waitText(R.string.auth_restricted_title)
        ui.onNodeWithTag("registration.username").assertDoesNotExist()
    }

    @Test fun consultaNamedAdminHasNoAdministrativePrivileges() {
        authenticated(AccountRole.CONSULTA); show(); waitText(R.string.title_home)
        route(NexoDestination.DatabaseInspection); waitText(R.string.auth_restricted_title)
        ui.onNodeWithText(text(R.string.db_version)).assertDoesNotExist()
    }

    @Test fun almacenCanOpenInventoryButCannotOpenDatabase() {
        authenticated(AccountRole.ALMACEN); show(); waitText(R.string.title_home)
        route(NexoDestination.Inventory); waitText(R.string.auth_feature_pending)
        route(NexoDestination.DatabaseInspection); waitText(R.string.auth_restricted_title)
        route(NexoDestination.Registration); waitText(R.string.auth_restricted_title)
        ui.onNodeWithTag("registration.username").assertDoesNotExist()
    }

    @Test fun administratorSeesOnlyRedactedDatabaseAndRevocationRemovesContent() {
        val id = authenticated(AccountRole.ADMIN); show(); waitText(R.string.title_home)
        route(NexoDestination.DatabaseInspection); waitText(R.string.db_operational)
        val row = runBlocking { db.accountDao().getAccountById(id)!! }
        ui.onAllNodesWithText(row.passwordHash).assertCountEquals(0)
        ui.onAllNodesWithText(row.salt).assertCountEquals(0)
        ui.onAllNodesWithText("password_hash", substring = true).assertCountEquals(0)
        runBlocking { db.accountDao().updateAccount(row.copy(role = AccountRole.CONSULTA)) }
        waitText(R.string.auth_restricted_title)
        ui.onNodeWithText(text(R.string.db_version)).assertDoesNotExist()
    }

    @Test fun visualRegistrationCreatesHashWithoutLegacyPassword() {
        authenticated(AccountRole.ADMIN); show(); waitText(R.string.title_home)
        route(NexoDestination.Registration); waitText(R.string.auth_register_title)
        input("registration.name", "Demo Reader")
        input("registration.username", "video.reader")
        input("registration.password", "demonstration password")
        input("registration.confirm", "demonstration password")
        click(R.string.auth_create_account); waitText(R.string.auth_created)
        runBlocking {
            val row = db.accountDao().getAccountByNormalizedUsername("video.reader")!!
            assertEquals(AccountRole.CONSULTA, row.role)
            assertTrue(Pbkdf2PasswordHasher().verify(secret,
                PasswordHash(row.passwordAlgorithm, row.passwordIterations, row.salt, row.passwordHash)))
            assertTrue(db.userDao().getAllUsers().isEmpty())
        }
    }

    @Test fun oldFixedCredentialFailsAndRealLoginAndLogoutWork() {
        runBlocking { deps.provisioning.createInitialAdmin(InitialAdminRequest("owner", secret, "Demo Owner")) }
        show(); waitText(R.string.title_login)
        input("login.username", "admin"); input("login.password", "Admin123!"); click(R.string.auth_login)
        waitText(R.string.auth_invalid_credentials)
        ui.onNodeWithTag("login.username").performTextClearance()
        input("login.username", "owner"); input("login.password", "demonstration password"); click(R.string.auth_login)
        waitText(R.string.title_home); click(R.string.menu_logout); waitText(R.string.title_login)
        runBlocking { assertNull(store.readAccountId()) }
        ui.runOnIdle { assertFalse(nav.popBackStack()) }
        ui.onNodeWithText(text(R.string.title_home)).assertDoesNotExist()
    }

    @Test fun roomAndDataStoreSessionRestoreAndLogoutSurviveRecreatedDependencies() = runBlocking {
        val id = authenticated(AccountRole.ADMIN)
        appScope.coroutineContext.job.cancelAndJoin(); storeScope.coroutineContext.job.cancelAndJoin(); db.close()
        db = AppDatabase.buildDatabase(context, name); connect()
        val restored = withTimeout(30_000) { coordinator.state.first { it !is SessionState.Initializing } }
        assertEquals(id, (restored as SessionState.Authenticated).account.accountId)
        deps.session.logout()
        appScope.coroutineContext.job.cancelAndJoin(); storeScope.coroutineContext.job.cancelAndJoin(); db.close()
        db = AppDatabase.buildDatabase(context, name); connect()
        assertEquals(SessionState.Guest, withTimeout(30_000) { coordinator.state.first { it !is SessionState.Initializing } })
        assertNull(store.readAccountId())
        show(); waitText(R.string.title_login)
    }

    @Test fun compactPhoneBootstrapWelcomeLoginAndRestrictionRemainUsable() {
        show(DpSize(320.dp, 568.dp)); waitText(R.string.auth_initial_title)
        assertTextFits(R.string.auth_initial_title)
        input("initial.name", "Compact Administrator")
        input("initial.username", "compact.owner")
        input("initial.password", "demonstration password")
        input("initial.confirm", "demonstration password")
        click(R.string.auth_create_admin); waitText(R.string.title_home)
        ui.onNode(hasText(text(R.string.menu_database)) and hasClickAction()).performScrollTo()
        assertTextFits(R.string.menu_database)
        route(NexoDestination.Welcome); waitText(R.string.title_nexostock)
        click(R.string.btn_start); waitText(R.string.title_login)
        route(NexoDestination.Home); click(R.string.menu_logout); waitText(R.string.title_login)
        input("login.username", "compact.owner"); input("login.password", "demonstration password")
        click(R.string.auth_login); waitText(R.string.title_home)
        click(R.string.menu_logout); waitText(R.string.title_login)
        click(R.string.auth_guest); waitText(R.string.title_home)
        route(NexoDestination.DatabaseInspection); waitText(R.string.auth_restricted_title)
        assertTextFits(R.string.auth_restricted_title)
        click(R.string.auth_login); waitText(R.string.title_login)
    }

    @Test fun compactPhoneRegistrationAndEntireSanitizedDashboardAreScrollable() {
        authenticated(AccountRole.ADMIN); show(DpSize(320.dp, 568.dp)); waitText(R.string.title_home)
        route(NexoDestination.Registration); waitText(R.string.auth_register_title)
        input("registration.name", "Compact Warehouse")
        input("registration.username", "compact.warehouse")
        input("registration.password", "demonstration password")
        input("registration.confirm", "demonstration password")
        ui.onNodeWithTag("role.ALMACEN").performScrollTo().performClick()
        click(R.string.auth_create_account); waitText(R.string.auth_created)
        click(R.string.auth_back); waitText(R.string.title_home)
        route(NexoDestination.DatabaseInspection); waitText(R.string.db_operational)
        for (label in listOf(R.string.db_accounts, R.string.db_admins, R.string.db_employees,
            R.string.db_clients, R.string.db_suppliers, R.string.db_categories, R.string.db_products,
            R.string.db_warehouses, R.string.db_stock, R.string.db_movements, R.string.db_events)) {
            ui.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text(label)))
            // Cuentas also labels the account-list section; select the first count-card label.
            ui.onAllNodesWithText(text(label))[0].assertIsDisplayed()
        }
        ui.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("compact.warehouse"))
        ui.onNodeWithText("compact.warehouse").assertIsDisplayed()
        ui.onNodeWithText("Compact Warehouse").assertIsDisplayed()
        ui.onNodeWithText(text(R.string.auth_role_warehouse)).assertIsDisplayed()
        ui.onAllNodesWithText("password_hash", substring = true).assertCountEquals(0)
        runBlocking { assertEquals(AccountRole.ALMACEN, db.accountDao().getAccountByNormalizedUsername("compact.warehouse")!!.role) }
    }
}
