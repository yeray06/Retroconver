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
