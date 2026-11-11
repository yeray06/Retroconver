package com.retroconver.data.sesion

import com.retroconver.data.remote.InterceptorSupabase
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Cuando el servidor responde 401 (token caducado o revocado), OkHttp llama aquí antes de dar
 * la petición por fallida (E4-04.1, #117). Se renueva la sesión y se repite la petición una
 * sola vez con el token nuevo. Si no se puede renovar, el 401 llega al repositorio como
 * [com.retroconver.data.remote.ResultadoRed.SesionCaducada].
 */
class RenovadorDeSesion(private val sesion: SesionRepository) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Ya se repitió una vez con un token nuevo y sigue fallando: no se insiste
        if (response.priorResponse != null) return null
        val tokenRechazado = response.request.header(InterceptorSupabase.CABECERA_AUTORIZACION)?.removePrefix("Bearer ")
        val nueva = sesion.renovarTrasRechazo(tokenRechazado) ?: return null
        return response.request.newBuilder()
            .header(InterceptorSupabase.CABECERA_AUTORIZACION, "Bearer ${nueva.tokenAcceso}")
            .build()
    }
}
