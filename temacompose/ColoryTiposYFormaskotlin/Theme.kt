package com.retroconver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RetroColorScheme = lightColorScheme(
    primary = RetroGlossyDark,
    secondary = RetroGlossyTop,
    background = RetroBackground,
    surface = RetroCardBackground,
    onPrimary = Color.White,
    onBackground = RetroTextDark,
    onSurface = RetroTextDark
)

@Composable
fun RetroconverTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        shapes = RetroShapes,
        content = content
    )
}