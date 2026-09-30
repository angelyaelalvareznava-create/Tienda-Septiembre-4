package com.example.tiendita.ui.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.*

@Composable fun PublicStatusScreen(title: String, loading: Boolean = false, message: String? = null,
    onBack: (() -> Unit)? = null, onRetry: (() -> Unit)? = null) {
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            NexoTopBar(title, onBackClick = onBack, showUserStatus = !loading)
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (loading) { CircularProgressIndicator(); Text(stringResource(R.string.auth_loading)) }
                message?.let { Text(it) }
                onRetry?.let { AdminButton(stringResource(R.string.auth_retry), onClick = it) }
            }
        }
    }
}
