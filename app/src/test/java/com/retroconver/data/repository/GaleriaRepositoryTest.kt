package com.retroconver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.GaleriaService
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
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Álbumes y fotos contra un servidor falso (E4-10.1 y E4-10.4, #68 y #71).
 * La sesión es de verdad (DataStore temporal) y Room se sustituye por mapas en memoria.
 */
class GaleriaRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var sesion: SesionRepository
    private val galeria = GaleriaDaoEnMemoria()
    private val perfiles = UsuarioDaoEnMemoria()
    private lateinit var repositorio: GaleriaRepository

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

    private fun crearRepositorio(configuracion: ConfiguracionSupabase): GaleriaRepository {
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, sesion))
        return GaleriaRepository(
            galeriaDao = galeria,
            usuarioDao = perfiles,
            servicio = retrofit.create(GaleriaService::class.java),
            almacenamiento = retrofit.create(AlmacenamientoService::class.java),
            sesion = sesion,
            configuracion = configuracion,
        )
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private val perfilJson = """{"id":"$YO","nombre":"Yeray","creado_en":"2026-11-01T09:00:00+00:00"}"""

    private fun fotoJson(id: String, album: String = ALBUM, segundo: Int = 0) =
        """{"id":"$id","album_id":"$album","propietario_id":"$YO","ruta":"$YO/$id.jpg","ancho":1280,"alto":720,
            "creada_en":"2026-11-11T10:00:${"%02d".format(segundo)}+00:00"}"""

    private fun albumJson(fotos: String = "") =
        """{"id":"$ALBUM","propietario_id":"$YO","titulo":"Verano 2008","descripcion":"",
            "creado_en":"2026-11-11T09:00:00+00:00","propietario":$perfilJson,"fotos":[$fotos]}"""

    private fun imagen() = ImagenParaSubir(carpeta.newFile("foto.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }, 1280, 720)

    private fun RecordedRequest.ruta() = requestUrl?.encodedPath

    /** Un álbum propio con una foto, ya en Room. */
    private suspend fun albumConUnaFoto(): FotoEntity {
        galeria.guardarAlbum(AlbumEntity(ALBUM, YO, "Verano 2008", fechaCreacion = Instant.EPOCH))
        val foto = FotoEntity("f1", ALBUM, YO, "$YO/f1.jpg", "https://servidor/$YO/f1.jpg", fechaAnadida = Instant.EPOCH)
        galeria.guardarFoto(foto)
        return foto
    }

    // ---------- Descargar ----------

    @Test
    fun refrescarDescargaLosAlbumesConSuDuenoYSusFotos() = runBlocking<Unit> {
        responder(200, "[${albumJson(fotos = "${fotoJson("f1", segundo = 1)},${fotoJson("f2", segundo = 2)}")}]")

        val resultado = repositorio.refrescarAlbumesDe(YO)

        assertTrue(resultado is ResultadoRed.Correcto)
        val peticion = servidor.takeRequest()
        assertEquals("/rest/v1/albumes", peticion.ruta())
        assertEquals("eq.$YO", peticion.requestUrl?.queryParameter("propietario_id"))
        assertTrue(peticion.requestUrl?.queryParameter("select")!!.contains("fotos(*)"))
        assertEquals("El dueño se guarda antes por la clave foránea", "Yeray", perfiles.perfiles.value[YO]?.nombre)
        val lista = repositorio.albumesDe(YO).first().single()
        assertEquals("Verano 2008", lista.album.titulo)
        assertEquals(2, lista.numFotos)
        assertTrue(lista.portadaUri!!.endsWith("/storage/v1/object/public/imagenes/$YO/f2.jpg"))
    }

    @Test
    fun refrescarQuitaLoQueSeHaBorradoEnElServidor() = runBlocking<Unit> {
        galeria.guardarAlbum(AlbumEntity("borrado", YO, "Ya no está", fechaCreacion = Instant.EPOCH))
        responder(200, "[${albumJson()}]")

        repositorio.refrescarAlbumesDe(YO)

        assertEquals(listOf(ALBUM), repositorio.albumesDe(YO).first().map { it.album.id })
    }

    @Test
    fun siUnAlbumYaNoExisteSeQuitaDeRoom() = runBlocking<Unit> {
        albumConUnaFoto()
        responder(200, "[]")

        val resultado = repositorio.refrescarAlbum(ALBUM)

        assertEquals(404, (resultado as ResultadoRed.ErrorServidor).estadoHttp)
        assertNull(repositorio.album(ALBUM).first())
    }

    @Test
    fun sinConexionSeQuedaLaCopiaDeRoom() = runBlocking<Unit> {
        albumConUnaFoto()
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        val sinRed = crearRepositorio(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", "clave"))

        assertEquals(ResultadoRed.SinConexion, sinRed.refrescarAlbumesDe(YO))
        assertEquals(1, repositorio.albumesDe(YO).first().single().numFotos)
    }

    // ---------- Crear un álbum ----------

    @Test
    fun crearUnAlbumLoGuardaEnElServidorYEnRoom() = runBlocking<Unit> {
        responder(201, "[${albumJson()}]")

        val resultado = repositorio.crearAlbum("Verano 2008")

        assertEquals(ResultadoRed.Correcto(ALBUM), resultado)
        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("return=representation", peticion.getHeader("Prefer"))
        assertTrue(peticion.body.readUtf8().contains("\"titulo\":\"Verano 2008\""))
        assertEquals("Verano 2008", repositorio.album(ALBUM).first()?.titulo)
    }

    // ---------- Fotos ----------

    @Test
    fun subirUnaFotoLaSubeASuCarpetaYCreaSuFila() = runBlocking<Unit> {
        albumConUnaFoto()
        responder(200, """{"Key":"imagenes/$YO/nueva.jpg"}""")
        responder(201, "[${fotoJson("f2", segundo = 5)}]")

        val resultado = repositorio.subirFoto(ALBUM, imagen())

        assertTrue(resultado is ResultadoRed.Correcto)
        val subida = servidor.takeRequest()
        assertTrue(subida.ruta()!!.startsWith("/storage/v1/object/imagenes/$YO/"))
        val rutaSubida = subida.ruta()!!.removePrefix("/storage/v1/object/imagenes/")
        val fila = servidor.takeRequest().body.readUtf8()
        assertTrue(fila.contains("\"album_id\":\"$ALBUM\""))
        assertTrue(fila.contains("\"ruta\":\"$rutaSubida\""))
        assertTrue(fila.contains("\"ancho\":1280"))
        assertEquals(2, repositorio.fotos(ALBUM).first().size)
    }

    @Test
    fun siNoSePuedeCrearLaFilaSeBorraLaImagenSubida() = runBlocking<Unit> {
        albumConUnaFoto()
        responder(200, """{"Key":"imagenes/$YO/nueva.jpg"}""")
        responder(403, """{"code":"42501","message":"new row violates row-level security policy for table \"fotos\""}""")
        responder(200, """{"message":"Successfully deleted"}""")

        val resultado = repositorio.subirFoto(ALBUM, imagen())

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
        val rutaSubida = servidor.takeRequest().ruta()
        servidor.takeRequest()
        val borrado = servidor.takeRequest()
        assertEquals("DELETE", borrado.method)
        assertEquals(rutaSubida, borrado.ruta())
        assertEquals(1, repositorio.fotos(ALBUM).first().size)
    }

    @Test
    fun borrarUnaFotoBorraSuFilaSuArchivoYSuCopia() = runBlocking<Unit> {
        val foto = albumConUnaFoto()
        responder(200, "[${fotoJson("f1")}]")
        responder(200, """{"message":"Successfully deleted"}""")

        val resultado = repositorio.borrarFoto(foto)

        assertTrue(resultado is ResultadoRed.Correcto)
        val fila = servidor.takeRequest()
        assertEquals("DELETE", fila.method)
        assertEquals("/rest/v1/fotos", fila.ruta())
        assertEquals("eq.f1", fila.requestUrl?.queryParameter("id"))
        val archivo = servidor.takeRequest()
        assertEquals("DELETE", archivo.method)
        assertEquals("/storage/v1/object/imagenes/$YO/f1.jpg", archivo.ruta())
        assertTrue(repositorio.fotos(ALBUM).first().isEmpty())
    }

    @Test
    fun noSePuedeBorrarUnaFotoAjena() = runBlocking<Unit> {
        val foto = albumConUnaFoto()
        responder(200, "[]") // la política de seguridad no deja borrarla: no devuelve ninguna fila

        val resultado = repositorio.borrarFoto(foto)

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals("No se intenta borrar el archivo", 2, servidor.requestCount)
        assertFalse(repositorio.fotos(ALBUM).first().isEmpty())
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
        const val ALBUM = "a1b2c3d4-0000-4000-8000-000000000001"
    }
}
