package com.retroconver.data.remote

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Resultado de cualquier llamada al servidor (E4-03.2, #115).
 *
 * Los repositorios nunca dejan escapar una excepción de red hacia la interfaz: devuelven uno de
 * estos casos y el ViewModel decide qué mostrar. El texto para la persona usuaria sale de
 * [mensaje] (MensajesRed.kt).
 */
sealed interface ResultadoRed<out T> {

    /** El servidor ha respondido bien. */
    data class Correcto<out T>(val datos: T) : ResultadoRed<T>

    /** Cualquier caso en el que no se ha podido hacer lo que se pedía. */
    sealed interface Fallo : ResultadoRed<Nothing>

    /** No hay red o el servidor no contesta. La pantalla sigue mostrando la copia de Room. */
    data object SinConexion : Fallo

    /** El token ya no vale y no se ha podido renovar: hay que volver a iniciar sesión. */
    data object SesionCaducada : Fallo

    /**
     * El servidor ha contestado con un error.
     *
     * @param estadoHttp Código HTTP (400, 403, 422, 500…). Es 0 si el error es de la propia app.
     * @param codigo Código de error de Supabase, por ejemplo `user_already_exists` (Auth) o `23514` (Postgres).
     * @param detalle Mensaje del servidor en inglés. Solo para los registros, no para la pantalla.
     */
    data class ErrorServidor(
        val estadoHttp: Int,
        val codigo: String? = null,
        val detalle: String? = null,
    ) : Fallo

    companion object {
        /** Código de [ErrorServidor] cuando faltan la URL o la clave en `local.properties`. */
        const val CODIGO_SIN_CONFIGURAR = "app_sin_configurar"

        /** Código de [ErrorServidor] cuando la respuesta no tiene el formato esperado. */
        const val CODIGO_RESPUESTA_NO_VALIDA = "respuesta_no_valida"
    }
}

/** Transforma los datos de un resultado correcto y deja igual los fallos. */
inline fun <T, R> ResultadoRed<T>.map(transformar: (T) -> R): ResultadoRed<R> = when (this) {
    is ResultadoRed.Correcto -> ResultadoRed.Correcto(transformar(datos))
    is ResultadoRed.Fallo -> this
}

/**
 * Hace una llamada al servidor y convierte cualquier problema en un [ResultadoRed.Fallo].
 *
 * ```
 * val resultado = llamarAlServidor { perfilService.obtener(igual(id)) }
 * ```
 *
 * La cancelación de la corrutina no se captura: si la pantalla se cierra, la llamada se cancela sin más.
 */
suspend fun <T> llamarAlServidor(llamada: suspend () -> T): ResultadoRed<T> =
    try {
        ResultadoRed.Correcto(llamada())
    } catch (e: ServidorNoConfiguradoException) {
        ResultadoRed.ErrorServidor(0, ResultadoRed.CODIGO_SIN_CONFIGURAR, e.message)
    } catch (e: IOException) {
        // Sin red, DNS que no resuelve, tiempo de espera agotado, conexión cortada…
        ResultadoRed.SinConexion
    } catch (e: HttpException) {
        errorDeRespuesta(e.code(), e.response()?.errorBody()?.string())
    } catch (e: SerializationException) {
        ResultadoRed.ErrorServidor(0, ResultadoRed.CODIGO_RESPUESTA_NO_VALIDA, e.message)
    }

/**
 * Para las llamadas que devuelven `Response<Unit>` (las que pueden responder sin cuerpo):
 * si el servidor ha respondido con error, lanza [HttpException] para que [llamarAlServidor] la traduzca.
 */
fun <T> Response<T>.exigirExito(): Response<T> {
    if (!isSuccessful) throw HttpException(this)
    return this
}

/**
 * Traduce una respuesta de error de Supabase a [ResultadoRed.Fallo].
 *
 * Cada parte de Supabase escribe el error a su manera, así que se buscan varios campos:
 * - **Auth:** `{"code": 422, "error_code": "user_already_exists", "msg": "…"}`
 * - **Auth (antiguo):** `{"error": "invalid_grant", "error_description": "…"}`
 * - **Tablas (PostgREST):** `{"code": "23514", "message": "…"}`
 * - **Storage:** `{"statusCode": "404", "error": "not_found", "message": "…"}`
 */
fun errorDeRespuesta(estadoHttp: Int, cuerpo: String?): ResultadoRed.Fallo {
    if (estadoHttp == 401) return ResultadoRed.SesionCaducada
    val objeto = cuerpo
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { Json.parseToJsonElement(it) as? JsonObject }.getOrNull() }

    fun texto(clave: String): String? =
        (objeto?.get(clave) as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }

    return ResultadoRed.ErrorServidor(
        estadoHttp = estadoHttp,
        codigo = texto("error_code") ?: texto("code") ?: texto("error"),
        detalle = texto("msg") ?: texto("message") ?: texto("error_description"),
    )
}
