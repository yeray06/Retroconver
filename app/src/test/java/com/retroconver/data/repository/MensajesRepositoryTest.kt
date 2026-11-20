package com.retroconver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.retroconver.data.local.MensajeEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.MensajesService
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.ServerSocket
import java.time.Instant

/**
 * Tablón de mensajes (E4-09, #72) contra un servidor falso. La sesión es de verdad (DataStore
 * en una carpeta temporal) y Room se sustituye por DAOs en memoria.
 */
class MensajesRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var sesion: SesionRepository
    private val perfiles = UsuarioDaoEnMemoria()
    private val room = MensajeDaoEnMemoria(perfiles)
    private lateinit var repositorio: MensajesRepository

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

    private fun crearRepositorio(configuracion: ConfiguracionSupabase): MensajesRepository {
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, sesion))
        val usuarios = UsuarioRepository(
            usuarioDao = perfiles,
            perfilService = retrofit.create(PerfilService::class.java),
            almacenamiento = retrofit.create(AlmacenamientoService::class.java),
            sesion = sesion,
            configuracion = configuracion,
        )
        return MensajesRepository(room, retrofit.create(MensajesService::class.java), usuarios)
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private val nombres = mapOf(YO to "Yeray", ANA to "Ana", BEA to "Bea")

    private fun perfil(id: String) =
        """{"id":"$id","nombre":"${nombres.getValue(id)}","biografia":"","estado":"","avatar_ruta":null,"creado_en":"2026-11-01T09:00:00+00:00"}"""

    private fun mensaje(id: String, en: String, de: String, texto: String, fecha: String = "2026-11-19T10:00:00+00:00") =
        """{"id":"$id","perfil_id":"$en","autor_id":"$de","texto":"$texto","creado_en":"$fecha",
            "autor":${perfil(de)},"perfil":${perfil(en)}}"""

    private suspend fun guardarEnRoom(vararg mensajes: MensajeEntity) {
        nombres.forEach { (id, nombre) -> perfiles.guardar(UsuarioEntity(id = id, nombre = nombre, fechaAlta = Instant.EPOCH)) }
        mensajes.forEach { room.guardar(it) }
    }

    @Test
    fun refrescarPideElTablonDeEsePerfilYGuardaLosMensajesConSusAutores() = runBlocking<Unit> {
        responder(
            200,
            "[${mensaje("m2", YO, BEA, "¿Quedamos?", "2026-11-19T11:00:00+00:00")},${mensaje("m1", YO, ANA, "¡Hola!")}]",
        )

        assertTrue(repositorio.refrescar(YO) is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("eq.$YO", peticion.requestUrl?.queryParameter("perfil_id"))
        assertEquals("creado_en.desc", peticion.requestUrl?.queryParameter("order"))
        assertTrue(peticion.requestUrl?.queryParameter("select")!!.contains("autor:perfiles!autor_id(*)"))
        val tablon = repositorio.tablon(YO).first()
        assertEquals(listOf("¿Quedamos?", "¡Hola!"), tablon.map { it.mensaje.texto })
        assertEquals(listOf("Bea", "Ana"), tablon.map { it.autor.nombre })
    }

    @Test
    fun alRefrescarDesaparecenLosBorradosSinTocarOtrosTablones() = runBlocking<Unit> {
        guardarEnRoom(
            MensajeEntity("viejo", YO, ANA, "Borrado desde otro móvil", Instant.EPOCH),
            MensajeEntity("ajeno", ANA, YO, "En el tablón de Ana", Instant.EPOCH),
        )
        responder(200, "[]")

        repositorio.refrescar(YO)

        assertEquals(emptyList<Any>(), repositorio.tablon(YO).first())
        assertEquals(listOf("ajeno"), repositorio.tablon(ANA).first().map { it.mensaje.id })
    }

    @Test
    fun escribirEnviaElTablonYElTextoSinEspaciosYLoGuarda() = runBlocking<Unit> {
        responder(201, "[${mensaje("m3", ANA, YO, "Feliz cumple")}]")

        assertTrue(repositorio.escribir(ANA, "  Feliz cumple  ") is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("""{"perfil_id":"$ANA","texto":"Feliz cumple"}""", peticion.body.readUtf8())
        assertEquals(listOf("Feliz cumple"), repositorio.tablon(ANA).first().map { it.mensaje.texto })
    }

    @Test
    fun siNoDevuelveLaFilaEsQueNoHayPermiso() = runBlocking<Unit> {
        responder(201, "[]")

        val resultado = repositorio.escribir(ANA, "Hola")

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals(emptyList<Any>(), repositorio.tablon(ANA).first())
    }

    @Test
    fun borrarLoQuitaDelServidorYDeRoom() = runBlocking<Unit> {
        guardarEnRoom(MensajeEntity("m4", YO, ANA, "Hola", Instant.EPOCH))
        responder(200, "[${mensaje("m4", YO, ANA, "Hola")}]")

        assertTrue(repositorio.borrar("m4") is ResultadoRed.Correcto)

        val peticion = servidor.takeRequest()
        assertEquals("DELETE", peticion.method)
        assertEquals("eq.m4", peticion.requestUrl?.queryParameter("id"))
        assertEquals(emptyList<Any>(), repositorio.tablon(YO).first())
    }

    @Test
    fun sinConexionSeQuedaLaCopiaDeRoom() = runBlocking<Unit> {
        guardarEnRoom(MensajeEntity("m5", YO, ANA, "Sigo aquí", Instant.EPOCH))
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        val sinRed = crearRepositorio(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", "clave"))

        assertEquals(ResultadoRed.SinConexion, sinRed.refrescar(YO))
        assertEquals(listOf("Sigo aquí"), repositorio.tablon(YO).first().map { it.mensaje.texto })
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
        const val ANA = "6f1c2a1e-0000-4000-8000-000000000002"
        const val BEA = "6f1c2a1e-0000-4000-8000-000000000003"
    }
}
