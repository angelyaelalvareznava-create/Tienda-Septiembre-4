package com.example.tiendita.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme

@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMovements: () -> Unit,
    onLogout: () -> Unit
) {
    ScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoTopBar(title = stringResource(R.string.title_home))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AdminButton(text = stringResource(R.string.menu_calendar), onClick = onNavigateToCalendar)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_suppliers), onClick = onNavigateToSuppliers)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_employees), onClick = onNavigateToEmployees)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_clients), onClick = onNavigateToClients)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_register_user), onClick = onNavigateToRegistration)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_edit_profile), onClick = onNavigateToEditProfile)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_inventory), onClick = onNavigateToInventory)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_movements), onClick = onNavigateToMovements)
                }
                item {
                    AdminButton(text = stringResource(R.string.menu_logout), onClick = onLogout)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NexoStockTheme {
        HomeScreen(
            onNavigateToCalendar = {},
            onNavigateToSuppliers = {},
            onNavigateToEmployees = {},
            onNavigateToClients = {},
            onNavigateToRegistration = {},
            onNavigateToEditProfile = {},
            onNavigateToInventory = {},
            onNavigateToMovements = {},
            onLogout = {}
        )
    }
}
