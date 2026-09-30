package com.example.tiendita.session

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.tiendita.auth.*
import com.example.tiendita.data.local.converter.AccountRole
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreSessionStoreTest {
    private suspend fun withFile(block: suspend (File) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.cacheDir, "session-test-${UUID.randomUUID()}").apply { mkdirs() }
        Log.i("AuthInfrastructureTest", "DataStore test API=${Build.VERSION.SDK_INT}")
        try {
            block(File(directory, DataStoreSessionStore.FILE_NAME))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun onlyIdentityIsStoredAndSurvivesDependencyRecreation() = runBlocking {
        withFile { file ->
            val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val dataStore = PreferenceDataStoreFactory.create(scope = firstScope, produceFile = { file })
                DataStoreSessionStore(dataStore).saveAccountId(17)
                val values = dataStore.data.first().asMap()
                assertEquals(1, values.size)
                assertEquals("accountId", values.keys.single().name)
                assertEquals(17L, values.values.single())
            } finally {
                firstScope.coroutineContext.job.cancelAndJoin()
            }
            val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val recreated = PreferenceDataStoreFactory.create(scope = secondScope, produceFile = { file })
                val store = DataStoreSessionStore(recreated)
                assertEquals(17L, store.readAccountId())
                store.clear()
                assertNull(store.readAccountId())
                assertTrue(recreated.data.first().asMap().isEmpty())
            } finally {
                secondScope.coroutineContext.job.cancelAndJoin()
            }
        }
    }

    @Test fun coordinatorRestoresAndClearsMissingAccountWithRealDataStore() = runBlocking {
        withFile { file ->
            val account = AuthenticatedAccount(17, "Someone", null, AccountRole.ALMACEN)
            val rows = MutableStateFlow<AuthenticatedAccount?>(account)
            val repository = object : AuthRepository {
                override suspend fun authenticate(username: String, password: String) = AuthenticationResult.InvalidCredentials
                override suspend fun getAccount(accountId: Long) = rows.value
                override fun observeAccount(accountId: Long) = rows
            }
            val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val store = DataStoreSessionStore(PreferenceDataStoreFactory.create(scope = firstScope, produceFile = { file }))
                store.saveAccountId(17)
                val first = SessionCoordinator(repository, store, firstScope)
                assertEquals(SessionState.Authenticated(account), withTimeout(5_000) {
                    first.state.first { it != SessionState.Initializing }
                })
            } finally {
                firstScope.coroutineContext.job.cancelAndJoin()
            }
            val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val store = DataStoreSessionStore(PreferenceDataStoreFactory.create(scope = secondScope, produceFile = { file }))
                val second = SessionCoordinator(repository, store, secondScope)
                assertEquals(SessionState.Authenticated(account), withTimeout(5_000) {
                    second.state.first { it != SessionState.Initializing }
                })
                rows.value = null
                withTimeout(5_000) { second.state.first { it == SessionState.Guest } }
                assertNull(store.readAccountId())
            } finally {
                secondScope.coroutineContext.job.cancelAndJoin()
            }
        }
    }
}
