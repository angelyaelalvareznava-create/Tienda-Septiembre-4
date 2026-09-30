package com.example.tiendita.ui.screens.suppliers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.tiendita.R
import com.example.tiendita.ui.model.DirectoryItemUi
import com.example.tiendita.ui.screens.common.EntityDirectoryScreen

@Composable
fun SuppliersScreen(onBack: () -> Unit, items: List<DirectoryItemUi>) {
    EntityDirectoryScreen(stringResource(R.string.title_suppliers), items,
        stringResource(R.string.msg_empty_suppliers), stringResource(R.string.auth_show_all), onBack)
}
