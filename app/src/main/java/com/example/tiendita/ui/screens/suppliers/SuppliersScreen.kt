package com.example.tiendita.ui.screens.suppliers

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.EntityItemRow
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import kotlinx.coroutines.launch

@Composable
fun SuppliersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val suppliers by database.supplierDao().getActiveSuppliersFlow().collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarMessage = stringResource(R.string.msg_detail_future_phase)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ScreenBackground(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NexoTopBar(title = stringResource(R.string.title_suppliers), onBackClick = onBack)

                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    if (suppliers.isEmpty()) {
                        Text(
                            text = stringResource(R.string.msg_empty_suppliers),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        AdminCard {
                            LazyColumn {
                                items(suppliers, key = { it.id }) { supplier ->
                                    val initials = supplier.companyName.split(" ")
                                        .let { if (it.size > 1) "${it[0].first()}${it[1].first()}" else supplier.companyName.take(2).uppercase() }
                                    EntityItemRow(
                                        title = supplier.companyName,
                                        subtitle = supplier.address ?: supplier.email ?: "Sin dirección",
                                        initials = initials,
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(snackbarMessage)
                                            }
                                        }
                                    )
                                    if (supplier != suppliers.last()) {
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
