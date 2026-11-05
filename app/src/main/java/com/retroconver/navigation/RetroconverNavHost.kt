package com.retroconver.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.retroconver.ui.screens.galeria.GaleriaScreen
import com.retroconver.ui.screens.mensajes.MensajesScreen
import com.retroconver.ui.screens.muro.MuroScreen
import com.retroconver.ui.screens.perfil.EditarPerfilScreen
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
        // Perfil es un grafo con dos pantallas: así la barra marca «Perfil» también al editar
        navigation(startDestination = RutasPerfil.VER, route = Destino.PERFIL.ruta) {
            composable(RutasPerfil.VER) {
                PerfilScreen(alEditar = { navController.navigate(RutasPerfil.EDITAR) { launchSingleTop = true } })
            }
            composable(RutasPerfil.EDITAR) {
                // Vuelve al perfil (y no más atrás aunque se pulse dos veces)
                EditarPerfilScreen(alTerminar = { navController.popBackStack(RutasPerfil.VER, inclusive = false) })
            }
        }
        composable(Destino.GALERIA.ruta) { GaleriaScreen() }
        composable(Destino.MENSAJES.ruta) { MensajesScreen() }
    }
}
