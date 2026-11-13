package com.retroconver.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Crea el cliente de red de la app (E4-03.1, #114). Lo usan el [com.retroconver.AppContainer]
 * y las pruebas, que lo apuntan a un servidor falso (MockWebServer).
 *
 * Para añadir llamadas a una tabla nueva, se crea una interfaz como [PerfilService] y se pide
 * en el `AppContainer` con `retrofit.create(MiService::class.java)`.
 */
object ClienteSupabase {

    /**
     * Conversión entre JSON y clases de Kotlin. Ignora los campos que la app no conoce,
     * así añadir una columna en el servidor no rompe las versiones anteriores de la app.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Cliente HTTP con la clave y el token en cada petición.
     *
     * @param autenticador Se llama cuando el servidor responde 401 (token caducado) para renovar
     *   la sesión y repetir la petición. Lo pone la sesión en E4-04.
     */
    fun crearClienteHttp(
        configuracion: ConfiguracionSupabase,
        fuenteDeToken: FuenteDeToken,
        autenticador: Authenticator = Authenticator.NONE,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(InterceptorSupabase(configuracion, fuenteDeToken))
        .authenticator(autenticador)
        // Si el servidor no contesta en este tiempo se considera sin conexión, en vez de esperar sin fin
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Retrofit sobre [clienteHttp], con JSON de kotlinx.serialization. [urlBase] tiene que acabar en «/». */
    fun crearRetrofit(urlBase: String, clienteHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(clienteHttp)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
        .build()
}
