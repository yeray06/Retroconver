package com.retroconver.data.remote

import com.retroconver.R
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.ServerSocket

/**
 * Cliente de red contra un servidor falso (MockWebServer) que devuelve respuestas preparadas
 * como las de Supabase (E4-03, #114 y #115). No hace falta Internet ni el servidor de verdad.
 */
class ClienteSupabaseTest {

    private val servidor = MockWebServer()

    /** Lo que devuelve la sesión en cada prueba: `null` es «sin sesión». */
    private var tokenDeSesion: String? = null

    private lateinit var auth: AuthService
    private lateinit var perfiles: PerfilService
    private lateinit var almacenamiento: AlmacenamientoService

    @Before
    fun preparar() {
        servidor.start()
        val configuracion = ConfiguracionSupabase(url = servidor.url("/").toString(), claveAnonima = CLAVE)
        crearServicios(configuracion)
    }

    @After
    fun cerrar() {
        servidor.shutdown()
    }

    private fun crearServicios(configuracion: ConfiguracionSupabase) {
        val clienteHttp = ClienteSupabase.crearClienteHttp(configuracion, { tokenDeSesion })
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, clienteHttp)
        auth = retrofit.create(AuthService::class.java)
        perfiles = retrofit.create(PerfilService::class.java)
        almacenamiento = retrofit.create(AlmacenamientoService::class.java)
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    // ---------- Cabeceras ----------

    @Test
    fun sinSesionCadaPeticionLlevaLaClavePublica() = runBlocking<Unit> {
        responder(200, "[]")
        llamarAlServidor { perfiles.obtener(igual(ID)) }

        val peticion = servidor.takeRequest()
        assertEquals(CLAVE, peticion.getHeader("apikey"))
        assertEquals("Bearer $CLAVE", peticion.getHeader("Authorization"))
    }

    @Test
    fun conSesionLaPeticionLlevaElTokenDeLaSesion() = runBlocking<Unit> {
        tokenDeSesion = "token-de-la-sesion"
        responder(200, "[]")
        llamarAlServidor { perfiles.obtener(igual(ID)) }

        val peticion = servidor.takeRequest()
        assertEquals(CLAVE, peticion.getHeader("apikey"))
        assertEquals("Bearer token-de-la-sesion", peticion.getHeader("Authorization"))
    }

    // ---------- Respuestas correctas ----------

    @Test
    fun leerUnPerfilConvierteElJsonYUsaElFiltroDePostgrest() = runBlocking<Unit> {
        responder(
            200,
            """[{"id":"$ID","nombre":"Yeray","biografia":"Hola","estado":"","avatar_ruta":null,
                "color_fondo":null,"tema":null,"creado_en":"2026-11-11T09:30:00.123456+00:00",
                "columna_nueva":"la app no la conoce"}]""",
        )

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        val perfil = (resultado as ResultadoRed.Correcto).datos.single()
        assertEquals("Yeray", perfil.nombre)
        assertEquals("Hola", perfil.biografia)
        assertNull(perfil.avatarRuta)
        val peticion = servidor.takeRequest()
        assertEquals("GET", peticion.method)
        assertEquals("/rest/v1/perfiles", peticion.requestUrl?.encodedPath)
        assertEquals("eq.$ID", peticion.requestUrl?.queryParameter("id"))
    }

    @Test
    fun iniciarSesionEnviaLasCredencialesYDevuelveLosTokens() = runBlocking<Unit> {
        responder(
            200,
            """{"access_token":"acceso","token_type":"bearer","expires_in":3600,"refresh_token":"renovacion",
                "user":{"id":"$ID","email":"yeray@retroconver.es"}}""",
        )

        val resultado = llamarAlServidor { auth.iniciarSesion(CredencialesDto("yeray@retroconver.es", "secreta")) }

        val sesion = (resultado as ResultadoRed.Correcto).datos
        assertEquals("acceso", sesion.tokenAcceso)
        assertEquals("renovacion", sesion.tokenRenovacion)
        assertEquals(ID, sesion.user.id)
        val peticion = servidor.takeRequest()
        assertEquals("/auth/v1/token", peticion.requestUrl?.encodedPath)
        assertEquals("password", peticion.requestUrl?.queryParameter("grant_type"))
        val cuerpo = peticion.body.readUtf8()
        assertTrue(cuerpo.contains("\"email\":\"yeray@retroconver.es\""))
        assertTrue(cuerpo.contains("\"password\":\"secreta\""))
    }

    @Test
    fun registrarseEnviaElNombreEnLosMetadatos() = runBlocking<Unit> {
        responder(200, """{"id":"$ID","email":"yeray@retroconver.es"}""")

        llamarAlServidor { auth.registrar(RegistroDto("yeray@retroconver.es", "secreta", DatosRegistroDto("Yeray"))) }

        val cuerpo = servidor.takeRequest().body.readUtf8()
        assertTrue(cuerpo.contains("\"data\":{\"nombre\":\"Yeray\"}"))
    }

    @Test
    fun alRegistrarseConConfirmacionDeEmailNoHaySesion() = runBlocking<Unit> {
        responder(200, """{"id":"$ID","aud":"authenticated","email":"yeray@retroconver.es"}""")

        val resultado = llamarAlServidor { auth.registrar(RegistroDto("yeray@retroconver.es", "secreta", DatosRegistroDto("Yeray"))) }

        assertNull((resultado as ResultadoRed.Correcto).datos.sesion())
    }

