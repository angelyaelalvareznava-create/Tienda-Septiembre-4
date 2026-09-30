package com.example.tiendita.ui.screens.database

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.*
import com.example.tiendita.viewmodel.*

@Composable fun DatabaseDataScreen(viewModel: DatabaseStatusViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            NexoTopBar(stringResource(R.string.db_title), onBackClick = onBack)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text(stringResource(R.string.db_version), style = MaterialTheme.typography.headlineSmall)
                    AdminButton(stringResource(R.string.db_refresh), enabled = state !is DatabaseStatusState.Loading, onClick = viewModel::refresh)
                }
                when (val current = state) {
                    DatabaseStatusState.Loading -> item { CircularProgressIndicator(); Text(stringResource(R.string.auth_loading)) }
                    DatabaseStatusState.Error -> item {
                        Text(stringResource(R.string.auth_error))
                        AdminButton(stringResource(R.string.auth_retry), onClick = viewModel::refresh)
                    }
                    is DatabaseStatusState.Data -> {
                        val counts = current.snapshot.counts
                        item { Text(stringResource(R.string.db_operational)) }
                        val rows = listOf(R.string.db_accounts to counts.accounts, R.string.db_admins to counts.administrators,
                            R.string.db_employees to counts.employees, R.string.db_clients to counts.clients,
                            R.string.db_suppliers to counts.suppliers, R.string.db_categories to counts.categories,
                            R.string.db_products to counts.products, R.string.db_warehouses to counts.warehouses,
                            R.string.db_stock to counts.stock, R.string.db_movements to counts.movements, R.string.db_events to counts.events)
                        items(rows) { (label, count) -> AdminCard {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(label)); Text(count.toString(), style = MaterialTheme.typography.titleMedium)
                            }
                        } }
                        item { Text(stringResource(R.string.db_accounts), style = MaterialTheme.typography.titleLarge) }
                        if (current.snapshot.accounts.isEmpty()) item { Text(stringResource(R.string.db_empty)) }
                        items(current.snapshot.accounts) { account -> AdminCard {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(account.username, style = MaterialTheme.typography.titleMedium)
                                account.displayName?.let { Text(it) }
                                Text(roleLabel(account.role))
                                Text(stringResource(if (account.active) R.string.db_active else R.string.db_inactive))
                                account.employeeId?.let { Text(stringResource(R.string.db_employee_link, it)) }
                            }
                        } }
                    }
                }
            }
        }
    }
}
