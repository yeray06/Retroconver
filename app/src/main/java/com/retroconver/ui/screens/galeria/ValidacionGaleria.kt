package com.retroconver.ui.screens.galeria

import com.retroconver.ui.screens.perfil.ErrorCampo

/** Límites de la galería: los mismos que comprueba la tabla `albumes` del servidor. */
object LimitesGaleria {
    const val TITULO_MAX = 40

    /** Fotos que se pueden elegir de una vez en el selector. */
    const val FOTOS_POR_SUBIDA = 10
}

/**
 * Comprueba el nombre de un álbum nuevo (E4-10.2, #69): obligatorio y de hasta
 * [LimitesGaleria.TITULO_MAX] caracteres, sin contar los espacios de los bordes.
 */
fun validarTituloAlbum(titulo: String): ErrorCampo? = when {
    titulo.isBlank() -> ErrorCampo.OBLIGATORIO
    titulo.trim().length > LimitesGaleria.TITULO_MAX -> ErrorCampo.DEMASIADO_LARGO
    else -> null
}
