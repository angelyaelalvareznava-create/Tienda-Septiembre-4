package com.example.tiendita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.session.SessionState

@Composable
fun NexoTopBar(title: String, onBackClick: (() -> Unit)? = null, onLoginClick: (() -> Unit)? = null,
    showUserStatus: Boolean = true, applyStatusBarInsets: Boolean = true) {
    val insets = if (applyStatusBarInsets) Modifier.windowInsetsPadding(WindowInsets.statusBars) else Modifier
    Surface(modifier = Modifier.fillMaxWidth().then(insets),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        color = MaterialTheme.colorScheme.primary, shadowElevation = 10.dp) {
        Column(Modifier.background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary))).padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBackClick != null) Text("‹", modifier = Modifier.clickable(onClick = onBackClick).padding(end = 14.dp),
                    color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
            }
            if (showUserStatus) {
                val account = (LocalSessionState.current as? SessionState.Authenticated)?.account
                val name = account?.displayName?.takeIf { it.isNotBlank() } ?: account?.username
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .92f)) {
                        Column(Modifier.widthIn(max = 260.dp).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(name ?: stringResource(R.string.auth_guest_status), maxLines = 1,
                                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                            if (account != null) Text(roleLabel(account.role), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                            else if (onLoginClick != null) Text(stringResource(R.string.auth_login),
                                modifier = Modifier.clickable(onClick = onLoginClick), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
