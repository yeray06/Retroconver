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
 *   Si la petición ya trae su propio `Authorization` (como el cierre de sesión), se respeta.
 */
class InterceptorSupabase(
    private val configuracion: ConfiguracionSupabase,
    private val fuenteDeToken: FuenteDeToken,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!configuracion.configurada) throw ServidorNoConfiguradoException()
        val original = chain.request()
        val peticion = original.newBuilder().header(CABECERA_CLAVE, configuracion.claveAnonima)
        if (original.header(CABECERA_AUTORIZACION) == null) {
            val token = fuenteDeToken.tokenDeAcceso() ?: configuracion.claveAnonima
            peticion.header(CABECERA_AUTORIZACION, "Bearer $token")
        }
        return chain.proceed(peticion.build())
    }

    companion object {
        const val CABECERA_CLAVE = "apikey"
        const val CABECERA_AUTORIZACION = "Authorization"
    }
}
