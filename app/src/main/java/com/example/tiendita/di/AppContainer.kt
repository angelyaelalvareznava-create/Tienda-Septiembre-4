package com.example.tiendita.di

import android.content.Context
import com.example.tiendita.auth.*
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.repository.*
import com.example.tiendita.session.*
import kotlinx.coroutines.*

class AppContainer(context: Context) : AppDependencies, AutoCloseable {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val database = AppDatabase.getDatabase(context.applicationContext)
    val passwordHasher = Pbkdf2PasswordHasher()
    val authRepository = RoomAuthRepository(database.accountDao(), passwordHasher)
    override val provisioning: AccountProvisioningRepository = RoomAccountProvisioningRepository(
        database, database.accountDao(), database.authMetadataDao(), passwordHasher)
    val sessionStore = DataStoreSessionStore(context.applicationContext)
    val sessionCoordinator = SessionCoordinator(authRepository, sessionStore, scope)
    override val session: SessionActions = CoordinatorSessionActions(sessionCoordinator)
    override val bootstrap: BootstrapRepository = RoomBootstrapRepository(database)
    override val status: DatabaseStatusRepository = RoomDatabaseStatusRepository(database)
    override val directories = DirectoryRepository(database)
    override fun close() { scope.cancel(); database.close() }
}
