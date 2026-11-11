package com.retroconver.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * De dónde saca el cliente de red el token de la sesión. Lo implementa la sesión (E4-04);
 * mientras no hay sesión devuelve `null` y las peticiones van solo con la clave pública.
 *
 * Se llama en un hilo de OkHttp, nunca en el principal, así que puede leer sin `suspend`.
 */
fun interface FuenteDeToken {
    fun tokenDeAcceso(): String?
}

/**
 * La app no tiene la URL o la clave del servidor en `local.properties`.
 * Es una [IOException] para que OkHttp la trate como un fallo de red normal y no cierre la app.
 */
class ServidorNoConfiguradoException :
    IOException("Falta SUPABASE_URL o SUPABASE_ANON_KEY en local.properties (ver supabase/README.md)")

/**
 * Añade a cada petición las dos cabeceras que pide Supabase (E4-03.1, #114):
 *
 * - `apikey`: la clave pública del proyecto.
 * - `Authorization: Bearer …`: el token de la sesión, o la clave pública si no hay sesión.
 *   Con el token, las políticas de seguridad del servidor saben quién hace la petición.
 */
class InterceptorSupabase(
    private val configuracion: ConfiguracionSupabase,
    private val fuenteDeToken: FuenteDeToken,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!configuracion.configurada) throw ServidorNoConfiguradoException()
        val token = fuenteDeToken.tokenDeAcceso() ?: configuracion.claveAnonima
        val peticion = chain.request().newBuilder()
            .header(CABECERA_CLAVE, configuracion.claveAnonima)
            .header(CABECERA_AUTORIZACION, "Bearer $token")
            .build()
        return chain.proceed(peticion)
    }

    companion object {
        const val CABECERA_CLAVE = "apikey"
        const val CABECERA_AUTORIZACION = "Authorization"
    }
}
