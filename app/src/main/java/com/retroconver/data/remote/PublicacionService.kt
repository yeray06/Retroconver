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
    @GET("rest/v1/publicaciones?select=*,autor:perfiles(*),comentarios(*,autor:perfiles(*))&order=creada_en.desc")
    suspend fun ultimas(@Query("limit") limite: Int): List<PublicacionDto>

    /** Una publicación con su autor y sus comentarios. Lista vacía si se ha borrado. */
    @GET("rest/v1/publicaciones?select=*,autor:perfiles(*),comentarios(*,autor:perfiles(*))")
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

    /** Añade un comentario de la cuenta de la sesión y lo devuelve con su autor. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/comentarios?select=*,autor:perfiles(*)")
    suspend fun comentar(@Body comentario: NuevoComentarioDto): List<ComentarioDto>
}

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
)
