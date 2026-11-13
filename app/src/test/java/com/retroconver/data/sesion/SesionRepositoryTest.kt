package com.retroconver.data.sesion

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.ServerSocket
import java.time.Instant

/**
 * Sesión con Supabase Auth (E4-04.1, #117) contra un servidor falso (MockWebServer)
 * y un DataStore de verdad en una carpeta temporal.
 */
class SesionRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var almacen: DataStore<Preferences>
    private lateinit var ficheroSesion: File

    /** Reloj de las pruebas: se adelanta a mano para simular que pasa el tiempo. */
    private var ahora = Instant.parse("2026-11-11T10:00:00Z")

    /** Cuántas veces se ha borrado la copia local (al cerrar o caducar la sesión). */
    private var copiasBorradas = 0

    private lateinit var sesion: SesionRepository
    private lateinit var perfiles: PerfilService

    @Before
    fun preparar() {
        servidor.start()
        ambitoDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ficheroSesion = File(carpeta.root, "sesion.preferences_pb")
        almacen = PreferenceDataStoreFactory.create(scope = ambitoDataStore) { ficheroSesion }
        val configuracion = ConfiguracionSupabase(servidor.url("/").toString(), CLAVE)
        sesion = crearSesion(configuracion)
        // Igual que en el AppContainer: las tablas van con el token de la sesión y se renueva al caducar
        val clienteHttp = ClienteSupabase.crearClienteHttp(configuracion, sesion, RenovadorDeSesion(sesion))
        perfiles = ClienteSupabase.crearRetrofit(configuracion.urlBase, clienteHttp).create(PerfilService::class.java)
    }

    @After
    fun cerrar() {
        servidor.shutdown()
        ambitoDataStore.cancel()
    }

    private fun crearSesion(configuracion: ConfiguracionSupabase): SesionRepository {
        val clienteAuth = ClienteSupabase.crearClienteHttp(configuracion, { null })
        val auth = ClienteSupabase.crearRetrofit(configuracion.urlBase, clienteAuth).create(AuthService::class.java)
        return SesionRepository(almacen, auth, alTerminarSesion = { copiasBorradas++ }, reloj = { ahora })
    }

    /** La misma sesión guardada, pero con un servidor que no contesta. */
    private fun sesionSinRed(): SesionRepository {
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        return crearSesion(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", CLAVE))
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun respuestaSesion(acceso: String, renovacion: String) =
        """{"access_token":"$acceso","token_type":"bearer","expires_in":3600,"refresh_token":"$renovacion",
            "user":{"id":"$ID","email":"$EMAIL"}}"""

    /** Inicia sesión con los tokens «acceso-1» y «renovacion-1» y retira la petición del servidor falso. */
    private suspend fun entrar() {
        responder(200, respuestaSesion("acceso-1", "renovacion-1"))
        sesion.iniciarSesion(EMAIL, CONTRASENA)
        servidor.takeRequest()
    }

    // ---------- Entrar y registrarse ----------

    @Test
    fun alEmpezarNoHaySesion() = runBlocking<Unit> {
        assertEquals(EstadoSesion.SinSesion, sesion.estado.first())
        assertNull(sesion.tokenDeAcceso())
    }

    @Test
    fun alEntrarSeGuardanLosTokensPeroNoLaContrasena() = runBlocking<Unit> {
        entrar()

        assertEquals(EstadoSesion.Iniciada(ID), sesion.estado.first())
        assertEquals(ID, sesion.idCuenta.first())
        assertEquals("acceso-1", sesion.tokenDeAcceso())
        assertEquals(ahora.plusSeconds(3600), sesion.sesionActual()?.caducaEn)
        val guardado = ficheroSesion.readBytes().toString(Charsets.UTF_8)
        assertTrue(guardado.contains("acceso-1"))
        assertFalse("La contraseña no puede estar en el móvil", guardado.contains(CONTRASENA))
    }

    @Test
    fun conCredencialesIncorrectasNoSeAbreSesion() = runBlocking<Unit> {
        responder(400, """{"code":400,"error_code":"invalid_credentials","msg":"Invalid login credentials"}""")

        val resultado = sesion.iniciarSesion(EMAIL, "mala")

        assertEquals("invalid_credentials", (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals(EstadoSesion.SinSesion, sesion.estado.first())
    }

    @Test
    fun registrarseSinConfirmacionDeEmailDejaLaSesionAbierta() = runBlocking<Unit> {
        responder(200, respuestaSesion("acceso-1", "renovacion-1"))

        val resultado = sesion.registrar("  Yeray ", EMAIL, CONTRASENA)

        assertEquals(ResultadoRed.Correcto(ResultadoRegistro.DENTRO), resultado)
        assertEquals(EstadoSesion.Iniciada(ID), sesion.estado.first())
        val cuerpo = servidor.takeRequest().body.readUtf8()
        assertTrue("El nombre se envía sin espacios", cuerpo.contains("\"nombre\":\"Yeray\""))
    }

    @Test
    fun registrarseConConfirmacionDeEmailNoAbreSesion() = runBlocking<Unit> {
        responder(200, """{"id":"$ID","aud":"authenticated","email":"$EMAIL"}""")

        val resultado = sesion.registrar("Yeray", EMAIL, CONTRASENA)

        assertEquals(ResultadoRed.Correcto(ResultadoRegistro.CONFIRMAR_EMAIL), resultado)
        assertEquals(EstadoSesion.SinSesion, sesion.estado.first())
    }

    // ---------- Renovar el token ----------

    @Test
    fun siElTokenEstaAPuntoDeCaducarSeRenuevaAntesDeLaPeticion() = runBlocking<Unit> {
        entrar()
        ahora = ahora.plusSeconds(3600 - 30) // le quedan 30 segundos
        responder(200, respuestaSesion("acceso-2", "renovacion-2"))
        responder(200, "[]")

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        assertTrue(resultado is ResultadoRed.Correcto)
        val renovacion = servidor.takeRequest()
        assertEquals("/auth/v1/token", renovacion.requestUrl?.encodedPath)
        assertEquals("refresh_token", renovacion.requestUrl?.queryParameter("grant_type"))
        assertTrue(renovacion.body.readUtf8().contains("renovacion-1"))
        assertEquals("Bearer acceso-2", servidor.takeRequest().getHeader("Authorization"))
        assertEquals("renovacion-2", sesion.sesionActual()?.tokenRenovacion)
    }

    @Test
    fun conUn401SeRenuevaLaSesionYSeRepiteLaPeticion() = runBlocking<Unit> {
        entrar()
        responder(401, """{"code":"PGRST301","message":"JWT expired"}""")
        responder(200, respuestaSesion("acceso-2", "renovacion-2"))
        responder(200, "[]")

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        assertTrue(resultado is ResultadoRed.Correcto)
        assertEquals("Bearer acceso-1", servidor.takeRequest().getHeader("Authorization"))
        assertEquals("/auth/v1/token", servidor.takeRequest().requestUrl?.encodedPath)
        assertEquals("Bearer acceso-2", servidor.takeRequest().getHeader("Authorization"))
        assertEquals("acceso-2", sesion.tokenDeAcceso())
    }

    @Test
    fun siElServidorYaNoAceptaLaRenovacionSeCierraLaSesion() = runBlocking<Unit> {
        entrar()
        responder(401, """{"code":"PGRST301","message":"JWT expired"}""")
        responder(400, """{"code":400,"error_code":"refresh_token_not_found","msg":"Invalid Refresh Token"}""")

        val resultado = llamarAlServidor { perfiles.obtener(igual(ID)) }

        assertEquals(ResultadoRed.SesionCaducada, resultado)
        assertEquals(EstadoSesion.SinSesion, sesion.estado.first())
        assertEquals(1, copiasBorradas)
    }

    @Test
    fun sinRedAlRenovarSeMantieneLaSesion() = runBlocking<Unit> {
        entrar()
        ahora = ahora.plusSeconds(3600 - 30)

        val sinRed = sesionSinRed()

        assertEquals("Sigue con el token que tiene", "acceso-1", sinRed.tokenDeAcceso())
        assertNotNull(sinRed.sesionActual())
        assertEquals(0, copiasBorradas)
    }

    // ---------- Cerrar sesión ----------

    @Test
    fun cerrarSesionAvisaAlServidorYBorraTodo() = runBlocking<Unit> {
        entrar()
        responder(204)

        sesion.cerrarSesion()

        val peticion = servidor.takeRequest()
        assertEquals("/auth/v1/logout", peticion.requestUrl?.encodedPath)
        assertEquals("Bearer acceso-1", peticion.getHeader("Authorization"))
        assertEquals(EstadoSesion.SinSesion, sesion.estado.first())
        assertNull(sesion.tokenDeAcceso())
        assertEquals(1, copiasBorradas)
    }

    @Test
    fun sinConexionTambienSePuedeCerrarSesion() = runBlocking<Unit> {
        entrar()

        sesionSinRed().cerrarSesion()

        assertEquals(EstadoSesion.SinSesion, sesion.estado.first())
        assertEquals(1, copiasBorradas)
    }

    private companion object {
        const val CLAVE = "clave-publica"
        const val ID = "6f1c2a1e-0000-4000-8000-000000000001"
        const val EMAIL = "yeray@retroconver.es"
        const val CONTRASENA = "contraseña-secreta"
    }
}
