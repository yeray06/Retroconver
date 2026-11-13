package com.retroconver.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.retroconver.data.sesion.EstadoSesion
import com.retroconver.ui.screens.acceso.InicioSesionScreen
import com.retroconver.ui.screens.acceso.RegistroScreen

/**
 * Lo primero que decide la app (E4-04.2, #118): sin sesión, las pantallas de acceso;
 * con sesión, la app con la barra de navegación.
 *
 * No hace falta navegar al entrar o al salir: al cambiar la sesión en DataStore cambia
 * [estadoSesion] y se muestra la otra parte. Así, al cerrar sesión no se puede volver
 * atrás al muro de la persona anterior.
 */
@Composable
fun RetroconverRaiz(estadoSesion: EstadoSesion, hayConexion: Boolean) {
    when (estadoSesion) {
        // Solo un instante, mientras se lee DataStore: el fondo, sin parpadeo del inicio de sesión
        EstadoSesion.Cargando -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )
        EstadoSesion.SinSesion -> AccesoNavHost(hayConexion)
        // Con otra cuenta se empieza de cero: pila de navegación y ViewModels nuevos
        is EstadoSesion.Iniciada -> key(estadoSesion.idCuenta) {
            PantallaPrincipal(hayConexion = hayConexion)
        }
    }
}

/** Inicio de sesión y registro. */
@Composable
private fun AccesoNavHost(hayConexion: Boolean) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = RutasAcceso.INICIO) {
        composable(RutasAcceso.INICIO) {
            InicioSesionScreen(
                hayConexion = hayConexion,
                alIrARegistro = { navController.navigate(RutasAcceso.REGISTRO) { launchSingleTop = true } },
            )
        }
        composable(RutasAcceso.REGISTRO) {
            RegistroScreen(
                hayConexion = hayConexion,
                // Vuelve al inicio de sesión (y no más atrás aunque se pulse dos veces)
                alIrAEntrar = {
                    if (!navController.popBackStack(RutasAcceso.INICIO, inclusive = false)) {
                        navController.navigate(RutasAcceso.INICIO)
                    }
                },
            )
        }
    }
}
