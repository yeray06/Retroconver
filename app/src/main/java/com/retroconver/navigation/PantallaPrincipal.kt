package com.retroconver.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.retroconver.R
import com.retroconver.ui.components.AvisoSinConexion
import com.retroconver.ui.components.RetroHeader
import com.retroconver.ui.components.RetroHeaderBoton

/**
 * Estructura común de la app: cabecera retro arriba, la sección actual y la barra de navegación abajo.
 * En el muro, la cabecera lleva el botón «+» que abre el formulario de nueva publicación.
 *
 * @param hayConexion Si es `false`, se muestra la franja de «Sin conexión» bajo la cabecera (RNF-06).
 */
@Composable
fun PantallaPrincipal(hayConexion: Boolean = true) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val enMuro = entradaActual?.destination?.route == RutasMuro.VER
    Scaffold(
        topBar = {
            RetroHeader(
                titulo = stringResource(R.string.logo_cabecera),
                accion = {
                    if (enMuro) {
                        RetroHeaderBoton(
                            texto = stringResource(R.string.muro_boton_mas),
                            descripcion = stringResource(R.string.muro_nueva_publicacion),
                            onClick = { navController.navigate(RutasMuro.NUEVA) { launchSingleTop = true } },
                        )
                    }
                },
            )
        },
        bottomBar = { BarraNavegacion(navController) },
    ) { espacioInterior ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(espacioInterior),
        ) {
            if (!hayConexion) AvisoSinConexion()
            RetroconverNavHost(
                navController = navController,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
