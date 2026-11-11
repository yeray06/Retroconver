package com.retroconver.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.retroconver.data.repository.PublicacionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Pruebas con una base de datos en memoria: orden por fecha, comentarios, borrado en cascada
 * y lo que llega del servidor (ids UUID y `@Upsert`, E4-05.1, #121).
 */
@RunWith(AndroidJUnit4::class)
class PublicacionDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var repositorio: PublicacionRepository

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        repositorio = PublicacionRepository(baseDeDatos.publicacionDao(), baseDeDatos.comentarioDao())
        runBlocking {
            baseDeDatos.usuarioDao().guardar(UsuarioEntity(id = ANA, nombre = "Ana", fechaAlta = Instant.EPOCH))
        }
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    @Test
    fun lasPublicacionesSalenDeMasRecienteAMasAntigua() = runBlocking {
        repositorio.publicar(ANA, "primera", ahora = Instant.ofEpochMilli(1_000))
        repositorio.publicar(ANA, "tercera", ahora = Instant.ofEpochMilli(3_000))
        repositorio.publicar(ANA, "segunda", ahora = Instant.ofEpochMilli(2_000))

        val textos = repositorio.publicaciones().first().map { it.publicacion.texto }

        assertEquals(listOf("tercera", "segunda", "primera"), textos)
    }

    @Test
    fun laPublicacionTraeSuAutorYSuNumeroDeComentarios() = runBlocking {
        val id = repositorio.publicar(ANA, "hola")
        repositorio.comentar(id, ANA, "uno")
        repositorio.comentar(id, ANA, "dos")

        val publicacion = repositorio.publicacion(id).first()!!

        assertEquals("Ana", publicacion.autor.nombre)
        assertEquals(2, publicacion.numComentarios)
    }

    @Test
    fun losComentariosSalenDelMasRecienteAlMasAntiguo() = runBlocking {
        val id = repositorio.publicar(ANA, "hola")
        repositorio.comentar(id, ANA, "antiguo", ahora = Instant.ofEpochMilli(1_000))
        repositorio.comentar(id, ANA, "nuevo", ahora = Instant.ofEpochMilli(2_000))

        val textos = repositorio.comentarios(id).first().map { it.comentario.texto }

        assertEquals(listOf("nuevo", "antiguo"), textos)
    }

    @Test
    fun alBorrarUnaPublicacionSeBorranSusComentarios() = runBlocking {
        val id = repositorio.publicar(ANA, "hola")
        repositorio.comentar(id, ANA, "adiós")

        repositorio.eliminar(id)

        assertNull(repositorio.publicacion(id).first())
        assertEquals(emptyList<ComentarioConAutor>(), repositorio.comentarios(id).first())
    }

    @Test
    fun alBorrarUnUsuarioSeBorranSusPublicaciones() = runBlocking {
        repositorio.publicar(ANA, "hola")

        baseDeDatos.openHelper.writableDatabase.execSQL("DELETE FROM usuarios WHERE id = '$ANA'")

        assertEquals(emptyList<PublicacionConAutor>(), repositorio.publicaciones().first())
    }

    @Test
    fun cadaPublicacionTieneUnUuidDistinto() = runBlocking {
        val primera = repositorio.publicar(ANA, "una")
        val segunda = repositorio.publicar(ANA, "otra")

        assertNotEquals(primera, segunda)
        assertEquals(36, primera.length)
    }

    @Test
    fun loQueLlegaDelServidorSeGuardaSinDuplicarNiBorrarComentarios() = runBlocking {
        val publicaciones = baseDeDatos.publicacionDao()
        publicaciones.guardar(listOf(publicacionDelServidor("primer texto")))
        repositorio.comentar(DEL_SERVIDOR, ANA, "comentario")

        // Al refrescar el muro llega otra vez la misma publicación, editada
        publicaciones.guardar(listOf(publicacionDelServidor("texto editado")))

        val muro = repositorio.publicaciones().first()
        assertEquals(1, muro.size)
        assertEquals("texto editado", muro.single().publicacion.texto)
        assertEquals(1, muro.single().numComentarios)
    }

    @Test
    fun losComentariosDelServidorTambienSeActualizan() = runBlocking {
        baseDeDatos.publicacionDao().guardar(listOf(publicacionDelServidor("hola")))
        val comentario = ComentarioEntity(
            id = "c1",
            publicacionId = DEL_SERVIDOR,
            usuarioId = ANA,
            texto = "antes",
            timestamp = Instant.EPOCH,
        )
        baseDeDatos.comentarioDao().guardar(listOf(comentario))
        baseDeDatos.comentarioDao().guardar(listOf(comentario.copy(texto = "después")))

        assertEquals(listOf("después"), repositorio.comentarios(DEL_SERVIDOR).first().map { it.comentario.texto })
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
