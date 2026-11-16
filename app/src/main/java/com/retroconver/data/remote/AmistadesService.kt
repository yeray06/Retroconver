package com.retroconver.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Tabla `amistades` del servidor (EX-04, #129).
 *
 * Cada fila trae los perfiles de las dos personas (`solicitante` y `destinatario`), que se
 * guardan antes en Room. Las políticas de seguridad dejan ver las amistades aceptadas de todos
 * y las solicitudes pendientes solo a sus dos personas; pedir solo en nombre propio, aceptar
 * solo a quien la recibe y borrar a cualquiera de las dos.
 */
interface AmistadesService {

    /**
     * Amistades y solicitudes en las que participa una persona. [filtroParticipa] se construye
     * con [participa]; con [filtroAceptada] = `is.true` solo vienen las aceptadas.
     */
    @GET("rest/v1/amistades?select=$PERFILES_DE_LA_AMISTAD&order=creada_en.desc")
    suspend fun de(
        @Query("or") filtroParticipa: String,
        @Query("aceptada") filtroAceptada: String? = null,
    ): List<AmistadDto>

    /** Pide amistad a otra persona. El servidor pone a la cuenta de la sesión como solicitante. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/amistades?select=$PERFILES_DE_LA_AMISTAD")
    suspend fun pedir(@Body solicitud: NuevaAmistadDto): List<AmistadDto>

    /** Acepta una solicitud recibida. Lista vacía si no era para la cuenta de la sesión. */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/amistades?select=$PERFILES_DE_LA_AMISTAD")
    suspend fun aceptar(@Query("id") filtroId: String, @Body cambio: AceptarAmistadDto): List<AmistadDto>

    /** Rechaza o cancela una solicitud, o deja de ser amigos. Lista vacía si no participa en ella. */
    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/amistades")
    suspend fun borrar(@Query("id") filtroId: String): List<AmistadDto>
}

/** Cada amistad con los perfiles de sus dos personas. Hay dos claves foráneas a `perfiles`: se dice cuál es cuál. */
private const val PERFILES_DE_LA_AMISTAD = "*,solicitante:perfiles!solicitante_id(*),destinatario:perfiles!destinatario_id(*)"

/** Filtro de PostgREST «[id] pidió o recibió la amistad». */
fun participa(id: String): String = "(solicitante_id.eq.$id,destinatario_id.eq.$id)"

/** Fila de `amistades` (supabase/esquema.sql), con los perfiles si se han pedido. */
@Serializable
data class AmistadDto(
    val id: String,
    @SerialName("solicitante_id") val solicitanteId: String,
    @SerialName("destinatario_id") val destinatarioId: String,
    val aceptada: Boolean = false,
    @SerialName("creada_en") val creadaEn: String,
    val solicitante: PerfilDto? = null,
    val destinatario: PerfilDto? = null,
)

@Serializable
data class NuevaAmistadDto(
    @SerialName("destinatario_id") val destinatarioId: String,
)

@Serializable
data class AceptarAmistadDto(
    val aceptada: Boolean = true,
)
