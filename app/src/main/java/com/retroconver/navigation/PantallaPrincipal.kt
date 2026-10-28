package com.retroconver.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

/** Estructura común de la app: la sección actual arriba y la barra de navegación abajo. */
@Composable
fun PantallaPrincipal() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BarraNavegacion(navController) },
    ) { espacioInterior ->
        RetroconverNavHost(
            navController = navController,
            modifier = Modifier.padding(espacioInterior),
        )
    }
}
