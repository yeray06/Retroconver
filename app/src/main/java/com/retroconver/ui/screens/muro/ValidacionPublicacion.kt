package com.retroconver.ui.screens.muro

/** Límite de longitud del texto de una publicación. */
object LimitesPublicacion {
    const val TEXTO_MAX = 500
}

/** Qué le pasa al texto de la publicación. */
enum class ErrorPublicacion { VACIO, DEMASIADO_LARGO }

/**
 * Comprueba el texto de una publicación (RF-11, #60): no puede estar vacío ni ser solo
 * espacios, ni pasar del límite. Se mide sin los espacios del principio y del final,
 * que es como se guarda; `null` significa que está bien.
 */
fun validarPublicacion(texto: String): ErrorPublicacion? = when {
    texto.isBlank() -> ErrorPublicacion.VACIO
    texto.trim().length > LimitesPublicacion.TEXTO_MAX -> ErrorPublicacion.DEMASIADO_LARGO
    else -> null
}
