package com.example.tiendita.ui.screens.common

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.EntityItemRow
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.model.DirectoryItemUi
import kotlinx.coroutines.launch

@Composable
fun EntityDirectoryScreen(
    title: String,
    items: List<DirectoryItemUi>,
    emptyMessage: String,
    loadMoreLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarMessage = stringResource(R.string.msg_detail_future_phase)

    val visibleItems = if (showAll) items else items.take(5)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ScreenBackground(modifier = modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NexoTopBar(title = title, onBackClick = onBack)

                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    if (items.isEmpty()) {
                        Text(
                            text = emptyMessage,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        AdminCard {
                            LazyColumn {
                                items(visibleItems, key = { it.id }) { item ->
                                    EntityItemRow(
                                        title = item.title,
                                        subtitle = item.subtitle,
                                        initials = item.initials,
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(snackbarMessage)
                                            }
                                        }
                                    )
                                    if (item != visibleItems.last() || (!showAll && items.size > 5)) {
                                        HorizontalDivider()
                                    }
                                }

                                if (!showAll && items.size > 5) {
                                    item {
                                        Box(modifier = Modifier.padding(16.dp)) {
                                            AdminButton(
                                                text = loadMoreLabel,
                                                onClick = { showAll = true }
                                            )
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
}
