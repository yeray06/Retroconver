package com.retroconver.data.remote

import com.retroconver.R
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

/**
 * Traducción de los errores de Supabase y de las fechas del servidor (E4-03.2, #115).
 * No usan red: prueban las funciones directamente.
 */
class ResultadoRedTest {

    // ---------- Formatos de error de cada parte de Supabase ----------

    @Test
    fun errorDeAuthConCodigoNumerico() {
        val error = errorDeRespuesta(422, """{"code":422,"error_code":"weak_password","msg":"Password should be at least 6 characters."}""")

        assertEquals(ResultadoRed.ErrorServidor(422, "weak_password", "Password should be at least 6 characters."), error)
    }

    @Test
    fun errorDeAuthEnElFormatoAntiguo() {
        val error = errorDeRespuesta(400, """{"error":"invalid_grant","error_description":"Invalid login credentials"}""")

        assertEquals(ResultadoRed.ErrorServidor(400, "invalid_grant", "Invalid login credentials"), error)
    }

    @Test
    fun errorDeUnaTabla() {
        val error = errorDeRespuesta(400, """{"code":"23514","details":null,"hint":null,"message":"violates check constraint"}""")

        assertEquals(ResultadoRed.ErrorServidor(400, "23514", "violates check constraint"), error)
    }

    @Test
    fun errorDelAlmacenamiento() {
        val error = errorDeRespuesta(404, """{"statusCode":"404","error":"not_found","message":"Object not found"}""")

        assertEquals(ResultadoRed.ErrorServidor(404, "not_found", "Object not found"), error)
    }

    @Test
    fun unCuerpoVacioOQueNoEsJsonDejaSoloElCodigoHttp() {
        assertEquals(ResultadoRed.ErrorServidor(500), errorDeRespuesta(500, null))
        assertEquals(ResultadoRed.ErrorServidor(500), errorDeRespuesta(500, ""))
        assertEquals(ResultadoRed.ErrorServidor(502), errorDeRespuesta(502, "<html>Bad gateway</html>"))
        assertEquals(ResultadoRed.ErrorServidor(500), errorDeRespuesta(500, "[1, 2]"))
    }

    @Test
    fun un401SiempreEsSesionCaducada() {
        assertEquals(ResultadoRed.SesionCaducada, errorDeRespuesta(401, """{"message":"Invalid JWT"}"""))
        assertEquals(ResultadoRed.SesionCaducada, errorDeRespuesta(401, null))
    }

    // ---------- Mensajes en español ----------

    @Test
    fun cadaFalloTieneSuMensaje() {
        val casos = mapOf(
            ResultadoRed.SinConexion to R.string.red_sin_conexion,
            ResultadoRed.SesionCaducada to R.string.red_sesion_caducada,
            ResultadoRed.ErrorServidor(400, "invalid_grant") to R.string.red_credenciales_incorrectas,
            ResultadoRed.ErrorServidor(400, "invalid_credentials") to R.string.red_credenciales_incorrectas,
            ResultadoRed.ErrorServidor(422, "user_already_exists") to R.string.red_email_registrado,
            ResultadoRed.ErrorServidor(422, "email_exists") to R.string.red_email_registrado,
            ResultadoRed.ErrorServidor(422, "weak_password") to R.string.red_contrasena_debil,
            ResultadoRed.ErrorServidor(400, "email_not_confirmed") to R.string.red_email_sin_confirmar,
            ResultadoRed.ErrorServidor(400, "email_address_invalid") to R.string.red_email_no_valido,
            ResultadoRed.ErrorServidor(400, "validation_failed") to R.string.red_datos_no_validos,
            ResultadoRed.ErrorServidor(429, "over_email_send_rate_limit") to R.string.red_demasiados_intentos,
            ResultadoRed.ErrorServidor(429) to R.string.red_demasiados_intentos,
            ResultadoRed.ErrorServidor(400, "23514") to R.string.red_texto_no_valido,
            ResultadoRed.ErrorServidor(403, "42501") to R.string.red_sin_permiso,
            ResultadoRed.ErrorServidor(403) to R.string.red_sin_permiso,
            ResultadoRed.ErrorServidor(404, "not_found") to R.string.red_no_encontrado,
            ResultadoRed.ErrorServidor(413, "Payload too large") to R.string.red_imagen_demasiado_grande,
            ResultadoRed.ErrorServidor(415, "invalid_mime_type") to R.string.red_imagen_no_valida,
            ResultadoRed.ErrorServidor(500) to R.string.red_servidor_caido,
            ResultadoRed.ErrorServidor(418, "codigo_que_no_conocemos") to R.string.red_error_desconocido,
            ResultadoRed.ErrorServidor(0, ResultadoRed.CODIGO_SIN_CONFIGURAR) to R.string.red_sin_configurar,
            ResultadoRed.ErrorServidor(0, ResultadoRed.CODIGO_RESPUESTA_NO_VALIDA) to R.string.red_respuesta_no_valida,
        )
        casos.forEach { (fallo, mensaje) -> assertEquals("Mensaje de $fallo", mensaje, fallo.mensaje()) }
    }

    // ---------- Utilidades ----------

    @Test
    fun mapTransformaSoloLosResultadosCorrectos() {
        assertEquals(ResultadoRed.Correcto(4), ResultadoRed.Correcto(2).map { it * 2 })
        val fallo: ResultadoRed<Int> = ResultadoRed.SinConexion
        assertEquals(ResultadoRed.SinConexion, fallo.map { it * 2 })
    }

    @Test
    fun lasFechasDelServidorSeLeenConCualquierPrecision() {
        val esperado = Instant.parse("2026-11-11T09:30:00Z")
        assertEquals(esperado, instanteDelServidor("2026-11-11T09:30:00+00:00"))
        assertEquals(esperado, instanteDelServidor("2026-11-11T10:30:00+01:00"))
        assertEquals(esperado, instanteDelServidor("2026-11-11T09:30:00Z"))
        assertEquals(esperado.plusNanos(123_456_000), instanteDelServidor("2026-11-11T09:30:00.123456+00:00"))
    }

    // ---------- Configuración ----------

    @Test
    fun laUrlBaseSiempreAcabaEnUnaBarra() {
        assertEquals("https://abc.supabase.co/", ConfiguracionSupabase("https://abc.supabase.co", "clave").urlBase)
        assertEquals("https://abc.supabase.co/", ConfiguracionSupabase("https://abc.supabase.co/", "clave").urlBase)
    }

    @Test
    fun laUrlPublicaDeUnaImagenApuntaAlBucket() {
        val configuracion = ConfiguracionSupabase("https://abc.supabase.co", "clave")

        assertEquals(
            "https://abc.supabase.co/storage/v1/object/public/imagenes/cuenta/foto.jpg",
            configuracion.urlPublica("cuenta/foto.jpg"),
        )
    }

    @Test
    fun sinUrlOSinClaveNoEstaConfigurada() {
        assertEquals(false, ConfiguracionSupabase("", "clave").configurada)
        assertEquals(false, ConfiguracionSupabase("https://abc.supabase.co", " ").configurada)
        assertEquals(true, ConfiguracionSupabase("https://abc.supabase.co", "clave").configurada)
    }

    @Test
    fun laClaveNoSeEscribeEnLosRegistros() {
        val texto = ConfiguracionSupabase("https://abc.supabase.co", "clave-secreta").toString()

        assertEquals(false, texto.contains("clave-secreta"))
    }

    @Test
    fun elFiltroIgualEsElDePostgrest() {
        assertEquals("eq.abc", igual("abc"))
    }
}
