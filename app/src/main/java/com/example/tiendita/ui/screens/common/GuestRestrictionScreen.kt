package com.example.tiendita.ui.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.*
import androidx.compose.material3.Text

@Composable fun GuestRestrictionScreen(onNavigateToLogin: () -> Unit, onBack: () -> Unit) {
    ScreenBackground {
        Column {
            NexoTopBar(stringResource(R.string.auth_restricted_title), onBackClick = onBack)
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.auth_restricted_text))
                AdminButton(stringResource(R.string.auth_login), onClick = onNavigateToLogin)
            }
        }
    }
}
