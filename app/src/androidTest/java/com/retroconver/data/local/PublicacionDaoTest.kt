package com.retroconver.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Pruebas con una base de datos en memoria: orden por fecha, comentarios, borrado en cascada
 * y lo que llega del servidor (ids UUID, `@Upsert` y sincronización del muro, E4-05.1 y E4-06, #121 y #123).
 */
@RunWith(AndroidJUnit4::class)
class PublicacionDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var publicaciones: PublicacionDao
    private lateinit var comentarios: ComentarioDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        publicaciones = baseDeDatos.publicacionDao()
        comentarios = baseDeDatos.comentarioDao()
        runBlocking {
            baseDeDatos.usuarioDao().guardar(UsuarioEntity(id = ANA, nombre = "Ana", fechaAlta = Instant.EPOCH))
        }
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    private fun publicacion(id: String, texto: String = id, ms: Long = 1_000) =
        PublicacionEntity(id = id, usuarioId = ANA, texto = texto, timestamp = Instant.ofEpochMilli(ms))

    private fun comentario(id: String, publicacionId: String, ms: Long = 1_000, texto: String = id) =
        ComentarioEntity(id = id, publicacionId = publicacionId, usuarioId = ANA, texto = texto, timestamp = Instant.ofEpochMilli(ms))

    private suspend fun ids() = publicaciones.observarTodas(ANA).first().map { it.publicacion.id }

    @Test
    fun lasPublicacionesSalenDeMasRecienteAMasAntigua() = runBlocking {
        publicaciones.insertar(publicacion("a", "primera", ms = 1_000))
        publicaciones.insertar(publicacion("b", "tercera", ms = 3_000))
        publicaciones.insertar(publicacion("c", "segunda", ms = 2_000))

        val textos = publicaciones.observarTodas(ANA).first().map { it.publicacion.texto }

        assertEquals(listOf("tercera", "segunda", "primera"), textos)
    }

    @Test
    fun laPublicacionTraeSuAutorYSuNumeroDeComentarios() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        comentarios.insertar(comentario("c1", "p1"))
        comentarios.insertar(comentario("c2", "p1"))

        val publicacion = publicaciones.observarPorId("p1", ANA).first()!!

        assertEquals("Ana", publicacion.autor.nombre)
        assertEquals(2, publicacion.numComentarios)
    }

    @Test
    fun losComentariosSalenDelMasRecienteAlMasAntiguo() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        comentarios.insertar(comentario("c1", "p1", ms = 1_000, texto = "antiguo"))
        comentarios.insertar(comentario("c2", "p1", ms = 2_000, texto = "nuevo"))

        val textos = comentarios.observarDePublicacion("p1").first().map { it.comentario.texto }

        assertEquals(listOf("nuevo", "antiguo"), textos)
    }

    @Test
    fun alBorrarUnaPublicacionSeBorranSusComentarios() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        comentarios.insertar(comentario("c1", "p1"))

        publicaciones.eliminar("p1")

        assertNull(publicaciones.observarPorId("p1", ANA).first())
        assertEquals(emptyList<ComentarioConAutor>(), comentarios.observarDePublicacion("p1").first())
    }

    @Test
    fun alBorrarUnUsuarioSeBorranSusPublicaciones() = runBlocking {
        publicaciones.insertar(publicacion("p1"))

        baseDeDatos.openHelper.writableDatabase.execSQL("DELETE FROM usuarios WHERE id = '$ANA'")

        assertEquals(emptyList<PublicacionConAutor>(), publicaciones.observarTodas(ANA).first())
    }

    @Test
    fun loQueLlegaDelServidorSeGuardaSinDuplicarNiBorrarComentarios() = runBlocking {
        publicaciones.guardar(listOf(publicacionDelServidor("primer texto")))
        comentarios.insertar(comentario("c1", DEL_SERVIDOR))

        // Al refrescar el muro llega otra vez la misma publicación, editada
        publicaciones.guardar(listOf(publicacionDelServidor("texto editado")))

        val muro = publicaciones.observarTodas(ANA).first()
        assertEquals(1, muro.size)
        assertEquals("texto editado", muro.single().publicacion.texto)
        assertEquals(1, muro.single().numComentarios)
    }

    @Test
    fun losComentariosDelServidorTambienSeActualizan() = runBlocking {
        publicaciones.guardar(listOf(publicacionDelServidor("hola")))
        val original = comentario("c1", DEL_SERVIDOR, texto = "antes")
        comentarios.guardar(listOf(original))
        comentarios.guardar(listOf(original.copy(texto = "después")))

        assertEquals(listOf("después"), comentarios.observarDePublicacion(DEL_SERVIDOR).first().map { it.comentario.texto })
    }

    @Test
    fun sincronizarQuitaLasPublicacionesBorradasEnElServidor() = runBlocking {
        publicaciones.insertar(publicacion("borrada", ms = 2_000))
        publicaciones.insertar(publicacion("sigue", ms = 3_000))

        publicaciones.sincronizar(listOf(publicacion("sigue", ms = 3_000), publicacion("nueva", ms = 4_000)), hayMasAntiguas = false)

        assertEquals(listOf("nueva", "sigue"), ids())
    }

    @Test
    fun sincronizarConMasAntiguasSoloQuitaDesdeLaMasAntiguaDescargada() = runBlocking {
        publicaciones.insertar(publicacion("muy-antigua", ms = 1_000)) // no se ha pedido: no se puede dar por borrada
        publicaciones.insertar(publicacion("borrada", ms = 5_000)) // dentro del tramo descargado y ya no está
        publicaciones.insertar(publicacion("sigue", ms = 3_000))

        publicaciones.sincronizar(listOf(publicacion("sigue", ms = 3_000), publicacion("nueva", ms = 6_000)), hayMasAntiguas = true)

        assertEquals(listOf("nueva", "sigue", "muy-antigua"), ids())
    }

    @Test
    fun sincronizarConElMuroVacioDelServidorVaciaRoom() = runBlocking {
        publicaciones.insertar(publicacion("p1"))

        publicaciones.sincronizar(emptyList(), hayMasAntiguas = false)

        assertEquals(emptyList<String>(), ids())
    }

    @Test
    fun losComentariosBorradosEnElServidorSeQuitan() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        comentarios.insertar(comentario("sigue", "p1"))
        comentarios.insertar(comentario("borrado", "p1"))

        comentarios.borrarQueYaNoEstan("p1", listOf("sigue"))

        assertEquals(listOf("sigue"), comentarios.observarDePublicacion("p1").first().map { it.comentario.id })
    }

    @Test
    fun borrarUnComentarioQuitaSoloEse() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        comentarios.insertar(comentario("sigue", "p1"))
        comentarios.insertar(comentario("borrado", "p1"))

        comentarios.eliminar("borrado")

        assertEquals(listOf("sigue"), comentarios.observarDePublicacion("p1").first().map { it.comentario.id })
    }

    // ---------- «Me gusta» (EX-05, #130) ----------

    @Test
    fun elMuroCuentaLosMeGustaYSabeSiEstaElMio() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        val meGusta = baseDeDatos.meGustaDao()
        meGusta.dar(MeGustaEntity("p1", ANA))
        meGusta.dar(MeGustaEntity("p1", "otra"))
        meGusta.dar(MeGustaEntity("p1", ANA)) // una persona solo cuenta una vez

        val deAna = publicaciones.observarPorId("p1", ANA).first()!!
        val deOtraCuenta = publicaciones.observarPorId("p1", "nadie").first()!!

        assertEquals(2, deAna.numMeGusta)
        assertTrue(deAna.meGustaMio)
        assertFalse(deOtraCuenta.meGustaMio)
    }

    @Test
    fun sincronizarLosMeGustaQuitaLosQueYaNoEstanYBorrarLaPublicacionLosBorra() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        val meGusta = baseDeDatos.meGustaDao()
        meGusta.dar(MeGustaEntity("p1", "otra"))

        meGusta.sincronizar(listOf("p1"), listOf(MeGustaEntity("p1", ANA)))
        val publicacion = publicaciones.observarPorId("p1", ANA).first()!!
        assertEquals(1, publicacion.numMeGusta)
        assertTrue(publicacion.meGustaMio)

        publicaciones.eliminar("p1")
        assertEquals(0, baseDeDatos.query("SELECT * FROM me_gusta", null).use { it.count })
    }

    @Test
    fun unaRespuestaGuardaElComentarioAlQueResponde() = runBlocking {
        publicaciones.insertar(publicacion("p1"))
        comentarios.insertar(comentario("c1", "p1"))
        comentarios.insertar(comentario("r1", "p1", ms = 2_000).copy(respuestaA = "c1"))

        val guardada = comentarios.observarDePublicacion("p1").first().first { it.comentario.id == "r1" }

        assertEquals("c1", guardada.comentario.respuestaA)
    }

    private fun publicacionDelServidor(texto: String) = PublicacionEntity(
        id = DEL_SERVIDOR,
        usuarioId = ANA,
        texto = texto,
        imagenRuta = "$ANA/foto.jpg",
        imagenUri = "https://servidor/$ANA/foto.jpg",
        timestamp = Instant.ofEpochMilli(5_000),
    )

    private companion object {
        const val ANA = "6f1c2a1e-0000-4000-8000-00000000000a"
        const val DEL_SERVIDOR = "0b5e1f6e-0000-4000-8000-000000000001"
    }
}
