package com.example.tiendita.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.tiendita.utils.SessionManager

private val LightColors = lightColorScheme(
    primary = NexoPrimary,
    onPrimary = NexoOnPrimary,
    background = NexoBackground,
    surface = NexoSurface,
    onSurface = NexoOnSurface,
    primaryContainer = NexoPrimaryContainer,
    onPrimaryContainer = NexoSurfaceDark,
    outline = NexoOutline,
    outlineVariant = NexoOutline
)

private val DarkColors = darkColorScheme(
    primary = NexoPrimaryContainer,
    onPrimary = NexoSurfaceDark,
    background = NexoSurfaceDark,
    surface = NexoSurface,
    onSurface = NexoOnSurface,
    primaryContainer = NexoPrimary,
    onPrimaryContainer = NexoOnPrimary,
    outline = NexoOutline,
    outlineVariant = NexoOutline
)

private val AdminLightColors = lightColorScheme(
    primary = Color(0xFF6A1B9A), // Deep Royal Purple for Admin
    onPrimary = Color.White,
    background = Color(0xFFFAF2FB),
    surface = Color.White,
    onSurface = Color(0xFF2E1A47),
    primaryContainer = Color(0xFFE1BEE7),
    onPrimaryContainer = Color(0xFF4A148C),
    outline = Color(0xFFBA68C8),
    outlineVariant = Color(0xFFCE93D8)
)

@Composable
fun NexoStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (SessionManager.isAdmin) {
        AdminLightColors
    } else {
        if (darkTheme) DarkColors else LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
