package com.retroconver.ui.screens.muro

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.retroconver.R
import com.retroconver.ui.screens.PantallaProvisional

/**
 * Muro con las publicaciones.
 * Provisional: David la sustituye en E3-03 (#16).
 */
@Composable
fun MuroScreen() {
    PantallaProvisional(titulo = stringResource(R.string.seccion_muro))
}
