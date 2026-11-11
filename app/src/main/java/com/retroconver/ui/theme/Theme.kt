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
    // Diálogos y el indicador de «deslizar para actualizar»: blancos como las tarjetas,
    // en vez del lila que trae Material 3 por defecto
    surfaceContainerHigh = RetroCardBackground,
    outline = RetroCardBorder,
    // Contraste de 4,5:1 o más también sobre el fondo azul claro (E4-12.2, #78)
    error = RetroErrorRed,
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
