package com.example.tiendita.ui.screens.database

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground

@Composable
fun DatabaseDataScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    val clients by database.clientDao().getActiveClientsFlow().collectAsState(initial = emptyList())
    val employees by database.employeeDao().getActiveEmployeesFlow().collectAsState(initial = emptyList())
    val suppliers by database.supplierDao().getActiveSuppliersFlow().collectAsState(initial = emptyList())
    val products by database.productDao().getActiveProductsFlow().collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ScreenBackground(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NexoTopBar(title = stringResource(R.string.title_database_inspection), onBackClick = onBack)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AdminCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📊 Clientes en Base de Datos (${clients.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                if (clients.isEmpty()) {
                                    Text("No hay clientes registrados.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    clients.forEach { client ->
                                        Text("• ${client.name} | Tel: ${client.phone ?: "N/A"} | Email: ${client.email ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        AdminCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "👥 Empleados en Base de Datos (${employees.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                if (employees.isEmpty()) {
                                    Text("No hay empleados registrados.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    employees.forEach { emp ->
                                        Text("• ${emp.firstName} ${emp.lastName} (${emp.position}) | Tel: ${emp.phone ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        AdminCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📦 Proveedores en Base de Datos (${suppliers.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                if (suppliers.isEmpty()) {
                                    Text("No hay proveedores registrados.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    suppliers.forEach { sup ->
                                        Text("• ${sup.companyName} | Contrato: ${sup.contactName ?: "N/A"} | Tel: ${sup.phone ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        AdminCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🏷️ Inventario / Productos (${products.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                if (products.isEmpty()) {
                                    Text("No hay productos en inventario.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    products.forEach { prod ->
                                        Text("• ${prod.name} (SKU: ${prod.sku}) | Venta: \$${(prod.salePriceCents ?: 0) / 100.0}", style = MaterialTheme.typography.bodySmall)
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
