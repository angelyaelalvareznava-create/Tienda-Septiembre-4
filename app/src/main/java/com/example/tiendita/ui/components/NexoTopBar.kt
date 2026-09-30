package com.example.tiendita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.utils.SessionManager

@Composable
fun NexoTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onLoginClick: (() -> Unit)? = null,
    showUserStatus: Boolean = true,
    applyStatusBarInsets: Boolean = true
) {
    val baseModifier = Modifier.fillMaxWidth()
    val modifierWithInsets = if (applyStatusBarInsets) {
        baseModifier.windowInsetsPadding(WindowInsets.statusBars)
    } else {
        baseModifier
    }

    Surface(
        modifier = modifierWithInsets,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBackClick != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                        modifier = Modifier.clickable(onClick = onBackClick)
                    ) {
                        Text(
                            text = "‹",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = if (onBackClick != null) 10.dp else 4.dp)
                        .weight(1f, fill = false)
                )
            }

            if (showUserStatus) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (SessionManager.isLoggedIn) {
                        Text(
                            text = SessionManager.loggedInUsername,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 120.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else if (onLoginClick != null) {
                        Text(
                            text = stringResource(R.string.login_prompt),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable(onClick = onLoginClick)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.guest_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
