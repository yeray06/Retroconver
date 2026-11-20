package com.retroconver.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Tabla `mensajes` del servidor: el tablón de cada perfil (E4-09, #22).
 *
 * Cada mensaje trae a su autor y al dueño del tablón, que se guardan antes en Room. Las
 * políticas de seguridad dejan leer todos los tablones con sesión, escribir solo en nombre
 * propio y borrar al autor del mensaje o al dueño del tablón (RF-23).
 */
interface MensajesService {

    /** Los últimos mensajes del tablón de un perfil, el más reciente primero. Filtro: `igual(idDelPerfil)`. */
    @GET("rest/v1/mensajes?select=$MENSAJE_CON_PERFILES&order=creado_en.desc")
    suspend fun tablon(
        @Query("perfil_id") filtroPerfil: String,
        @Query("limit") limite: Int,
    ): List<MensajeDto>

    /** Escribe en un tablón. El servidor pone como autor a la cuenta de la sesión. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/mensajes?select=$MENSAJE_CON_PERFILES")
    suspend fun escribir(@Body mensaje: NuevoMensajeDto): List<MensajeDto>

    /** Borra un mensaje. Lista vacía si ya no existía o si no se puede borrar. */
    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/mensajes")
    suspend fun borrar(@Query("id") filtroId: String): List<MensajeDto>
}

/** Cada mensaje con su autor y el dueño del tablón. Hay dos claves foráneas a `perfiles`: se dice cuál es cuál. */
private const val MENSAJE_CON_PERFILES = "*,autor:perfiles!autor_id(*),perfil:perfiles!perfil_id(*)"

/** Fila de `mensajes` (supabase/esquema.sql), con los perfiles si se han pedido. */
@Serializable
data class MensajeDto(
    val id: String,
    @SerialName("perfil_id") val perfilId: String,
    @SerialName("autor_id") val autorId: String,
    val texto: String,
    @SerialName("creado_en") val creadoEn: String,
    val autor: PerfilDto? = null,
    val perfil: PerfilDto? = null,
)

@Serializable
data class NuevoMensajeDto(
    @SerialName("perfil_id") val perfilId: String,
    val texto: String,
)
