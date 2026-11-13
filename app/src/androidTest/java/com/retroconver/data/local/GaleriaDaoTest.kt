package com.retroconver.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Álbumes y fotos con una base de datos en memoria (E4-10.1, #68). */
@RunWith(AndroidJUnit4::class)
class GaleriaDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var galeria: GaleriaDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        galeria = baseDeDatos.galeriaDao()
        runBlocking {
            baseDeDatos.usuarioDao().guardarTodos(
                listOf(
                    UsuarioEntity(id = ANA, nombre = "Ana", fechaAlta = Instant.EPOCH),
                    UsuarioEntity(id = DANI, nombre = "Dani", fechaAlta = Instant.EPOCH),
                ),
            )
        }
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    private fun album(id: String, segundo: Long, dueno: String = ANA) =
        AlbumEntity(id = id, propietarioId = dueno, titulo = "Álbum $id", fechaCreacion = Instant.ofEpochSecond(segundo))

    private fun foto(id: String, album: String, segundo: Long) = FotoEntity(
        id = id,
        albumId = album,
        propietarioId = ANA,
        ruta = "$ANA/$id.jpg",
        uri = "https://servidor/$ANA/$id.jpg",
        fechaAnadida = Instant.ofEpochSecond(segundo),
    )

    @Test
    fun losAlbumesSalenDelMasRecienteAlMasAntiguoConSuNumeroDeFotosYSuPortada() = runBlocking {
        galeria.guardarAlbumes(listOf(album("viejo", 1), album("nuevo", 2)))
        galeria.guardarFotos(listOf(foto("f1", "viejo", 10), foto("f2", "viejo", 20), foto("f3", "nuevo", 5)))

        val albumes = galeria.observarAlbumesDe(ANA).first()

        assertEquals(listOf("nuevo", "viejo"), albumes.map { it.album.id })
        assertEquals(listOf(1, 2), albumes.map { it.numFotos })
        assertEquals("La portada es la foto más reciente", "https://servidor/$ANA/f2.jpg", albumes[1].portadaUri)
    }

    @Test
    fun unAlbumVacioNoTienePortada() = runBlocking {
        galeria.guardarAlbum(album("vacio", 1))

        val albumVacio = galeria.observarAlbumesDe(ANA).first().single()

        assertEquals(0, albumVacio.numFotos)
        assertNull(albumVacio.portadaUri)
    }

    @Test
    fun soloSalenLosAlbumesDeEsaPersona() = runBlocking {
        galeria.guardarAlbumes(listOf(album("de-ana", 1), album("de-dani", 2, dueno = DANI)))

        assertEquals(listOf("de-dani"), galeria.observarAlbumesDe(DANI).first().map { it.album.id })
    }

    @Test
    fun lasFotosDeUnAlbumSalenDeLaMasRecienteALaMasAntigua() = runBlocking {
        galeria.guardarAlbum(album("a", 1))
        galeria.guardarFotos(listOf(foto("antigua", "a", 1), foto("nueva", "a", 2)))

        assertEquals(listOf("nueva", "antigua"), galeria.observarFotos("a").first().map { it.id })
    }

    @Test
    fun alBorrarUnAlbumSeBorranSusFotos() = runBlocking {
        galeria.guardarAlbum(album("a", 1))
        galeria.guardarFoto(foto("f", "a", 1))

        galeria.borrarAlbum("a")

        assertNull(galeria.observarFoto("f").first())
    }

    @Test
    fun sincronizarDejaRoomIgualQueElServidor() = runBlocking {
        galeria.guardarAlbumes(listOf(album("sigue", 1), album("borrado", 2)))
        galeria.guardarFotos(listOf(foto("f-sigue", "sigue", 1), foto("f-borrada", "sigue", 2)))
        galeria.guardarAlbum(album("de-dani", 3, dueno = DANI))

        // En el servidor: «borrado» ya no existe, «f-borrada» tampoco y hay una foto nueva
        galeria.sincronizarAlbumesDe(
            ANA,
            albumes = listOf(album("sigue", 1).copy(titulo = "Renombrado")),
            fotos = listOf(foto("f-sigue", "sigue", 1), foto("f-nueva", "sigue", 3)),
        )

        val albumes = galeria.observarAlbumesDe(ANA).first()
        assertEquals(listOf("sigue"), albumes.map { it.album.id })
        assertEquals("Renombrado", albumes.single().album.titulo)
        assertEquals(listOf("f-nueva", "f-sigue"), galeria.observarFotos("sigue").first().map { it.id })
        assertEquals("Los álbumes de otra persona no se tocan", 1, galeria.observarAlbumesDe(DANI).first().size)
    }

    @Test
    fun sincronizarUnAlbumQuitaLasFotosBorradas() = runBlocking {
        galeria.guardarAlbum(album("a", 1))
        galeria.guardarFotos(listOf(foto("queda", "a", 1), foto("se-va", "a", 2)))

        galeria.sincronizarAlbum(album("a", 1), listOf(foto("queda", "a", 1)))

        assertEquals(listOf("queda"), galeria.observarFotos("a").first().map { it.id })
    }

    @Test
    fun sincronizarSinAlbumesLosQuitaTodos() = runBlocking {
        galeria.guardarAlbum(album("a", 1))

        galeria.sincronizarAlbumesDe(ANA, albumes = emptyList(), fotos = emptyList())

        assertEquals(0, galeria.observarAlbumesDe(ANA).first().size)
    }

    private companion object {
        const val ANA = "6f1c2a1e-0000-4000-8000-00000000000a"
        const val DANI = "6f1c2a1e-0000-4000-8000-00000000000d"
    }
}
