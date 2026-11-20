package com.retroconver.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.retroconver.ui.screens.galeria.AlbumScreen
import com.retroconver.ui.screens.galeria.AlbumesScreen
import com.retroconver.ui.screens.galeria.FotoScreen
import com.retroconver.ui.screens.mensajes.MensajesScreen
import com.retroconver.ui.screens.muro.MuroScreen
import com.retroconver.ui.screens.muro.NuevaPublicacionScreen
import com.retroconver.ui.screens.muro.PublicacionScreen
import com.retroconver.ui.screens.perfil.EditarPerfilScreen
import com.retroconver.ui.screens.perfil.PerfilScreen
import com.retroconver.ui.screens.personas.PerfilDePersonaScreen
import com.retroconver.ui.screens.personas.PersonasScreen

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
        // Perfil de otra persona (E4-08.1): desde el muro, una publicación, la lista de amigos o «Personas»
        val abrirPerfil: (String) -> Unit = { id -> navController.navigate(RutasPersonas.perfil(id)) }
        // Muro es un grafo: así la barra marca «Muro» también al publicar o abrir una publicación (E4-07)
        navigation(startDestination = RutasMuro.VER, route = Destino.MURO.ruta) {
            composable(RutasMuro.VER) {
                MuroScreen(
                    alAbrirPublicacion = { id -> navController.navigate(RutasMuro.publicacion(id)) },
                    alAbrirPerfil = abrirPerfil,
                )
            }
            composable(
                RutasMuro.PUBLICACION,
                arguments = listOf(navArgument(RutasMuro.ARG_PUBLICACION) { type = NavType.StringType }),
            ) { PublicacionScreen(alVolver = { navController.popBackStack() }, alAbrirPerfil = abrirPerfil) }
            composable(RutasMuro.NUEVA) {
                // Publicar o cancelar vuelve al muro (y no más atrás aunque se pulse dos veces)
                NuevaPublicacionScreen(alTerminar = { navController.popBackStack(RutasMuro.VER, inclusive = false) })
            }
        }
        // Perfil es un grafo con dos pantallas: así la barra marca «Perfil» también al editar
        navigation(startDestination = RutasPerfil.VER, route = Destino.PERFIL.ruta) {
            composable(RutasPerfil.VER) {
                PerfilScreen(
                    alEditar = { navController.navigate(RutasPerfil.EDITAR) { launchSingleTop = true } },
                    alAbrirPerfil = abrirPerfil,
                )
            }
            composable(RutasPerfil.EDITAR) {
                // Vuelve al perfil (y no más atrás aunque se pulse dos veces)
                EditarPerfilScreen(alTerminar = { navController.popBackStack(RutasPerfil.VER, inclusive = false) })
            }
        }
        // Galería es un grafo: lista de álbumes, detalle en cuadrícula y foto a pantalla completa (E4-10)
        navigation(startDestination = RutasGaleria.MIS_ALBUMES, route = Destino.GALERIA.ruta) {
            val abrirAlbum: (String) -> Unit = { id -> navController.navigate(RutasGaleria.album(id)) }
            composable(RutasGaleria.MIS_ALBUMES) { AlbumesScreen(alAbrirAlbum = abrirAlbum) }
            composable(
                RutasGaleria.ALBUMES_DE,
                arguments = listOf(navArgument(RutasGaleria.ARG_PROPIETARIO) { type = NavType.StringType }),
            ) { AlbumesScreen(alAbrirAlbum = abrirAlbum) }
            composable(
                RutasGaleria.ALBUM,
                arguments = listOf(navArgument(RutasGaleria.ARG_ALBUM) { type = NavType.StringType }),
            ) {
                AlbumScreen(
                    alVolver = { navController.popBackStack() },
                    alAbrirFoto = { id -> navController.navigate(RutasGaleria.foto(id)) },
                )
            }
            composable(
                RutasGaleria.FOTO,
                arguments = listOf(navArgument(RutasGaleria.ARG_FOTO) { type = NavType.StringType }),
            ) { FotoScreen(alVolver = { navController.popBackStack() }) }
        }
        composable(Destino.MENSAJES.ruta) { MensajesScreen() }
        // Personas y perfiles ajenos (E4-08): fuera de las secciones de la barra
        composable(RutasPersonas.LISTA) {
            PersonasScreen(alVolver = { navController.popBackStack() }, alAbrirPerfil = abrirPerfil)
        }
        composable(
            RutasPersonas.PERFIL,
            arguments = listOf(navArgument(RutasPersonas.ARG_PERSONA) { type = NavType.StringType }),
        ) {
            PerfilDePersonaScreen(
                alVolver = { navController.popBackStack() },
                alAbrirPerfil = abrirPerfil,
                alVerAlbumes = { id -> navController.navigate(RutasGaleria.albumesDe(id)) },
                // Si es el propio, se cambia por la sección Perfil, con sus botones de editar
                alEsMiPerfil = {
                    navController.navigate(Destino.PERFIL.ruta) {
                        popUpTo(RutasPersonas.PERFIL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
