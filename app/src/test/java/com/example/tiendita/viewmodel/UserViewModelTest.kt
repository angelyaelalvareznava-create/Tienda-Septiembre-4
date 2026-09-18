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
            assertEquals("", initialState.nombre)
            assertEquals("", initialState.apellidos)
            assertEquals("", initialState.direccion)
            assertEquals("", initialState.telefono)
            assertFalse(initialState.isFormValid)
        }
    }

    @Test
    fun `nombre validation triggers error on empty`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(UserFormEvent.OnNombreChanged(""))
            val state = awaitItem()
            assertEquals("El nombre es obligatorio", state.errorNombre)
            assertFalse(state.isFormValid)
        }
    }

    @Test
    fun `telefono validation requires exactly 10 digits`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            // Test 9 digits
            viewModel.onEvent(UserFormEvent.OnTelefonoChanged("123456789"))
            val state9 = awaitItem()
            assertEquals("El teléfono debe tener 10 dígitos", state9.errorTelefono)
            assertFalse(state9.isFormValid)

            // Test 10 digits
            viewModel.onEvent(UserFormEvent.OnTelefonoChanged("1234567890"))
            val state10 = awaitItem()
            assertNull(state10.errorTelefono)
        }
    }

    @Test
    fun `form becomes valid when all fields are correct`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(UserFormEvent.OnNombreChanged("Juan"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnApellidosChanged("Perez"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnDireccionChanged("Calle Falsa 123"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnTelefonoChanged("1234567890"))
            
            val finalState = awaitItem()
            assertTrue(finalState.isFormValid)
        }
    }

    @Test
    fun `onSubmit with valid data calls repository and updates state`() = runTest {
        whenever(repository.insertUser(any())).thenReturn(1L)
        
        viewModel.state.test {
            awaitItem() // initial state
            
            // Set valid data
            viewModel.onEvent(UserFormEvent.OnNombreChanged("Juan"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnApellidosChanged("Perez"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnDireccionChanged("Calle Falsa 123"))
            awaitItem()
            viewModel.onEvent(UserFormEvent.OnTelefonoChanged("1234567890"))
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
