package com.retroconver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PerfilDto
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
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
 * Perfil guardado en el servidor (E4-05.1 y E4-05.2, #121 y #122) contra un servidor falso.
 * La sesión es de verdad (DataStore en una carpeta temporal) y Room se sustituye por un
 * [UsuarioDao] en memoria.
 */
class UsuarioRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var sesion: SesionRepository
    private val room = UsuarioDaoEnMemoria()
    private lateinit var repositorio: UsuarioRepository

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
        // Se entra una vez y se retira esa petición
        responder(200, """{"access_token":"acceso","refresh_token":"renovacion","expires_in":3600,"user":{"id":"$YO"}}""")
        sesion.iniciarSesion("yeray@retroconver.es", "secreta")
        servidor.takeRequest()
    }

    @After
    fun cerrar() {
        servidor.shutdown()
        ambitoDataStore.cancel()
    }

    private fun crearRepositorio(configuracion: ConfiguracionSupabase): UsuarioRepository {
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, sesion))
        return UsuarioRepository(
            usuarioDao = room,
            perfilService = retrofit.create(PerfilService::class.java),
            almacenamiento = retrofit.create(AlmacenamientoService::class.java),
            sesion = sesion,
            configuracion = configuracion,
        )
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun perfil(nombre: String = "Yeray", avatar: String? = null, estado: String = "") =
        """{"id":"$YO","nombre":"$nombre","biografia":"","estado":"$estado",
            "avatar_ruta":${avatar?.let { "\"$it\"" } ?: "null"},"creado_en":"2026-11-11T09:00:00+00:00"}"""

    private fun imagen(): File = carpeta.newFile("avatar.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

    private fun RecordedRequest.ruta() = requestUrl?.encodedPath

    // ---------- Leer ----------

    @Test
    fun elUsuarioActualEsElDeLaSesion() = runBlocking<Unit> {
        assertEquals(YO, repositorio.idActual())
        assertNull(repositorio.usuarioActual().first())

        responder(200, "[${perfil()}]")
        assertTrue(repositorio.refrescarActual() is ResultadoRed.Correcto)

        assertEquals("Yeray", repositorio.usuarioActual().first()?.nombre)
        assertEquals("eq.$YO", servidor.takeRequest().requestUrl?.queryParameter("id"))
    }

    // ---------- Editar el perfil ----------

    @Test
    fun editarElPerfilLoGuardaEnElServidorYDespuesEnRoom() = runBlocking<Unit> {
        responder(200, "[${perfil(estado = "Estudiando")}]")

        val resultado = repositorio.actualizarPerfil("Yeray", "", "Estudiando")

        assertTrue(resultado is ResultadoRed.Correcto)
        val peticion = servidor.takeRequest()
        assertEquals("PATCH", peticion.method)
        assertEquals("eq.$YO", peticion.requestUrl?.queryParameter("id"))
        assertEquals("return=representation", peticion.getHeader("Prefer"))
        assertTrue(peticion.body.readUtf8().contains("\"estado\":\"Estudiando\""))
        assertEquals("Estudiando", room.perfiles.value[YO]?.estado)
    }

    @Test
    fun sinConexionNoSeCambiaNada() = runBlocking<Unit> {
        room.guardar(UsuarioEntity(id = YO, nombre = "Yeray", fechaAlta = Instant.EPOCH))
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        val sinRed = crearRepositorio(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", "clave"))

        val resultado = sinRed.actualizarPerfil("Otro nombre", "", "")

        assertEquals(ResultadoRed.SinConexion, resultado)
        assertEquals("Yeray", room.perfiles.value[YO]?.nombre)
    }

    @Test
    fun siElServidorNoDevuelveLaFilaEsQueNoHayPermiso() = runBlocking<Unit> {
        responder(200, "[]")

        val resultado = repositorio.actualizarPerfil("Yeray", "", "")

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
    }

    // ---------- Avatar ----------

    @Test
    fun cambiarElAvatarSubeLaImagenApuntaElPerfilYBorraLaAnterior() = runBlocking<Unit> {
        responder(200, "[${perfil(avatar = "$YO/anterior.jpg")}]") // perfil actual
        responder(200, """{"Key":"imagenes/$YO/nueva.jpg"}""") // subida
        responder(200, "[${perfil(avatar = "$YO/NUEVA")}]") // perfil apuntando a la nueva
        responder(200, """{"message":"Successfully deleted"}""") // borrar la anterior

        val resultado = repositorio.cambiarAvatar(imagen())

        assertTrue(resultado is ResultadoRed.Correcto)
        assertEquals("GET", servidor.takeRequest().method)
        val subida = servidor.takeRequest()
        assertEquals("POST", subida.method)
        val rutaNueva = subida.ruta()!!.removePrefix("/storage/v1/object/imagenes/")
        assertTrue("Va a la carpeta de la cuenta", rutaNueva.startsWith("$YO/") && rutaNueva.endsWith(".jpg"))
        assertEquals("image/jpeg", subida.getHeader("Content-Type"))
        val cambio = servidor.takeRequest()
        assertEquals("PATCH", cambio.method)
        assertTrue(cambio.body.readUtf8().contains("\"avatar_ruta\":\"$rutaNueva\""))
        val borrado = servidor.takeRequest()
        assertEquals("DELETE", borrado.method)
        assertEquals("/storage/v1/object/imagenes/$YO/anterior.jpg", borrado.ruta())
        // Room guarda lo que ha devuelto el servidor, con su URL pública
        assertTrue(room.perfiles.value[YO]?.avatarUri!!.endsWith("/storage/v1/object/public/imagenes/$YO/NUEVA"))
    }

    @Test
    fun siFallaApuntarElPerfilSeBorraLaImagenRecienSubida() = runBlocking<Unit> {
        responder(200, "[${perfil(avatar = null)}]")
        responder(200, """{"Key":"imagenes/$YO/nueva.jpg"}""")
        responder(500)
        responder(200, """{"message":"Successfully deleted"}""")

        val resultado = repositorio.cambiarAvatar(imagen())

        assertEquals(500, (resultado as ResultadoRed.ErrorServidor).estadoHttp)
        servidor.takeRequest()
        val rutaSubida = servidor.takeRequest().ruta()
        servidor.takeRequest()
        val borrado = servidor.takeRequest()
        assertEquals("DELETE", borrado.method)
        assertEquals(rutaSubida, borrado.ruta())
        assertNull(room.perfiles.value[YO])
    }

    @Test
    fun siFallaLaSubidaNoSeTocaElPerfil() = runBlocking<Unit> {
        responder(200, "[${perfil()}]")
        responder(413, """{"statusCode":"413","error":"Payload too large","message":"The object exceeded the maximum allowed size"}""")

        val resultado = repositorio.cambiarAvatar(imagen())

        assertEquals(413, (resultado as ResultadoRed.ErrorServidor).estadoHttp)
        // Inicio de sesión, GET del perfil y la subida; ningún PATCH
        assertEquals(3, servidor.requestCount)
    }

    @Test
    fun losAutoresQueLleganConOtrasRespuestasSeGuardan() = runBlocking<Unit> {
        val dto = ClienteSupabase.json.decodeFromString(PerfilDto.serializer(), perfil(nombre = "Dani"))

        repositorio.guardarDelServidor(listOf(dto))

        assertEquals("Dani", room.perfiles.value[YO]?.nombre)
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
    }
}

/** Sustituto de Room para estas pruebas: los perfiles en un mapa que avisa de los cambios. */
private class UsuarioDaoEnMemoria : UsuarioDao {

    val perfiles = MutableStateFlow<Map<String, UsuarioEntity>>(emptyMap())

    override suspend fun guardar(usuario: UsuarioEntity) {
        perfiles.value = perfiles.value + (usuario.id to usuario)
    }

    override suspend fun guardarTodos(usuarios: List<UsuarioEntity>) = usuarios.forEach { guardar(it) }

    override fun observar(id: String): Flow<UsuarioEntity?> = perfiles.map { it[id] }

    override suspend fun obtener(id: String): UsuarioEntity? = perfiles.value[id]

    override suspend fun actualizarDatos(id: String, nombre: String, biografia: String, estado: String) {
        perfiles.value[id]?.let { guardar(it.copy(nombre = nombre, biografia = biografia, estado = estado)) }
    }

    override suspend fun actualizarAvatar(id: String, avatarRuta: String?, avatarUri: String?) {
        perfiles.value[id]?.let { guardar(it.copy(avatarRuta = avatarRuta, avatarUri = avatarUri)) }
    }
}
