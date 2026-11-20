package com.retroconver.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.Query

/**
 * Tabla `perfiles` del servidor (`/rest/v1/perfiles`). Lo usa el módulo Perfil (E4-05).
 *
 * Supabase expone cada tabla con PostgREST: los filtros van en la URL (`?id=eq.{uuid}`)
 * y las políticas de seguridad deciden qué filas se pueden leer o cambiar.
 * Para construir el filtro se usa [igual].
 */
interface PerfilService {

    /** El perfil con ese id. PostgREST siempre devuelve una lista: vacía si no existe. */
    @GET("rest/v1/perfiles?select=*")
    suspend fun obtener(@Query("id") filtroId: String): List<PerfilDto>

    /**
     * Perfiles cuyo nombre cumple [filtroNombre] ([contiene]), por orden alfabético y sin el de
     * [filtroId] (`neq.{id}`, el propio). Es el buscador de amigos (EX-04, #129).
     */
    @GET("rest/v1/perfiles?select=*&order=nombre.asc")
    suspend fun buscar(
        @Query("nombre") filtroNombre: String,
        @Query("id") filtroId: String,
        @Query("limit") limite: Int,
    ): List<PerfilDto>

    /** Cambia nombre, biografía y estado. Devuelve la fila ya cambiada (o vacía si no es tuya). */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/perfiles")
    suspend fun actualizarDatos(@Query("id") filtroId: String, @Body cambios: CambiosPerfilDto): List<PerfilDto>

    /** Cambia la ruta del avatar dentro del bucket (`null` lo quita). */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/perfiles")
    suspend fun actualizarAvatar(@Query("id") filtroId: String, @Body cambio: CambioAvatarDto): List<PerfilDto>

    /** Cambia el tema y la tipografía del perfil (EX-03, #30). */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/perfiles")
    suspend fun actualizarEstilo(@Query("id") filtroId: String, @Body cambio: CambioEstiloDto): List<PerfilDto>
}

/** Filtro de PostgREST «columna igual a [valor]»: `igual(id)` → `eq.{id}`. */
fun igual(valor: String): String = "eq.$valor"

/** Filtro de PostgREST «columna distinta de [valor]»: `distinto(id)` → `neq.{id}`. */
fun distinto(valor: String): String = "neq.$valor"

/**
 * Filtro de PostgREST «la columna contiene [texto]», sin distinguir mayúsculas: `ilike.*texto*`.
 * Los comodines de SQL que escriba la persona (`%`, `_`) se buscan como texto y el `*` se quita,
 * porque PostgREST lo convierte en comodín.
 */
fun contiene(texto: String): String {
    val literal = texto.replace("*", "").replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    return "ilike.*$literal*"
}

/** Fila de `perfiles` tal como la devuelve el servidor (supabase/esquema.sql). */
@Serializable
data class PerfilDto(
    val id: String,
    val nombre: String,
    val biografia: String = "",
    val estado: String = "",
    /** Ruta dentro del bucket `imagenes`, o `null` si no tiene avatar. */
    @SerialName("avatar_ruta") val avatarRuta: String? = null,
    @SerialName("color_fondo") val colorFondo: String? = null,
    val tema: String? = null,
    val tipografia: String? = null,
    /** Fecha ISO 8601; se convierte con [instanteDelServidor]. */
    @SerialName("creado_en") val creadoEn: String,
)

@Serializable
data class CambiosPerfilDto(
    val nombre: String,
    val biografia: String,
    val estado: String,
)

@Serializable
data class CambioAvatarDto(
    @SerialName("avatar_ruta") val avatarRuta: String?,
)

/** Claves del tema y de la tipografía (EX-03). El servidor solo acepta las tipografías conocidas. */
@Serializable
data class CambioEstiloDto(
    val tema: String,
    val tipografia: String,
)
