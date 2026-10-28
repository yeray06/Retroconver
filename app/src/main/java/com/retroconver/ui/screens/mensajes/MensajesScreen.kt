package com.retroconver.ui.screens.mensajes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.retroconver.R
import com.retroconver.ui.screens.PantallaProvisional

/**
 * Mensajes.
 * Provisional: David la sustituye en E4-03 (#22).
 */
@Composable
fun MensajesScreen() {
    PantallaProvisional(titulo = stringResource(R.string.seccion_mensajes))
}
