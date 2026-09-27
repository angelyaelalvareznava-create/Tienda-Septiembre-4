package com.example.tiendita.viewmodel

import app.cash.turbine.test
import com.example.tiendita.model.User
import com.example.tiendita.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UserRepository
    private lateinit var viewModel: UserViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = UserViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertFalse(initialState.roleSelected)
            assertEquals("", initialState.username)
            assertEquals("", initialState.nombre)
            assertEquals("", initialState.apellidos)
            assertEquals("", initialState.direccion)
            assertEquals("", initialState.telefono)
            assertEquals("", initialState.email)
            assertEquals("", initialState.password)
            assertEquals("", initialState.confirmPassword)
            assertFalse(initialState.isFormValid)
        }
    }

    @Test
    fun `email validation rules work correctly`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            // Too short (< 5 chars)
            viewModel.onEvent(UserFormEvent.OnEmailChanged("a@b"))
            val stateShort = awaitItem()
            assertEquals("El correo electrónico debe tener al menos 5 caracteres", stateShort.errorEmail)

            // Missing @
            viewModel.onEvent(UserFormEvent.OnEmailChanged("testdomain.com"))
            val stateNoAt = awaitItem()
            assertEquals("El correo electrónico debe tener exactamente un símbolo @", stateNoAt.errorEmail)

            // Multiple @
            viewModel.onEvent(UserFormEvent.OnEmailChanged("test@@domain.com"))
            val stateMultiAt = awaitItem()
            assertEquals("El correo electrónico debe tener exactamente un símbolo @", stateMultiAt.errorEmail)

            // Missing text after @
            viewModel.onEvent(UserFormEvent.OnEmailChanged("test@"))
            val stateNoAfter = awaitItem()
            assertEquals("El correo electrónico debe contener texto antes y después del @", stateNoAfter.errorEmail)

            // Missing period after @
            viewModel.onEvent(UserFormEvent.OnEmailChanged("test@domain"))
            val stateNoPeriod = awaitItem()
            assertEquals("El correo electrónico debe tener un punto después del @", stateNoPeriod.errorEmail)

            // Valid email
            viewModel.onEvent(UserFormEvent.OnEmailChanged("test@example.com"))
            val stateValid = awaitItem()
            assertNull(stateValid.errorEmail)
        }
    }

    @Test
    fun `form becomes valid when all fields are correct`() = runTest {
        whenever(repository.getUserByUsername(any())).thenReturn(null)

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(UserFormEvent.OnSelectUserType("Empleado"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnUsernameChanged("admin123"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnNombreChanged("Juan"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnApellidosChanged("Perez"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnTelefonoChanged("1234567890"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnEmailChanged("juan@example.com"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnPasswordChanged("Password123!"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnConfirmPasswordChanged("Password123!"))
            
            val finalState = awaitItem()
            assertTrue(finalState.isFormValid)
        }
    }

    @Test
    fun `onSubmit with valid data calls repository and updates state`() = runTest {
        whenever(repository.getUserByUsername(any())).thenReturn(null)
        whenever(repository.insertUser(any())).thenReturn(1L)
        whenever(repository.insertEmployee(any())).thenReturn(1L)
        
        viewModel.state.test {
            awaitItem() // initial state
            
            // Set valid data
            viewModel.onEvent(UserFormEvent.OnSelectUserType("Empleado"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnUsernameChanged("admin123"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnNombreChanged("Juan"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnApellidosChanged("Perez"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnTelefonoChanged("1234567890"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnEmailChanged("juan@example.com"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnPasswordChanged("Password123!"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnConfirmPasswordChanged("Password123!"))
            val validState = awaitItem()
            assertTrue(validState.isFormValid)
            
            // Submit
            viewModel.onEvent(UserFormEvent.OnSubmit)
            
            val savingState = awaitItem()
            assertTrue(savingState.guardando)
            
            // Fast forward coroutine
            testDispatcher.scheduler.advanceUntilIdle()
            
            val successState = awaitItem()
            assertTrue(successState.registroExitoso)
            assertFalse(successState.guardando)
            assertEquals("", successState.nombre) // Should reset form
            
            verify(repository).insertUser(any())
        }
    }
}
