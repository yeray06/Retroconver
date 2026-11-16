package com.retroconver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.retroconver.R
import com.retroconver.data.local.AmistadEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AmistadesService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant

/**
 * Amistades y solicitudes (EX-04, #129) contra un servidor falso. La sesión es de verdad
 * (DataStore en una carpeta temporal) y Room se sustituye por DAOs en memoria.
 */
class AmistadesRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var sesion: SesionRepository
    private val perfiles = UsuarioDaoEnMemoria()
    private val room = AmistadDaoEnMemoria(perfiles)
    private lateinit var repositorio: AmistadesRepository

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
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, sesion))
        val usuarios = UsuarioRepository(
            usuarioDao = perfiles,
            perfilService = retrofit.create(PerfilService::class.java),
            almacenamiento = retrofit.create(AlmacenamientoService::class.java),
            sesion = sesion,
            configuracion = configuracion,
        )
        repositorio = AmistadesRepository(room, retrofit.create(AmistadesService::class.java), usuarios, sesion)
        responder(200, """{"access_token":"acceso","refresh_token":"renovacion","expires_in":3600,"user":{"id":"$YO"}}""")
        sesion.iniciarSesion("yeray@retroconver.es", "secreta")
        servidor.takeRequest()
    }

    @After
    fun cerrar() {
        servidor.shutdown()
        ambitoDataStore.cancel()
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun perfil(id: String, nombre: String) =
        """{"id":"$id","nombre":"$nombre","biografia":"","estado":"","avatar_ruta":null,"creado_en":"2026-11-01T09:00:00+00:00"}"""

    private fun amistad(id: String, de: String, para: String, aceptada: Boolean, fecha: String = "2026-11-16T10:00:00+00:00") =
        """{"id":"$id","solicitante_id":"$de","destinatario_id":"$para","aceptada":$aceptada,"creada_en":"$fecha",
            "solicitante":${perfil(de, nombres.getValue(de))},"destinatario":${perfil(para, nombres.getValue(para))}}"""

    private val nombres = mapOf(YO to "Yeray", ANA to "Ana", BEA to "Bea", CRIS to "Cris")

    private suspend fun guardarEnRoom(vararg amistades: AmistadEntity) {
        nombres.forEach { (id, nombre) -> perfiles.guardar(UsuarioEntity(id = id, nombre = nombre, fechaAlta = Instant.EPOCH)) }
        amistades.forEach { room.guardar(it) }
    }

    // ---------- Descargar ----------

    @Test
    fun refrescarGuardaLosPerfilesLosAmigosYLasSolicitudes() = runBlocking<Unit> {
        responder(200, "[${amistad("am1", YO, ANA, aceptada = true)},${amistad("am2", BEA, YO, aceptada = false)}]")

        assertTrue(repositorio.refrescarMias() is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("(solicitante_id.eq.$YO,destinatario_id.eq.$YO)", peticion.requestUrl?.queryParameter("or"))
        assertTrue(peticion.requestUrl?.queryParameter("select")!!.contains("solicitante:perfiles!solicitante_id(*)"))
        assertEquals(listOf("Ana"), repositorio.misAmigos().first().map { it.nombre })
        assertEquals(listOf("Bea"), repositorio.solicitudesRecibidas().first().map { it.persona.nombre })
        assertEquals(emptyList<Any>(), repositorio.solicitudesEnviadas().first())
        assertEquals(setOf(YO, ANA), repositorio.circulo().first())
    }

    @Test
    fun alRefrescarDesaparecenLasQueSeHanBorradoEnElServidor() = runBlocking<Unit> {
        guardarEnRoom(AmistadEntity("vieja", YO, ANA, aceptada = true, fechaCreacion = Instant.EPOCH))
        responder(200, "[]")

        repositorio.refrescarMias()

        assertEquals(emptyList<Any>(), repositorio.misAmigos().first())
    }

    @Test
    fun losAmigosDeOtraPersonaSoloTraenLasAceptadas() = runBlocking<Unit> {
        responder(200, "[${amistad("am3", ANA, CRIS, aceptada = true)}]")

        repositorio.refrescarAmigosDe(ANA)

        val peticion = servidor.takeRequest()
        assertEquals("is.true", peticion.requestUrl?.queryParameter("aceptada"))
        assertEquals(listOf("Cris"), repositorio.amigosDe(ANA).first().map { it.nombre })
    }

    // ---------- Pedir, aceptar y borrar ----------

    @Test
    fun pedirAmistadEnviaElDestinatarioYQuedaPendiente() = runBlocking<Unit> {
        responder(201, "[${amistad("am4", YO, ANA, aceptada = false)}]")

        assertTrue(repositorio.pedirAmistad(ANA) is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("""{"destinatario_id":"$ANA"}""", peticion.body.readUtf8())
        assertEquals(RelacionAmistad.SolicitudEnviada("am4"), repositorio.relacionCon(ANA).first())
        assertEquals(listOf("Ana"), repositorio.solicitudesEnviadas().first().map { it.persona.nombre })
    }

    @Test
    fun aceptarUnaSolicitudLaConvierteEnAmistad() = runBlocking<Unit> {
        guardarEnRoom(AmistadEntity("am5", BEA, YO, aceptada = false, fechaCreacion = Instant.EPOCH))
        assertEquals(RelacionAmistad.SolicitudRecibida("am5"), repositorio.relacionCon(BEA).first())
        responder(200, "[${amistad("am5", BEA, YO, aceptada = true)}]")

        assertTrue(repositorio.aceptar("am5") is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("PATCH", peticion.method)
        assertEquals("eq.am5", peticion.requestUrl?.queryParameter("id"))
        assertEquals("""{"aceptada":true}""", peticion.body.readUtf8())
        assertEquals(RelacionAmistad.Amigos("am5"), repositorio.relacionCon(BEA).first())
    }

    @Test
    fun borrarQuitaLaAmistadAunqueElServidorYaNoLaTuviera() = runBlocking<Unit> {
        guardarEnRoom(AmistadEntity("am6", YO, ANA, aceptada = true, fechaCreacion = Instant.EPOCH))
        responder(200, "[]")

        assertTrue(repositorio.borrar("am6") is ResultadoRed.Correcto)

        assertEquals("DELETE", servidor.takeRequest().method)
        assertEquals(RelacionAmistad.Ninguna, repositorio.relacionCon(ANA).first())
    }

    @Test
    fun siYaHabiaUnaSolicitudSeDiceQueYaExiste() = runBlocking<Unit> {
        responder(409, """{"code":"23505","message":"duplicate key value violates unique constraint"}""")

        val resultado = repositorio.pedirAmistad(ANA)

        assertEquals(R.string.red_ya_existe, (resultado as ResultadoRed.Fallo).mensaje())
        assertEquals(RelacionAmistad.Ninguna, repositorio.relacionCon(ANA).first())
    }

    @Test
    fun siNoDevuelveLaFilaEsQueNoHayPermiso() = runBlocking<Unit> {
        responder(200, "[]")

        val resultado = repositorio.aceptar("de-otra-persona")

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
    }

    // ---------- Relación con otra persona ----------

    @Test
    fun laRelacionDependeDeQuienPidioYDeSiSeHaAceptado() {
        val pedidaPorMi = AmistadEntity("a", YO, ANA, aceptada = false, fechaCreacion = Instant.EPOCH)
        assertEquals(RelacionAmistad.EsMiPerfil, relacionEntre(YO, YO, null))
        assertEquals(RelacionAmistad.Ninguna, relacionEntre(YO, ANA, null))
        assertEquals(RelacionAmistad.Ninguna, relacionEntre(null, ANA, pedidaPorMi))
        assertEquals(RelacionAmistad.SolicitudEnviada("a"), relacionEntre(YO, ANA, pedidaPorMi))
        assertEquals(RelacionAmistad.SolicitudRecibida("a"), relacionEntre(ANA, YO, pedidaPorMi))
        assertEquals(RelacionAmistad.Amigos("a"), relacionEntre(ANA, YO, pedidaPorMi.copy(aceptada = true)))
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
        const val ANA = "6f1c2a1e-0000-4000-8000-000000000002"
        const val BEA = "6f1c2a1e-0000-4000-8000-000000000003"
        const val CRIS = "6f1c2a1e-0000-4000-8000-000000000004"
    }
}
