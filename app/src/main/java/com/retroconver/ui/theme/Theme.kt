package com.retroconver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroColorScheme = lightColorScheme(
    primary = RetroGlossyDark,
    onPrimary = Color.White,
    secondary = RetroGlossyTop,
    onSecondary = Color.White,
    // Indicador de la sección seleccionada en la barra inferior
    secondaryContainer = RetroProfileCardBg,
    onSecondaryContainer = RetroGlossyDark,
    background = RetroBackground,
    onBackground = RetroTextDark,
    surface = RetroCardBackground,
    onSurface = RetroTextDark,
    surfaceVariant = RetroProfileCardBg,
    onSurfaceVariant = RetroGrayText,
    // Fondo de la barra de navegación inferior
    surfaceContainer = RetroCardBackground,
    outline = RetroCardBorder,
)

/**
 * Tema retro de toda la app: colores, tipografía y formas de docs/estilo.md.
 * Todas las pantallas se dibujan dentro de él (ver MainActivity).
 */
@Composable
fun RetroconverTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        shapes = RetroShapes,
        content = content,
    )
}
