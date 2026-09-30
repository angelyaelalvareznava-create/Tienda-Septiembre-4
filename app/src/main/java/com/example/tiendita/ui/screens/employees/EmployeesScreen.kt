package com.example.tiendita.ui.screens.employees

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.tiendita.R
import com.example.tiendita.ui.model.DirectoryItemUi
import com.example.tiendita.ui.screens.common.EntityDirectoryScreen

@Composable
fun EmployeesScreen(onBack: () -> Unit, items: List<DirectoryItemUi>) {
    EntityDirectoryScreen(stringResource(R.string.title_employees), items,
        stringResource(R.string.msg_empty_employees), stringResource(R.string.auth_show_all), onBack)
}
