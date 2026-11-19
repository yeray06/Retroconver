package com.retroconver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.retroconver.data.local.ComentarioEntity
import com.retroconver.data.local.PublicacionEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PublicacionService
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
 * Publicaciones y comentarios contra un servidor falso (E4-06 y E4-07, #123 y #18).
 * La sesión es de verdad (DataStore temporal) y Room se sustituye por mapas en memoria.
 */
class PublicacionRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer()
    private lateinit var ambitoDataStore: CoroutineScope
    private lateinit var sesion: SesionRepository
    private val perfiles = UsuarioDaoEnMemoria()
    private val comentariosRoom = ComentarioDaoEnMemoria(perfiles)
    private val publicacionesRoom = PublicacionDaoEnMemoria(perfiles, comentariosRoom)
    private lateinit var repositorio: PublicacionRepository

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

    private fun crearRepositorio(configuracion: ConfiguracionSupabase): PublicacionRepository {
        val retrofit = ClienteSupabase.crearRetrofit(configuracion.urlBase, ClienteSupabase.crearClienteHttp(configuracion, sesion))
        return PublicacionRepository(
            publicacionDao = publicacionesRoom,
            comentarioDao = comentariosRoom,
            usuarioDao = perfiles,
            servicio = retrofit.create(PublicacionService::class.java),
            almacenamiento = retrofit.create(AlmacenamientoService::class.java),
            sesion = sesion,
            configuracion = configuracion,
        )
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun perfilJson(id: String, nombre: String) =
        """{"id":"$id","nombre":"$nombre","creado_en":"2026-11-01T09:00:00+00:00"}"""

    private fun comentarioJson(id: String, publicacion: String, autor: String = ANA, nombre: String = "Ana", segundo: Int = 0) =
        """{"id":"$id","publicacion_id":"$publicacion","autor_id":"$autor","texto":"Comentario $id",
            "creado_en":"2026-11-12T10:00:${"%02d".format(segundo)}+00:00","autor":${perfilJson(autor, nombre)}}"""

    private fun publicacionJson(
        id: String,
        autor: String = ANA,
        nombre: String = "Ana",
        minuto: Int = 0,
        imagen: String? = null,
        comentarios: String = "",
    ) = """{"id":"$id","autor_id":"$autor","texto":"Texto $id",
        "imagen_ruta":${imagen?.let { "\"$it\"" } ?: "null"},
        "creada_en":"2026-11-12T10:${"%02d".format(minuto)}:00+00:00",
        "autor":${perfilJson(autor, nombre)},"comentarios":[$comentarios]}"""

    private fun imagen(nombre: String = "foto.jpg") =
        ImagenParaSubir(carpeta.newFile(nombre).apply { writeBytes(byteArrayOf(1, 2, 3)) }, 1280, 720)

    private fun RecordedRequest.ruta() = requestUrl?.encodedPath

    private suspend fun publicacionEnRoom(id: String, minuto: Int = 0, imagenRuta: String? = null): PublicacionEntity {
        perfiles.guardar(UsuarioEntity(id = YO, nombre = "Yeray", fechaAlta = Instant.EPOCH))
        val publicacion = PublicacionEntity(
            id = id,
            usuarioId = YO,
            texto = "Texto $id",
            imagenRuta = imagenRuta,
            imagenUri = imagenRuta?.let { "https://servidor/$it" },
            timestamp = Instant.parse("2026-11-12T10:${"%02d".format(minuto)}:00Z"),
        )
        publicacionesRoom.guardarUna(publicacion)
        return publicacion
    }

    // ---------- Descargar el muro ----------

    @Test
    fun refrescarDescargaLasPublicacionesDeTodasLasPersonasConSusAutores() = runBlocking<Unit> {
        responder(200, "[${publicacionJson("p2", ANA, "Ana", minuto = 2)},${publicacionJson("p1", YO, "Yeray", minuto = 1)}]")

        val resultado = repositorio.refrescar()

        assertTrue(resultado is ResultadoRed.Correcto)
        val peticion = servidor.takeRequest()
        assertEquals("/rest/v1/publicaciones", peticion.ruta())
        assertEquals("creada_en.desc", peticion.requestUrl?.queryParameter("order"))
        assertEquals(PublicacionRepository.LIMITE_MURO.toString(), peticion.requestUrl?.queryParameter("limit"))
        assertTrue(peticion.requestUrl?.queryParameter("select")!!.contains("autor:perfiles(*)"))
        assertEquals("Los autores se guardan antes por la clave foránea", "Ana", perfiles.perfiles.value[ANA]?.nombre)
        val muro = repositorio.publicaciones().first()
        assertEquals(listOf("p2", "p1"), muro.map { it.publicacion.id })
        assertEquals(listOf("Ana", "Yeray"), muro.map { it.autor.nombre })
    }

    @Test
    fun laImagenDeLaPublicacionSeConvierteEnSuUrlPublica() = runBlocking<Unit> {
        responder(200, "[${publicacionJson("p1", imagen = "$ANA/foto.jpg")}]")

        repositorio.refrescar()

        val publicacion = repositorio.publicacion("p1").first()!!.publicacion
        assertEquals("$ANA/foto.jpg", publicacion.imagenRuta)
        assertTrue(publicacion.imagenUri!!.endsWith("/storage/v1/object/public/imagenes/$ANA/foto.jpg"))
    }

    @Test
    fun elContadorDelMuroSalePorLosComentariosQueTrae() = runBlocking<Unit> {
        val comentarios = "${comentarioJson("c1", "p1", segundo = 1)},${comentarioJson("c2", "p1", YO, "Yeray", segundo = 2)}"
        responder(200, "[${publicacionJson("p1", comentarios = comentarios)}]")

        repositorio.refrescar()

        assertEquals(2, repositorio.publicaciones().first().single().numComentarios)
        assertEquals(setOf("Ana", "Yeray"), repositorio.comentarios("p1").first().map { it.autor.nombre }.toSet())
    }

    @Test
    fun refrescarQuitaLoQueSeHaBorradoEnElServidor() = runBlocking<Unit> {
        publicacionEnRoom("borrada", minuto = 5)
        comentariosRoom.insertar(ComentarioEntity("c-viejo", "p1", YO, "ya no está", Instant.EPOCH))
        publicacionEnRoom("p1", minuto = 6)
        responder(200, "[${publicacionJson("p1", minuto = 6)}]")

        repositorio.refrescar()

        assertEquals(listOf("p1"), repositorio.publicaciones().first().map { it.publicacion.id })
        assertEquals("También se quitan sus comentarios borrados", 0, repositorio.publicaciones().first().single().numComentarios)
    }

    @Test
    fun cuandoHayMasPublicacionesAntiguasNoSeDanPorBorradas() = runBlocking<Unit> {
        publicacionEnRoom("antigua", minuto = 0)
        // El servidor devuelve justo el límite: puede haber más antiguas que no se han pedido
        val filas = (1..PublicacionRepository.LIMITE_MURO).joinToString(",") { publicacionJson("n$it", minuto = it) }
        responder(200, "[$filas]")

        repositorio.refrescar()

        val ids = repositorio.publicaciones().first().map { it.publicacion.id }
        assertTrue("antigua" in ids)
        assertEquals(PublicacionRepository.LIMITE_MURO + 1, ids.size)
    }

    @Test
    fun siElMuroDelServidorEstaVacioSeVaciaTambienRoom() = runBlocking<Unit> {
        publicacionEnRoom("p1")
        responder(200, "[]")

        repositorio.refrescar()

        assertTrue(repositorio.publicaciones().first().isEmpty())
    }

    @Test
    fun sinConexionSeQuedaLaCopiaDeRoom() = runBlocking<Unit> {
        publicacionEnRoom("p1")
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        val sinRed = crearRepositorio(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", "clave"))

        assertEquals(ResultadoRed.SinConexion, sinRed.refrescar())
        assertEquals(1, repositorio.publicaciones().first().size)
    }

    @Test
    fun refrescarPublicacionDescargaLaPublicacionConSusComentarios() = runBlocking<Unit> {
        val comentarios = "${comentarioJson("c1", "p1", segundo = 1)},${comentarioJson("c2", "p1", YO, "Yeray", segundo = 2)}"
        responder(200, "[${publicacionJson("p1", comentarios = comentarios)}]")

        val resultado = repositorio.refrescarPublicacion("p1")

        assertTrue(resultado is ResultadoRed.Correcto)
        assertEquals("eq.p1", servidor.takeRequest().requestUrl?.queryParameter("id"))
        assertEquals("Texto p1", repositorio.publicacion("p1").first()!!.publicacion.texto)
        assertEquals(listOf("c2", "c1"), repositorio.comentarios("p1").first().map { it.comentario.id })
    }

    @Test
    fun siUnaPublicacionYaNoExisteSeQuitaDeRoom() = runBlocking<Unit> {
        publicacionEnRoom("p1")
        responder(200, "[]")

        val resultado = repositorio.refrescarPublicacion("p1")

        assertEquals(404, (resultado as ResultadoRed.ErrorServidor).estadoHttp)
        assertNull(repositorio.publicacion("p1").first())
    }

    // ---------- Publicar ----------

    @Test
    fun publicarSoloTextoLoGuardaEnElServidorYEnRoom() = runBlocking<Unit> {
        responder(201, "[${publicacionJson("nueva", YO, "Yeray")}]")

        val resultado = repositorio.publicar("Hola, red")

        assertEquals(ResultadoRed.Correcto("nueva"), resultado)
        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/rest/v1/publicaciones", peticion.ruta())
        assertEquals("return=representation", peticion.getHeader("Prefer"))
        val cuerpo = peticion.body.readUtf8()
        assertTrue(cuerpo.contains("\"texto\":\"Hola, red\""))
        assertTrue("Sin imagen va a null", cuerpo.contains("\"imagen_ruta\":null"))
        assertFalse("El autor lo pone el servidor", cuerpo.contains("autor_id"))
        assertEquals(1, servidor.requestCount - 1)
        assertEquals("nueva", repositorio.publicaciones().first().single().publicacion.id)
    }

    @Test
    fun publicarConImagenLaSubeASuCarpetaYGuardaSuRuta() = runBlocking<Unit> {
        responder(200, """{"Key":"imagenes/$YO/nueva.jpg"}""")
        responder(201, "[${publicacionJson("nueva", YO, "Yeray", imagen = "$YO/nueva.jpg")}]")

        val resultado = repositorio.publicar("Con foto", imagen())

        assertTrue(resultado is ResultadoRed.Correcto)
        val subida = servidor.takeRequest()
        assertEquals("POST", subida.method)
        assertTrue(subida.ruta()!!.startsWith("/storage/v1/object/imagenes/$YO/"))
        assertTrue(subida.ruta()!!.endsWith(".jpg"))
        val rutaSubida = subida.ruta()!!.removePrefix("/storage/v1/object/imagenes/")
        val fila = servidor.takeRequest().body.readUtf8()
        assertTrue(fila.contains("\"imagen_ruta\":\"$rutaSubida\""))
    }

    @Test
    fun siFallaLaSubidaNoSeCreaLaPublicacion() = runBlocking<Unit> {
        responder(413, """{"statusCode":"413","error":"Payload too large","message":"The object exceeded the maximum allowed size"}""")

        val resultado = repositorio.publicar("Con foto", imagen())

        assertEquals(413, (resultado as ResultadoRed.ErrorServidor).estadoHttp)
        assertEquals("Solo se ha intentado la subida", 2, servidor.requestCount)
        assertTrue(repositorio.publicaciones().first().isEmpty())
    }

    @Test
    fun siNoSePuedeCrearLaFilaSeBorraLaImagenSubida() = runBlocking<Unit> {
        responder(200, """{"Key":"imagenes/$YO/nueva.jpg"}""")
        responder(400, """{"code":"23514","message":"new row violates check constraint \"publicaciones_texto_check\""}""")
        responder(200, """{"message":"Successfully deleted"}""")

        val resultado = repositorio.publicar("x", imagen())

        assertEquals("23514", (resultado as ResultadoRed.ErrorServidor).codigo)
        val rutaSubida = servidor.takeRequest().ruta()
        servidor.takeRequest()
        val borrado = servidor.takeRequest()
        assertEquals("DELETE", borrado.method)
        assertEquals(rutaSubida, borrado.ruta())
        assertTrue(repositorio.publicaciones().first().isEmpty())
    }

    @Test
    fun sinConexionNoSePublicaNiSeGuardaNada() = runBlocking<Unit> {
        val puertoCerrado = ServerSocket(0).use { it.localPort }
        val sinRed = crearRepositorio(ConfiguracionSupabase("http://127.0.0.1:$puertoCerrado", "clave"))

        assertEquals(ResultadoRed.SinConexion, sinRed.publicar("Hola"))
        assertTrue(repositorio.publicaciones().first().isEmpty())
    }

    // ---------- Comentarios ----------

    @Test
    fun comentarLoGuardaEnElServidorYElContadorSubeSolo() = runBlocking<Unit> {
        publicacionEnRoom("p1")
        responder(201, "[${comentarioJson("c1", "p1", YO, "Yeray")}]")

        val resultado = repositorio.comentar("p1", "Qué bueno")

        assertEquals(ResultadoRed.Correcto("c1"), resultado)
        val peticion = servidor.takeRequest()
        assertEquals("/rest/v1/comentarios", peticion.ruta())
        val cuerpo = peticion.body.readUtf8()
        assertTrue(cuerpo.contains("\"publicacion_id\":\"p1\""))
        assertTrue(cuerpo.contains("\"texto\":\"Qué bueno\""))
        assertEquals(1, repositorio.publicacion("p1").first()!!.numComentarios)
    }

    // ---------- Borrar ----------

    @Test
    fun borrarUnaPublicacionBorraSuFilaSuImagenYSuCopia() = runBlocking<Unit> {
        val publicacion = publicacionEnRoom("p1", imagenRuta = "$YO/foto.jpg")
        responder(200, "[${publicacionJson("p1", YO, "Yeray", imagen = "$YO/foto.jpg")}]")
        responder(200, """{"message":"Successfully deleted"}""")

        val resultado = repositorio.eliminar(publicacion)

        assertTrue(resultado is ResultadoRed.Correcto)
        val fila = servidor.takeRequest()
        assertEquals("DELETE", fila.method)
        assertEquals("/rest/v1/publicaciones", fila.ruta())
        assertEquals("eq.p1", fila.requestUrl?.queryParameter("id"))
        assertEquals("/storage/v1/object/imagenes/$YO/foto.jpg", servidor.takeRequest().ruta())
        assertTrue(repositorio.publicaciones().first().isEmpty())
    }

    @Test
    fun noSePuedeBorrarUnaPublicacionAjena() = runBlocking<Unit> {
        val publicacion = publicacionEnRoom("p1", imagenRuta = "$YO/foto.jpg")
        responder(200, "[]") // la política de seguridad no deja borrarla: no devuelve ninguna fila

        val resultado = repositorio.eliminar(publicacion)

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals("No se intenta borrar la imagen", 2, servidor.requestCount)
        assertFalse(repositorio.publicaciones().first().isEmpty())
    }

    @Test
    fun borrarUnComentarioPropioLoBorraDelServidorYDeRoom() = runBlocking<Unit> {
        publicacionEnRoom("p1")
        comentariosRoom.insertar(ComentarioEntity("c1", "p1", YO, "mío", Instant.EPOCH))
        responder(200, "[${comentarioJson("c1", "p1", YO, "Yeray")}]")

        val resultado = repositorio.eliminarComentario("c1")

        assertTrue(resultado is ResultadoRed.Correcto)
        val peticion = servidor.takeRequest()
        assertEquals("DELETE", peticion.method)
        assertEquals("/rest/v1/comentarios", peticion.ruta())
        assertEquals("eq.c1", peticion.requestUrl?.queryParameter("id"))
        assertEquals(0, repositorio.publicacion("p1").first()!!.numComentarios)
    }

    @Test
    fun noSePuedeBorrarUnComentarioAjeno() = runBlocking<Unit> {
        publicacionEnRoom("p1")
        comentariosRoom.insertar(ComentarioEntity("c1", "p1", ANA, "de Ana", Instant.EPOCH))
        responder(200, "[]") // la política de seguridad no deja borrarlo: no devuelve ninguna fila

        val resultado = repositorio.eliminarComentario("c1")

        assertEquals("42501", (resultado as ResultadoRed.ErrorServidor).codigo)
        assertEquals(1, repositorio.publicacion("p1").first()!!.numComentarios)
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
        const val ANA = "6f1c2a1e-0000-4000-8000-00000000000a"
    }
}
