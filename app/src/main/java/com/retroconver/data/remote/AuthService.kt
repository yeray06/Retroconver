package com.retroconver.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Cuentas con Supabase Auth (`/auth/v1`). Lo usa la sesión (E4-04).
 *
 * La contraseña solo viaja aquí, por HTTPS, y no se guarda en ningún sitio del móvil.
 *
 * Este servicio va en un cliente HTTP **sin** el token de la sesión: así renovar el token
 * no depende del propio token. Por eso el cierre de sesión pasa su token a mano.
 */
interface AuthService {

    /** Crea una cuenta. El nombre va en los metadatos y el servidor crea con él la fila de `perfiles`. */
    @POST("auth/v1/signup")
    suspend fun registrar(@Body datos: RegistroDto): RespuestaRegistroDto

    /** Inicia sesión con email y contraseña. */
    @POST("auth/v1/token?grant_type=password")
    suspend fun iniciarSesion(@Body credenciales: CredencialesDto): SesionDto

    /**
     * Pide un token nuevo con el de renovación. No es `suspend` porque la llama el
     * `Authenticator` de OkHttp, que trabaja de forma síncrona en su propio hilo.
     */
    @POST("auth/v1/token?grant_type=refresh_token")
    fun renovarSesion(@Body token: RenovarSesionDto): Call<SesionDto>

    /**
     * Invalida la sesión en el servidor. Responde sin cuerpo (204).
     *
     * @param autorizacion `"Bearer {token de acceso}"` de la sesión que se cierra.
     */
    @POST("auth/v1/logout")
    suspend fun cerrarSesion(@Header("Authorization") autorizacion: String): Response<Unit>
}

@Serializable
data class CredencialesDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegistroDto(
    val email: String,
    val password: String,
    /** Metadatos de la cuenta. El trigger `crear_perfil_al_registrarse` lee de aquí el nombre. */
    val data: DatosRegistroDto,
)

@Serializable
data class DatosRegistroDto(val nombre: String)

@Serializable
data class RenovarSesionDto(
    @SerialName("refresh_token") val tokenRenovacion: String,
)

/** Sesión abierta: tokens y cuenta. */
@Serializable
data class SesionDto(
    @SerialName("access_token") val tokenAcceso: String,
    @SerialName("refresh_token") val tokenRenovacion: String,
    /** Segundos que dura el token de acceso (una hora en Supabase). */
    @SerialName("expires_in") val duracionSegundos: Long = 3600,
    val user: CuentaDto,
)

@Serializable
data class CuentaDto(
    /** UUID de la cuenta. Es también el id de su fila en `perfiles`. */
    val id: String,
    val email: String? = null,
)

/**
 * Respuesta al registrarse. Depende de si el proyecto pide confirmar el email:
 * - **Sin confirmación:** trae la sesión (tokens y `user`) y la persona ya está dentro.
 * - **Con confirmación:** trae solo la cuenta (`id`, `email`…) y hay que confirmar el correo antes de entrar.
 */
@Serializable
data class RespuestaRegistroDto(
    @SerialName("access_token") val tokenAcceso: String? = null,
    @SerialName("refresh_token") val tokenRenovacion: String? = null,
    @SerialName("expires_in") val duracionSegundos: Long? = null,
    val user: CuentaDto? = null,
    val id: String? = null,
    val email: String? = null,
) {
    /** La sesión, si el servidor la ha abierto ya (proyecto sin confirmación de email). */
    fun sesion(): SesionDto? {
        val acceso = tokenAcceso ?: return null
        val renovacion = tokenRenovacion ?: return null
        val cuenta = user ?: return null
        return SesionDto(acceso, renovacion, duracionSegundos ?: 3600, cuenta)
    }
}
