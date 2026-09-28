package com.example.tiendita.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorOnVariant = Color(0xFF355A57)

private val LightColors = lightColorScheme(
    primary = NexoPrimary,
    onPrimary = NexoOnPrimary,
    background = NexoBackground,
    onBackground = NexoOnSurface,
    surface = NexoSurface,
    onSurface = NexoOnSurface,
    surfaceVariant = NexoPrimaryContainer,
    onSurfaceVariant = ColorOnVariant,
    primaryContainer = NexoPrimaryContainer,
    onPrimaryContainer = NexoSurfaceDark,
    outline = NexoOutline,
    outlineVariant = Color(0xFFD5E3E1),
    secondary = NexoElectric,
    tertiary = NexoAccent
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6EE7D8),
    onPrimary = Color(0xFF003A35),
    background = Color(0xFF0B171A),
    onBackground = Color(0xFFE5F2F0),
    surface = Color(0xFF15262A),
    onSurface = Color(0xFFE5F2F0),
    surfaceVariant = Color(0xFF244044),
    onSurfaceVariant = Color(0xFFB9CFCC),
    primaryContainer = Color(0xFF15534D),
    onPrimaryContainer = Color(0xFFD4FFF5),
    outline = Color(0xFF52716E),
    outlineVariant = Color(0xFF334D4B),
    secondary = Color(0xFFB9A2FF),
    tertiary = Color(0xFFFF9D7A)
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
