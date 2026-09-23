package com.example.tiendita.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun NexoStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
