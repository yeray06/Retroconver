package com.retroconver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.MensajesDirectosService
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.sesion.SesionRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.ServerSocket

/**
 * Mensajes directos (EX-01, #28) contra un servidor falso. La sesión es de verdad (DataStore en
 * una carpeta temporal) y Room se sustituye por DAOs en memoria.
 */
class MensajesDirectosRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var sesion: SesionRepository
    private val perfiles = UsuarioDaoEnMemoria()
    private val room = MensajeDirectoDaoEnMemoria(perfiles)
    private lateinit var repositorio: MensajesDirectosRepository

    @Before
    fun preparar() = runBlocking<Unit> {
        servidor.start()
        ambitoDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val almacen = PreferenceDataStoreFactory.create(scope = ambitoDataStore) {
            File(carpeta.root, "sesion.preferences_pb")
        }
        val configuracion = ConfiguracionSupabase(servidor.url("/").toString(), "clave")
        val auth = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, { null }))
            .create(AuthService::class.java)
        sesion = SesionRepository(almacen, auth)
        repositorio = crearRepositorio(configuracion)
        responder(200, """{"access_token":"acceso","refresh_token":"renovacion","expires_in":3600,"user":{"id":"$YO"}}""")
        sesion.iniciarSesion("yeray@retroconver.es", "secreta")
        servidor.takeRequest()
    }

    @After
    fun cerrar() {
        servidor.shutdown()
        ambitoDataStore.cancel()
    }

    private fun crearRepositorio(configuracion: ConfiguracionSupabase): MensajesDirectosRepository {
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, sesion))
        val usuarios = UsuarioRepository(
            usuarioDao = perfiles,
            perfilService = retrofit.create(PerfilService::class.java),
            almacenamiento = retrofit.create(AlmacenamientoService::class.java),
            sesion = sesion,
            configuracion = configuracion,
        )
        return MensajesDirectosRepository(room, retrofit.create(MensajesDirectosService::class.java), usuarios)
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private val nombres = mapOf(YO to "Yeray", ANA to "Ana", BEA to "Bea")

    private fun perfil(id: String) =
        """{"id":"$id","nombre":"${nombres.getValue(id)}","biografia":"","estado":"","avatar_ruta":null,"creado_en":"2026-11-01T09:00:00+00:00"}"""

    private fun directo(id: String, de: String, para: String, texto: String, minuto: Int = 0, zumbido: Boolean = false) =
        """{"id":"$id","remitente_id":"$de","destinatario_id":"$para","texto":"$texto","zumbido":$zumbido,
            "creado_en":"2026-11-20T10:${"%02d".format(minuto)}:00+00:00","remitente":${perfil(de)},"destinatario":${perfil(para)}}"""

    @Test
    fun refrescarGuardaLosMensajesConLasDosPersonas() = runBlocking<Unit> {
        responder(200, "[${directo("d2", ANA, YO, "¿Qué tal?", minuto = 2)},${directo("d1", YO, ANA, "¡Hola!")}]")

        assertTrue(repositorio.refrescar() is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("/rest/v1/mensajes_directos", peticion.requestUrl?.encodedPath)
        assertEquals("200", peticion.requestUrl?.queryParameter("limit"))
        val chat = repositorio.conversacion(YO, ANA).first()
        assertEquals(listOf("¡Hola!", "¿Qué tal?"), chat.map { it.mensaje.texto })
        assertEquals("Ana", chat.first().destinatario.nombre)
    }

    @Test
    fun laListaTieneUnaConversacionPorPersonaConSuUltimoMensaje() = runBlocking<Unit> {
        responder(
            200,
            "[${directo("d3", BEA, YO, "Buenas", minuto = 3)},${directo("d2", ANA, YO, "¿Qué tal?", minuto = 2)}," +
                "${directo("d1", YO, ANA, "¡Hola!")}]",
        )
        repositorio.refrescar()

        val lista = repositorio.conversaciones(YO).first()

        assertEquals(listOf("Bea", "Ana"), lista.map { it.persona.nombre })
        assertEquals("¿Qué tal?", lista[1].ultimo.mensaje.texto)
    }

    @Test
    fun enviarMandaElDestinatarioYGuardaLaRespuesta() = runBlocking<Unit> {
        responder(201, "[${directo("d1", YO, ANA, "¡Hola!")}]")

        val resultado = repositorio.enviar(ANA, "  ¡Hola!  ")

        assertTrue(resultado is ResultadoRed.Correcto)
        val cuerpo = servidor.takeRequest().body.readUtf8()
        assertTrue(cuerpo.contains("\"destinatario_id\":\"$ANA\""))
        assertTrue("Se envía sin los espacios de los bordes", cuerpo.contains("\"texto\":\"¡Hola!\""))
        assertTrue(cuerpo.contains("\"zumbido\":false"))
        assertEquals(1, repositorio.conversacion(YO, ANA).first().size)
    }

    @Test
    fun unZumbidoSeEnviaMarcado() = runBlocking<Unit> {
        responder(201, "[${directo("d1", YO, ANA, "¡Zumbido!", zumbido = true)}]")

        repositorio.enviar(ANA, "¡Zumbido!", zumbido = true)

        assertTrue(servidor.takeRequest().body.readUtf8().contains("\"zumbido\":true"))
        assertTrue(repositorio.conversacion(YO, ANA).first().single().mensaje.zumbido)
    }

    @Test
    fun siNoSonAmigosElServidorNoDejaYNoSeGuardaNada() = runBlocking<Unit> {
        responder(403, """{"code":"42501","message":"new row violates row-level security policy"}""")

        val resultado = repositorio.enviar(BEA, "Hola")

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
        assertTrue(repositorio.conversacion(YO, BEA).first().isEmpty())
    }

    @Test
    fun sinConexionNoSeEnviaNada() = runBlocking<Unit> {
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        val sinRed = crearRepositorio(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", "clave"))

        assertEquals(ResultadoRed.SinConexion, sinRed.enviar(ANA, "Hola"))
        assertFalse(repositorio.conversaciones(YO).first().isNotEmpty())
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
        const val ANA = "6f1c2a1e-0000-4000-8000-00000000000a"
        const val BEA = "6f1c2a1e-0000-4000-8000-00000000000b"
    }
}
