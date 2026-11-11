package com.retroconver.data.remote

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Imágenes en Supabase Storage (`/storage/v1`): avatar, imágenes del muro y fotos de los álbumes.
 *
 * Cada cuenta solo puede subir y borrar dentro de su carpeta (`imagenes/{id de la cuenta}/…`).
 * Para ver una imagen no hace falta este servicio: Coil la carga con
 * [ConfiguracionSupabase.urlPublica].
 */
interface AlmacenamientoService {

    /**
     * Sube una imagen ya reescalada a 720p.
     *
     * @param ruta Ruta dentro del bucket, con las barras sin codificar: `{id de la cuenta}/{uuid}.jpg`.
     * @param imagen Bytes del JPEG, por ejemplo `fichero.asRequestBody("image/jpeg".toMediaType())`.
     */
    @POST("storage/v1/object/{bucket}/{ruta}")
    suspend fun subir(
        @Path("bucket") bucket: String,
        @Path(value = "ruta", encoded = true) ruta: String,
        @Body imagen: RequestBody,
    ): Response<Unit>

    /** Borra una imagen. Se usa al cambiar el avatar o borrar una publicación o una foto. */
    @DELETE("storage/v1/object/{bucket}/{ruta}")
    suspend fun borrar(
        @Path("bucket") bucket: String,
        @Path(value = "ruta", encoded = true) ruta: String,
    ): Response<Unit>
}
