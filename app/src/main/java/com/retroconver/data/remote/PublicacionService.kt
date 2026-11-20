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
 * Tablas `publicaciones` y `comentarios` del servidor (E4-06 y E4-07, #123 y #18).
 *
 * Al leer el muro se piden en la misma llamada el autor de cada publicación y sus comentarios
 * con sus autores (`select=*,autor:perfiles(*),comentarios(*,autor:perfiles(*))`): PostgREST
 * los une por las claves foráneas. Así el contador de comentarios del muro es correcto sin
 * abrir cada publicación. Las políticas de seguridad dejan leer todo con sesión y cambiar o
 * borrar solo lo propio.
 */
interface PublicacionService {

    /**
     * Las publicaciones más recientes de todas las personas, con su autor y sus comentarios.
     * No hay algoritmo: orden cronológico inverso y nada más.
     */
    @GET("rest/v1/publicaciones?select=$PUBLICACION_COMPLETA&order=creada_en.desc")
    suspend fun ultimas(
        @Query("limit") limite: Int,
        /** Solo las de estas personas (`in.(id1,id2)`, EX-04), o todas si es `null`. */
        @Query("autor_id") filtroAutores: String? = null,
    ): List<PublicacionDto>

    /** Una publicación con su autor y sus comentarios. Lista vacía si se ha borrado. */
    @GET("rest/v1/publicaciones?select=$PUBLICACION_COMPLETA")
    suspend fun publicacion(@Query("id") filtroId: String): List<PublicacionDto>

    /** Crea una publicación de la cuenta de la sesión (el servidor pone el autor) y la devuelve con su autor. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/publicaciones?select=*,autor:perfiles(*)")
    suspend fun crear(@Body publicacion: NuevaPublicacionDto): List<PublicacionDto>

    /** Borra una publicación propia. Devuelve la fila borrada, o lista vacía si no era tuya. */
    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/publicaciones")
    suspend fun borrar(@Query("id") filtroId: String): List<PublicacionDto>

    /** Comentarios de una publicación con su autor. Filtro: `igual(idDeLaPublicacion)`. */
    @GET("rest/v1/comentarios?select=*,autor:perfiles(*)&order=creado_en.desc")
    suspend fun comentariosDe(@Query("publicacion_id") filtroPublicacion: String): List<ComentarioDto>

    /** Borra un comentario propio. Devuelve la fila borrada, o lista vacía si no era tuyo. */
    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/comentarios")
    suspend fun borrarComentario(@Query("id") filtroId: String): List<ComentarioDto>

    /** Añade un comentario de la cuenta de la sesión y lo devuelve con su autor. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/comentarios?select=*,autor:perfiles(*)")
    suspend fun comentar(@Body comentario: NuevoComentarioDto): List<ComentarioDto>

    /** Da «Me gusta» a una publicación en nombre de la cuenta de la sesión (EX-05, #130). */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/me_gusta")
    suspend fun darMeGusta(@Body meGusta: NuevoMeGustaDto): List<MeGustaDto>

    /** Quita el «Me gusta» propio. Filtros: `igual(idDeLaPublicacion)` y `igual(miId)`. */
    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/me_gusta")
    suspend fun quitarMeGusta(
        @Query("publicacion_id") filtroPublicacion: String,
        @Query("usuario_id") filtroUsuario: String,
    ): List<MeGustaDto>
}

/** Cada publicación con su autor, sus comentarios con sus autores y quién le ha dado «Me gusta». */
private const val PUBLICACION_COMPLETA = "*,autor:perfiles(*),comentarios(*,autor:perfiles(*)),me_gusta(*)"

/** Fila de `publicaciones` (supabase/esquema.sql), con su autor y sus comentarios si se han pedido. */
@Serializable
data class PublicacionDto(
    val id: String,
    @SerialName("autor_id") val autorId: String,
    val texto: String,
    /** Ruta dentro del bucket `imagenes`, o `null` si no lleva imagen. */
    @SerialName("imagen_ruta") val imagenRuta: String? = null,
    @SerialName("creada_en") val creadaEn: String,
    val autor: PerfilDto? = null,
    val comentarios: List<ComentarioDto> = emptyList(),
    /** Quién le ha dado «Me gusta» (EX-05, #130). */
    @SerialName("me_gusta") val meGusta: List<MeGustaDto> = emptyList(),
)

/** Fila de `comentarios`, con su autor si se ha pedido. */
@Serializable
data class ComentarioDto(
    val id: String,
    @SerialName("publicacion_id") val publicacionId: String,
    @SerialName("autor_id") val autorId: String,
    val texto: String,
    @SerialName("creado_en") val creadoEn: String,
    val autor: PerfilDto? = null,
    /** Comentario al que responde (EX-02, #29), o `null`. */
    @SerialName("respuesta_a") val respuestaA: String? = null,
)

/** Fila de `me_gusta` (EX-05, #130). */
@Serializable
data class MeGustaDto(
    @SerialName("publicacion_id") val publicacionId: String,
    @SerialName("usuario_id") val usuarioId: String,
)

@Serializable
data class NuevoMeGustaDto(
    @SerialName("publicacion_id") val publicacionId: String,
)

@Serializable
data class NuevaPublicacionDto(
    val texto: String,
    @SerialName("imagen_ruta") val imagenRuta: String? = null,
)

@Serializable
data class NuevoComentarioDto(
    @SerialName("publicacion_id") val publicacionId: String,
    val texto: String,
    @SerialName("respuesta_a") val respuestaA: String? = null,
)
