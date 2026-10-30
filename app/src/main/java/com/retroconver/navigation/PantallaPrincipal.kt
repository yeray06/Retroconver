package com.retroconver.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.retroconver.R
import com.retroconver.ui.components.RetroHeader

/** Estructura común de la app: cabecera retro arriba, la sección actual y la barra de navegación abajo. */
@Composable
fun PantallaPrincipal() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { RetroHeader(titulo = stringResource(R.string.logo_cabecera)) },
        bottomBar = { BarraNavegacion(navController) },
    ) { espacioInterior ->
        RetroconverNavHost(
            navController = navController,
            modifier = Modifier.padding(espacioInterior),
        )
    }
}
