package com.example.tiendita.ui.screens.clients

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
fun ClientsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val clients by database.clientDao().getActiveClientsFlow().collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarMessage = stringResource(R.string.msg_detail_future_phase)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ScreenBackground(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NexoTopBar(title = stringResource(R.string.title_clients), onBackClick = onBack)

                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    if (clients.isEmpty()) {
                        Text(
                            text = stringResource(R.string.msg_empty_clients),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        AdminCard {
                            LazyColumn {
                                items(clients, key = { it.id }) { client ->
                                    val initials = client.name.split(" ")
                                        .let { if (it.size > 1) "${it[0].first()}${it[1].first()}" else client.name.take(2).uppercase() }
                                    EntityItemRow(
                                        title = client.name,
                                        subtitle = client.address ?: client.notes ?: "Cliente",
                                        initials = initials,
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(snackbarMessage)
                                            }
                                        }
                                    )
                                    if (client != clients.last()) {
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