    @Test
    fun alRegistrarseSinConfirmacionDeEmailYaHaySesion() = runBlocking<Unit> {
        responder(
            200,
            """{"access_token":"acceso","refresh_token":"renovacion","expires_in":3600,"user":{"id":"$ID"}}""",
        )

        val resultado = llamarAlServidor { auth.registrar(RegistroDto("yeray@retroconver.es", "secreta", DatosRegistroDto("Yeray"))) }

        val sesion = (resultado as ResultadoRed.Correcto).datos.sesion()
        assertNotNull(sesion)
        assertEquals(ID, sesion!!.user.id)
    }

    @Test
    fun subirUnaImagenNoCodificaLasBarrasDeLaRuta() = runBlocking<Unit> {
        responder(200, """{"Key":"imagenes/$ID/foto.jpg","Id":"x"}""")
        val imagen = byteArrayOf(1, 2, 3).toRequestBody("image/jpeg".toMediaType())

        val resultado = llamarAlServidor {
            almacenamiento.subir(ConfiguracionSupabase.BUCKET_IMAGENES, "$ID/foto.jpg", imagen).exigirExito()
        }

        assertTrue(resultado is ResultadoRed.Correcto)
        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/storage/v1/object/imagenes/$ID/foto.jpg", peticion.requestUrl?.encodedPath)
        assertEquals("image/jpeg", peticion.getHeader("Content-Type"))
        assertEquals(3L, peticion.bodySize)
    }

    @Test
    fun unaRespuestaSinCuerpoTambienEsCorrecta() = runBlocking<Unit> {
        responder(204)

        val resultado = llamarAlServidor { auth.cerrarSesion("Bearer token-que-se-cierra").exigirExito() }

        assertTrue(resultado is ResultadoRed.Correcto)
    }

    @Test
    fun siLaPeticionYaTraeSuTokenNoSeCambia() = runBlocking<Unit> {
        tokenDeSesion = "otro-token"
        responder(204)

        llamarAlServidor { auth.cerrarSesion("Bearer token-que-se-cierra").exigirExito() }

        val peticion = servidor.takeRequest()
        assertEquals("Bearer token-que-se-cierra", peticion.getHeader("Authorization"))
        assertEquals(CLAVE, peticion.getHeader("apikey"))
    }

    // ---------- Errores ----------

    @Test
    fun un401EsSesionCaducada() = runBlocking<Unit> {
        responder(401, """{"code":"PGRST301","message":"JWT expired"}""")

        assertEquals(ResultadoRed.SesionCaducada, llamarAlServidor { perfiles.obtener(igual(ID)) })
    }

    @Test
    fun emailYaRegistradoSeTraduce() = runBlocking<Unit> {
        responder(422, """{"code":422,"error_code":"user_already_exists","msg":"User already registered"}""")

        val resultado = llamarAlServidor { auth.registrar(RegistroDto("yeray@retroconver.es", "secreta", DatosRegistroDto("Yeray"))) }

        val error = resultado as ResultadoRed.ErrorServidor
        assertEquals(422, error.estadoHttp)
        assertEquals("user_already_exists", error.codigo)
        assertEquals(R.string.red_email_registrado, error.mensaje())
    }

    @Test
    fun credencialesIncorrectasSeTraducen() = runBlocking<Unit> {
        responder(400, """{"code":400,"error_code":"invalid_credentials","msg":"Invalid login credentials"}""")

        val resultado = llamarAlServidor { auth.iniciarSesion(CredencialesDto("yeray@retroconver.es", "mala")) }

        assertEquals(R.string.red_credenciales_incorrectas, (resultado as ResultadoRed.Fallo).mensaje())
    }

    @Test
    fun unErrorDelServidorSinJsonNoRompeNada() = runBlocking<Unit> {
        responder(503, "<html>Service Unavailable</html>")

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        val error = resultado as ResultadoRed.ErrorServidor
        assertEquals(503, error.estadoHttp)
        assertNull(error.codigo)
        assertEquals(R.string.red_servidor_caido, error.mensaje())
    }

    @Test
    fun unErrorEnUnaRespuestaSinCuerpoTambienSeTraduce() = runBlocking<Unit> {
        responder(500)

        val resultado = llamarAlServidor { auth.cerrarSesion("Bearer token").exigirExito() }

        assertEquals(500, (resultado as ResultadoRed.ErrorServidor).estadoHttp)
    }

    @Test
    fun unJsonQueNoEsElEsperadoEsRespuestaNoValida() = runBlocking<Unit> {
        responder(200, """{"esto no es": "una lista"""")

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        assertEquals(ResultadoRed.CODIGO_RESPUESTA_NO_VALIDA, (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals(R.string.red_respuesta_no_valida, resultado.mensaje())
    }

    @Test
    fun siElServidorNoContestaEsSinConexion() = runBlocking<Unit> {
        // Un puerto que se acaba de cerrar: la conexión se rechaza como sin red
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        crearServicios(ConfiguracionSupabase(url = "http://127.0.0.1:$puertoCerrado", claveAnonima = CLAVE))

        assertEquals(ResultadoRed.SinConexion, llamarAlServidor { perfiles.obtener(igual(ID)) })
    }

    @Test
    fun sinConfiguracionNoSeConectaYSeExplicaPorQue() = runBlocking<Unit> {
        crearServicios(ConfiguracionSupabase(url = "", claveAnonima = ""))

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        assertEquals(ResultadoRed.CODIGO_SIN_CONFIGURAR, (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals(R.string.red_sin_configurar, resultado.mensaje())
        assertEquals(0, servidor.requestCount)
    }

    private companion object {
        const val CLAVE = "clave-publica"
        const val ID = "6f1c2a1e-0000-4000-8000-000000000001"
    }
}
