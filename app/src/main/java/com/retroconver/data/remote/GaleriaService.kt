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
 * Tablas `albumes` y `fotos` del servidor (E4-10.1, #68).
 *
 * Al leer un álbum se piden también su dueño y sus fotos en la misma llamada
 * (`select=*,propietario:perfiles(*),fotos(*)`): PostgREST los une por las claves foráneas.
 * Las políticas de seguridad dejan leer los álbumes de todos y cambiar solo los propios.
 */
interface GaleriaService {

    /** Álbumes de una persona con su dueño y sus fotos. Filtro: `igual(idDeLaPersona)`. */
    @GET("rest/v1/albumes?select=*,propietario:perfiles(*),fotos(*)&order=creado_en.desc")
    suspend fun albumesDe(@Query("propietario_id") filtroPropietario: String): List<AlbumDto>

    /** Un álbum con su dueño y sus fotos. Lista vacía si se ha borrado. */
    @GET("rest/v1/albumes?select=*,propietario:perfiles(*),fotos(*)")
    suspend fun album(@Query("id") filtroId: String): List<AlbumDto>

    /** Crea un álbum de la cuenta de la sesión (el servidor pone el dueño) y lo devuelve con su dueño. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/albumes?select=*,propietario:perfiles(*)")
    suspend fun crearAlbum(@Body album: NuevoAlbumDto): List<AlbumDto>

    /** Añade una foto, ya subida al almacenamiento, a un álbum propio. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/fotos")
    suspend fun anadirFoto(@Body foto: NuevaFotoDto): List<FotoDto>

    /** Borra una foto propia. Devuelve la fila borrada, o lista vacía si no era tuya. */
    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/fotos")
    suspend fun borrarFoto(@Query("id") filtroId: String): List<FotoDto>
}

/** Fila de `albumes` (supabase/esquema.sql), con su dueño y sus fotos si se han pedido. */
@Serializable
data class AlbumDto(
    val id: String,
    @SerialName("propietario_id") val propietarioId: String,
    val titulo: String,
    val descripcion: String = "",
    @SerialName("creado_en") val creadoEn: String,
    val propietario: PerfilDto? = null,
    val fotos: List<FotoDto> = emptyList(),
)

/** Fila de `fotos`. */
@Serializable
data class FotoDto(
    val id: String,
    @SerialName("album_id") val albumId: String,
    @SerialName("propietario_id") val propietarioId: String,
    /** Ruta dentro del bucket `imagenes`. */
    val ruta: String,
    val ancho: Int? = null,
    val alto: Int? = null,
    @SerialName("creada_en") val creadaEn: String,
)

@Serializable
data class NuevoAlbumDto(
    val titulo: String,
    val descripcion: String = "",
)

@Serializable
data class NuevaFotoDto(
    @SerialName("album_id") val albumId: String,
    val ruta: String,
    val ancho: Int?,
    val alto: Int?,
)
