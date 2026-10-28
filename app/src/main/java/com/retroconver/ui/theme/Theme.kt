package com.retroconver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Tema de toda la app. Todas las pantallas se dibujan dentro de él.
 *
 * De momento usa los colores y la tipografía por defecto de Material 3.
 * E2-06.1 (#46): David define aquí la paleta, las tipografías y las formas de docs/estilo.md.
 */
@Composable
fun RetroconverTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content,
    )
}
