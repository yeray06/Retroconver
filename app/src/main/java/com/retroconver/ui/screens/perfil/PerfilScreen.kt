package com.retroconver.ui.screens.perfil

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.retroconver.R
import com.retroconver.ui.screens.PantallaProvisional

/**
 * Perfil de la persona usuaria.
 * Provisional: Yeray la sustituye en E3-06 (#14).
 */
@Composable
fun PerfilScreen() {
    PantallaProvisional(titulo = stringResource(R.string.seccion_perfil))
}
