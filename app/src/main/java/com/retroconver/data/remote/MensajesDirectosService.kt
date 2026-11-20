package com.retroconver.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Tabla `mensajes_directos` del servidor (EX-01, #28): conversaciones privadas entre amigos.
 *
 * Las políticas de seguridad solo dejan leer los mensajes en los que la cuenta de la sesión es
 * remitente o destinataria, y escribir en nombre propio a un amigo. Por eso «todos» son siempre
 * los míos. No se editan ni se borran.
 */
interface MensajesDirectosService {

    /** Los últimos mensajes en los que estoy, el más reciente primero, con las dos personas. */
    @GET("rest/v1/mensajes_directos?select=$DIRECTO_CON_PERSONAS&order=creado_en.desc")
    suspend fun mios(@Query("limit") limite: Int): List<MensajeDirectoDto>

    /** Envía un mensaje (o un zumbido). El servidor pone como remitente a la cuenta de la sesión. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/mensajes_directos?select=$DIRECTO_CON_PERSONAS")
    suspend fun enviar(@Body mensaje: NuevoMensajeDirectoDto): List<MensajeDirectoDto>
}

/** Hay dos claves foráneas a `perfiles`: se dice cuál es cuál. */
private const val DIRECTO_CON_PERSONAS =
    "*,remitente:perfiles!remitente_id(*),destinatario:perfiles!destinatario_id(*)"

/** Fila de `mensajes_directos` (supabase/esquema.sql), con las dos personas si se han pedido. */
@Serializable
data class MensajeDirectoDto(
    val id: String,
    @SerialName("remitente_id") val remitenteId: String,
    @SerialName("destinatario_id") val destinatarioId: String,
    val texto: String,
    val zumbido: Boolean = false,
    @SerialName("creado_en") val creadoEn: String,
    val remitente: PerfilDto? = null,
    val destinatario: PerfilDto? = null,
)

@Serializable
data class NuevoMensajeDirectoDto(
    @SerialName("destinatario_id") val destinatarioId: String,
    val texto: String,
    val zumbido: Boolean = false,
)
