package com.example.tiendita.auth

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.entity.UserAccountEntity
import com.example.tiendita.database.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomAuthRepositoryAndroidTest {
    @Test fun realRoomCredentialsRoleAndDeactivation() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java).build()
        Log.i("AuthInfrastructureTest", "Room auth test API=${Build.VERSION.SDK_INT}")
        try {
            val hasher = Pbkdf2PasswordHasher()
            val repository = RoomAuthRepository(database.accountDao(), hasher)
            assertEquals(AuthenticationResult.InvalidCredentials, repository.authenticate("admin", "Admin123!"))
            val hash = hasher.hash(" secret ".toCharArray())
            val row = UserAccountEntity(employeeId = null, username = "ADMIN", passwordHash = hash.hash,
                salt = hash.salt, passwordAlgorithm = hash.algorithm, passwordIterations = hash.iterations,
                role = AccountRole.CONSULTA, displayName = null, email = null, phone = null)
            val id = database.accountDao().insertAccount(row)
            val result = repository.authenticate(" admin ", " secret ") as AuthenticationResult.Success
            assertEquals(AccountRole.CONSULTA, result.account.role)
            assertEquals(AuthenticationResult.InvalidCredentials, repository.authenticate("admin", "wrong"))
            database.accountDao().updateAccount(row.copy(id = id, role = AccountRole.ALMACEN))
            assertEquals(AccountRole.ALMACEN, withTimeout(5_000) { repository.observeAccount(id).first() }?.role)
            database.accountDao().updateAccount(row.copy(id = id, active = false))
            assertNull(repository.getAccount(id))
            assertNull(withTimeout(5_000) { repository.observeAccount(id).first() })
        } finally {
            database.close()
        }
    }
}
