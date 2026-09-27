package com.example.tiendita.ui.screens.employees

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.EntityItemRow
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Composable
fun EmployeesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val employees by produceState(initialValue = emptyList<EmployeeEntity>()) {
        try {
            val database = AppDatabase.getDatabase(context)
            database.employeeDao().getActiveEmployeesFlow()
                .catch { emit(emptyList()) }
                .collect { value = it }
        } catch (e: Exception) {
            value = emptyList()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarMessage = stringResource(R.string.msg_detail_future_phase)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ScreenBackground(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NexoTopBar(title = stringResource(R.string.title_employees), onBackClick = onBack)

                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    if (employees.isEmpty()) {
                        Text(
                            text = stringResource(R.string.msg_empty_employees),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        AdminCard {
                            LazyColumn {
                                items(employees, key = { it.id }) { employee ->
                                    val fullName = "${employee.firstName} ${employee.lastName}"
                                    val initials = "${employee.firstName.firstOrNull() ?: 'E'}${employee.lastName.firstOrNull() ?: 'E'}"
                                    EntityItemRow(
                                        title = fullName,
                                        subtitle = employee.position,
                                        initials = initials,
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(snackbarMessage)
                                            }
                                        }
                                    )
                                    if (employee != employees.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
