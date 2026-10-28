package com.retroconver.ui.screens.galeria

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.retroconver.R
import com.retroconver.ui.screens.PantallaProvisional

/**
 * Galería de álbumes.
 * Provisional: Yeray la sustituye en E4-02 (#21).
 */
@Composable
fun GaleriaScreen() {
    PantallaProvisional(titulo = stringResource(R.string.seccion_galeria))
}
