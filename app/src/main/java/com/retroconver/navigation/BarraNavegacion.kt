package com.retroconver.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Barra inferior con las cuatro secciones. Marca la sección actual.
 *
 * Al cambiar de sección se guarda el estado de la anterior y, al volver, se recupera.
 * El botón atrás vuelve primero al Muro y después sale de la app, como recomienda Android.
 */
@Composable
fun BarraNavegacion(navController: NavHostController) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    NavigationBar {
        Destino.entries.forEach { destino ->
            val seleccionado = destinoActual?.hierarchy?.any { it.route == destino.ruta } == true
            NavigationBarItem(
                selected = seleccionado,
                onClick = {
                    navController.navigate(destino.ruta) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // El emoji es decorativo: TalkBack lee solo la etiqueta
                icon = { Text(text = destino.icono, modifier = Modifier.clearAndSetSemantics { }) },
                label = { Text(stringResource(destino.etiqueta)) },
            )
        }
    }
}
