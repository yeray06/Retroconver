package com.retroconver.ui.screens.perfil

/** Límites de longitud de los campos del perfil (RF-06). */
object LimitesPerfil {
    const val NOMBRE_MAX = 30
    const val BIOGRAFIA_MAX = 200
    const val ESTADO_MAX = 80
}

/** Qué le pasa a un campo del formulario. */
enum class ErrorCampo { OBLIGATORIO, DEMASIADO_LARGO }

/** Error de cada campo del formulario de perfil; `null` si el campo está bien. */
data class ErroresPerfil(
    val nombre: ErrorCampo? = null,
    val biografia: ErrorCampo? = null,
    val estado: ErrorCampo? = null,
) {
    val hayErrores: Boolean get() = nombre != null || biografia != null || estado != null
}

/**
 * Comprueba el formulario de perfil (E3-06.2, #54): el nombre es obligatorio y ningún campo
 * puede pasar de su límite. Se valida el texto sin espacios al principio ni al final,
 * que es como se guarda.
 */
fun validarPerfil(nombre: String, biografia: String, estado: String): ErroresPerfil = ErroresPerfil(
    nombre = when {
        nombre.isBlank() -> ErrorCampo.OBLIGATORIO
        nombre.trim().length > LimitesPerfil.NOMBRE_MAX -> ErrorCampo.DEMASIADO_LARGO
        else -> null
    },
    biografia = if (biografia.trim().length > LimitesPerfil.BIOGRAFIA_MAX) ErrorCampo.DEMASIADO_LARGO else null,
    estado = if (estado.trim().length > LimitesPerfil.ESTADO_MAX) ErrorCampo.DEMASIADO_LARGO else null,
)
