package com.retroconver.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.retroconver.ui.screens.galeria.GaleriaScreen
import com.retroconver.ui.screens.mensajes.MensajesScreen
import com.retroconver.ui.screens.muro.MuroScreen
import com.retroconver.ui.screens.perfil.PerfilScreen

/** Grafo de navegación: qué pantalla se muestra en cada ruta. */
@Composable
fun RetroconverNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destino.inicio.ruta,
        modifier = modifier,
    ) {
        composable(Destino.MURO.ruta) { MuroScreen() }
        composable(Destino.PERFIL.ruta) { PerfilScreen() }
        composable(Destino.GALERIA.ruta) { GaleriaScreen() }
        composable(Destino.MENSAJES.ruta) { MensajesScreen() }
    }
}
