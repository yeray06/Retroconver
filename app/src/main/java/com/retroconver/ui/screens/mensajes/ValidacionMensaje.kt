package com.retroconver.ui.screens.mensajes

import com.retroconver.ui.screens.muro.ErrorPublicacion

/** Límite de longitud de un mensaje del tablón (el mismo que fija el servidor en `mensajes`). */
object LimitesMensaje {
    const val TEXTO_MAX = 300
}

/**
 * Comprueba el texto de un mensaje del tablón (E4-09.3, #74), igual que el de un comentario:
 * no puede estar vacío ni ser solo espacios, ni pasar del límite sin contar los espacios de los
 * bordes. `null` significa que está bien.
 */
fun validarMensaje(texto: String): ErrorPublicacion? = when {
    texto.isBlank() -> ErrorPublicacion.VACIO
    texto.trim().length > LimitesMensaje.TEXTO_MAX -> ErrorPublicacion.DEMASIADO_LARGO
    else -> null
}
