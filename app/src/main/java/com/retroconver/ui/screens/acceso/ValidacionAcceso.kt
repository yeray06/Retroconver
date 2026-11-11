package com.retroconver.ui.screens.acceso

import com.retroconver.ui.screens.perfil.LimitesPerfil

/** Límites de los formularios de acceso (E4-04.2, #118). */
object LimitesAcceso {
    /** Mínimo de Supabase Auth: con menos, el servidor rechaza la contraseña. */
    const val CONTRASENA_MIN = 6

    /** El mismo que en el perfil y en la tabla `perfiles` del servidor. */
    const val NOMBRE_MAX = LimitesPerfil.NOMBRE_MAX
}

/** Qué le pasa a un campo de los formularios de acceso. */
enum class ErrorAcceso { OBLIGATORIO, EMAIL_NO_VALIDO, CONTRASENA_CORTA, DEMASIADO_LARGO }

/** Error de cada campo; `null` si está bien. */
data class ErroresAcceso(
    val nombre: ErrorAcceso? = null,
    val email: ErrorAcceso? = null,
    val contrasena: ErrorAcceso? = null,
) {
    val hayErrores: Boolean get() = nombre != null || email != null || contrasena != null
}

/**
 * Formato de email sencillo: algo, una arroba, un dominio con punto y al menos dos letras al final.
 * No intenta cubrir todos los casos raros: el servidor hace la comprobación definitiva.
 */
private val FORMATO_EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s.]{2,}$")

fun esEmailValido(email: String): Boolean = FORMATO_EMAIL.matches(email.trim())

private fun validarEmail(email: String): ErrorAcceso? = when {
    email.isBlank() -> ErrorAcceso.OBLIGATORIO
    !esEmailValido(email) -> ErrorAcceso.EMAIL_NO_VALIDO
    else -> null
}

/** Inicio de sesión: email con formato correcto y contraseña escrita. */
fun validarInicioSesion(email: String, contrasena: String): ErroresAcceso = ErroresAcceso(
    email = validarEmail(email),
    contrasena = if (contrasena.isEmpty()) ErrorAcceso.OBLIGATORIO else null,
)

/**
 * Registro: nombre obligatorio y de hasta [LimitesAcceso.NOMBRE_MAX] caracteres, email con
 * formato correcto y contraseña de al menos [LimitesAcceso.CONTRASENA_MIN] caracteres.
 * La contraseña no se recorta: los espacios cuentan, como en el servidor.
 */
fun validarRegistro(nombre: String, email: String, contrasena: String): ErroresAcceso = ErroresAcceso(
    nombre = when {
        nombre.isBlank() -> ErrorAcceso.OBLIGATORIO
        nombre.trim().length > LimitesAcceso.NOMBRE_MAX -> ErrorAcceso.DEMASIADO_LARGO
        else -> null
    },
    email = validarEmail(email),
    contrasena = when {
        contrasena.isEmpty() -> ErrorAcceso.OBLIGATORIO
        contrasena.length < LimitesAcceso.CONTRASENA_MIN -> ErrorAcceso.CONTRASENA_CORTA
        else -> null
    },
)
