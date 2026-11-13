package com.retroconver.data.remote

import androidx.annotation.StringRes
import com.retroconver.R

/**
 * Texto en español que se enseña a la persona usuaria para cada fallo (E4-03.2, #115).
 * Devuelve el id del texto en `strings_red.xml`; la pantalla lo pinta con `stringResource`.
 *
 * Los códigos son los de Supabase Auth y de Postgres. Si llega uno que no está aquí, se
 * muestra un mensaje general según el código HTTP.
 */
@StringRes
fun ResultadoRed.Fallo.mensaje(): Int = when (this) {
    ResultadoRed.SinConexion -> R.string.red_sin_conexion
    ResultadoRed.SesionCaducada -> R.string.red_sesion_caducada
    is ResultadoRed.ErrorServidor -> mensajeDeError(this)
}

@StringRes
private fun mensajeDeError(error: ResultadoRed.ErrorServidor): Int {
    val codigo = error.codigo.orEmpty()
    return when {
        codigo == ResultadoRed.CODIGO_SIN_CONFIGURAR -> R.string.red_sin_configurar
        codigo == ResultadoRed.CODIGO_RESPUESTA_NO_VALIDA -> R.string.red_respuesta_no_valida
        // Supabase Auth
        codigo in CREDENCIALES_INCORRECTAS -> R.string.red_credenciales_incorrectas
        codigo in EMAIL_YA_REGISTRADO -> R.string.red_email_registrado
        codigo == "weak_password" -> R.string.red_contrasena_debil
        codigo == "email_not_confirmed" -> R.string.red_email_sin_confirmar
        codigo == "email_address_invalid" -> R.string.red_email_no_valido
        codigo == "validation_failed" -> R.string.red_datos_no_validos
        error.estadoHttp == 429 || codigo.startsWith("over_") -> R.string.red_demasiados_intentos
        // Postgres: 23514 = no cumple un CHECK (texto vacío o demasiado largo); 42501 = política de seguridad
        codigo == "23514" -> R.string.red_texto_no_valido
        codigo == "42501" || error.estadoHttp == 403 -> R.string.red_sin_permiso
        error.estadoHttp == 404 -> R.string.red_no_encontrado
        error.estadoHttp >= 500 -> R.string.red_servidor_caido
        else -> R.string.red_error_desconocido
    }
}

private val CREDENCIALES_INCORRECTAS = setOf("invalid_credentials", "invalid_grant")
private val EMAIL_YA_REGISTRADO = setOf("user_already_exists", "email_exists")
