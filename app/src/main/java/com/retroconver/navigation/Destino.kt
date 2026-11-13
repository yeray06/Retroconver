package com.retroconver.navigation

import androidx.annotation.StringRes
import com.retroconver.R

/**
 * Secciones de la barra de navegación inferior, en el orden en que aparecen.
 * Es el único sitio donde se definen las rutas de la app.
 *
 * El icono es un emoji, como en los bocetos de docs/diseño/.
 */
enum class Destino(
    val ruta: String,
    @param:StringRes val etiqueta: Int,
    val icono: String,
) {
    MURO("muro", R.string.seccion_muro, "🏠"),
    PERFIL("perfil", R.string.seccion_perfil, "👤"),
    GALERIA("galeria", R.string.seccion_galeria, "🖼️"),
    MENSAJES("mensajes", R.string.seccion_mensajes, "💬");

    companion object {
        /** Sección que se abre al arrancar la app (ver docs/navegacion.md). */
        val inicio = MURO
    }
}

/** Pantallas dentro de la sección Perfil. La ruta de la sección ([Destino.PERFIL]) abre [VER]. */
object RutasPerfil {
    const val VER = "perfil/ver"
    const val EDITAR = "perfil/editar"
}

/** Pantallas dentro de la sección Muro. La ruta de la sección ([Destino.MURO]) abre [VER]. */
object RutasMuro {
    const val VER = "muro/ver"
    const val NUEVA = "muro/nueva"
}

/** Pantallas de acceso, que se ven cuando no hay sesión (E4-04.2). Empieza en [INICIO]. */
object RutasAcceso {
    const val INICIO = "acceso/inicio"
    const val REGISTRO = "acceso/registro"
}

/**
 * Pantallas dentro de la sección Galería (E4-10). La ruta de la sección ([Destino.GALERIA])
 * abre [MIS_ALBUMES]. Para abrir los álbumes de otra persona desde su perfil (E4-08):
 * `navController.navigate(RutasGaleria.albumesDe(idDeLaPersona))`.
 */
object RutasGaleria {
    const val ARG_PROPIETARIO = "propietarioId"
    const val ARG_ALBUM = "albumId"
    const val ARG_FOTO = "fotoId"

    const val MIS_ALBUMES = "galeria/mis-albumes"
    const val ALBUMES_DE = "galeria/persona/{$ARG_PROPIETARIO}"
    const val ALBUM = "galeria/album/{$ARG_ALBUM}"
    const val FOTO = "galeria/foto/{$ARG_FOTO}"

    fun albumesDe(propietarioId: String) = "galeria/persona/$propietarioId"

    fun album(albumId: String) = "galeria/album/$albumId"

    fun foto(fotoId: String) = "galeria/foto/$fotoId"
}
