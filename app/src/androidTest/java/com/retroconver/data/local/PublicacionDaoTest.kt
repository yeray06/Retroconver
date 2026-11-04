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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Pruebas con una base de datos en memoria: orden por fecha, comentarios y borrado en cascada. */
@RunWith(AndroidJUnit4::class)
class PublicacionDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var repositorio: PublicacionRepository

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        repositorio = PublicacionRepository(baseDeDatos.publicacionDao(), baseDeDatos.comentarioDao())
        // Aún no hay UsuarioDao (E3-01): el usuario se inserta con SQL directo.
        baseDeDatos.openHelper.writableDatabase.execSQL(
            "INSERT INTO usuarios (id, nombre, fechaAlta) VALUES (1, 'Ana', 0)",
        )
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    @Test
    fun lasPublicacionesSalenDeMasRecienteAMasAntigua() = runBlocking {
        repositorio.publicar(1, "primera", ahora = Instant.ofEpochMilli(1_000))
        repositorio.publicar(1, "tercera", ahora = Instant.ofEpochMilli(3_000))
        repositorio.publicar(1, "segunda", ahora = Instant.ofEpochMilli(2_000))

        val textos = repositorio.publicaciones().first().map { it.publicacion.texto }

        assertEquals(listOf("tercera", "segunda", "primera"), textos)
    }

    @Test
    fun laPublicacionTraeSuAutorYSuNumeroDeComentarios() = runBlocking {
        val id = repositorio.publicar(1, "hola")
        repositorio.comentar(id, 1, "uno")
        repositorio.comentar(id, 1, "dos")

        val publicacion = repositorio.publicacion(id).first()!!

        assertEquals("Ana", publicacion.autor.nombre)
        assertEquals(2, publicacion.numComentarios)
    }

    @Test
    fun losComentariosSalenDelMasRecienteAlMasAntiguo() = runBlocking {
        val id = repositorio.publicar(1, "hola")
        repositorio.comentar(id, 1, "antiguo", ahora = Instant.ofEpochMilli(1_000))
        repositorio.comentar(id, 1, "nuevo", ahora = Instant.ofEpochMilli(2_000))

        val textos = repositorio.comentarios(id).first().map { it.comentario.texto }

        assertEquals(listOf("nuevo", "antiguo"), textos)
    }

    @Test
    fun alBorrarUnaPublicacionSeBorranSusComentarios() = runBlocking {
        val id = repositorio.publicar(1, "hola")
        repositorio.comentar(id, 1, "adiós")

        repositorio.eliminar(id)

        assertNull(repositorio.publicacion(id).first())
        assertEquals(emptyList<ComentarioConAutor>(), repositorio.comentarios(id).first())
    }

    @Test
    fun alBorrarUnUsuarioSeBorranSusPublicaciones() = runBlocking {
        repositorio.publicar(1, "hola")

        baseDeDatos.openHelper.writableDatabase.execSQL("DELETE FROM usuarios WHERE id = 1")

        assertEquals(emptyList<PublicacionConAutor>(), repositorio.publicaciones().first())
    }
}
