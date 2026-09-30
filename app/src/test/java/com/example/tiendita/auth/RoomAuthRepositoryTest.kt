package com.example.tiendita.auth

import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.dao.AccountDao
import com.example.tiendita.data.local.entity.UserAccountEntity
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class RoomAuthRepositoryTest {
    private val dao = mock<AccountDao>()
    private val hasher = object : PasswordHasher {
        override suspend fun hash(password: CharArray) = error("Not used")
        override suspend fun verify(password: CharArray, stored: PasswordHash) = String(password) == " secret "
        override fun isSupported(stored: PasswordHash) = stored.algorithm == "supported"
    }
    private val repository = RoomAuthRepository(dao, hasher)
    private fun account(id: Long = 1, username: String = "Admin", role: AccountRole = AccountRole.CONSULTA,
        active: Boolean = true) = UserAccountEntity(id, null, username, "hash", "salt", "supported",
        600_000, role, "Presentation", null, null, active)

    @Test fun caseInsensitiveUsernameAndUnmodifiedPassword() = runTest {
        val row = account()
        whenever(dao.getAccountsForAuthentication()).thenReturn(listOf(row))
        whenever(dao.getAccountById(1)).thenReturn(row)
        assertEquals(AuthenticationResult.Success(AuthenticatedAccount(1, "Admin", "Presentation", AccountRole.CONSULTA)),
            repository.authenticate(" ADMIN ", " secret "))
        assertEquals(AuthenticationResult.InvalidCredentials, repository.authenticate("admin", "secret"))
    }

    @Test fun missingUserDoesNotCreateFixedAdmin() = runTest {
        whenever(dao.getAccountsForAuthentication()).thenReturn(emptyList())
        assertEquals(AuthenticationResult.InvalidCredentials, repository.authenticate("admin", "Admin123!"))
        assertEquals(AuthenticationResult.InvalidCredentials, repository.authenticate("missing", " secret "))
        verify(dao, never()).insertAccount(any())
    }

    @Test fun normalizedCollisionRejectedWithoutReplacingAccounts() = runTest {
        whenever(dao.getAccountsForAuthentication()).thenReturn(listOf(account(), account(2, "ADMIN")))
        assertEquals(AuthenticationResult.UsernameConflict, repository.authenticate("admin", " secret "))
        verify(dao, never()).updateAccount(any())
        verify(dao, never()).insertAccount(any())
    }

    @Test fun roleComesFromLatestRoomRecordNotUsername() = runTest {
        whenever(dao.getAccountsForAuthentication()).thenReturn(listOf(account(role = AccountRole.ADMIN)))
        whenever(dao.getAccountById(1)).thenReturn(account(role = AccountRole.ALMACEN))
        val result = repository.authenticate("admin", " secret ") as AuthenticationResult.Success
        assertEquals(AccountRole.ALMACEN, result.account.role)
    }

    @Test fun inactiveMissingOrInvalidAccountCannotRestore() = runTest {
        whenever(dao.getAccountById(1)).thenReturn(account(active = false), null,
            account().copy(passwordAlgorithm = "unsupported"))
        repeat(3) { assertNull(repository.getAccount(1)) }
    }

    @Test fun disablingAccountDuringVerificationRejectsLogin() = runTest {
        whenever(dao.getAccountsForAuthentication()).thenReturn(listOf(account()))
        whenever(dao.getAccountById(1)).thenReturn(account(active = false))
        assertEquals(AuthenticationResult.InvalidCredentials, repository.authenticate("admin", " secret "))
    }

    @Test fun roomObservationPropagatesRoleAndInvalidation() = runTest {
        val rows = MutableStateFlow<UserAccountEntity?>(account())
        whenever(dao.observeAccountById(1)).thenReturn(rows)
        assertEquals(AccountRole.CONSULTA, repository.observeAccount(1).first()?.role)
        rows.value = account(role = AccountRole.ADMIN)
        assertEquals(AccountRole.ADMIN, repository.observeAccount(1).first()?.role)
        rows.value = account(active = false)
        assertNull(repository.observeAccount(1).first())
    }

    @Test fun normalizationIsIndependentOfDeviceLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("identity", normalizeUsername(" IDENTITY "))
        } finally {
            Locale.setDefault(original)
        }
    }
}
