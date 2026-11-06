package com.retroconver.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.retroconver.data.repository.UsuarioRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Pruebas del usuario local con una base de datos en memoria (E3-01, #13 y #52). */
@RunWith(AndroidJUnit4::class)
class UsuarioDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        repositorio = UsuarioRepository(baseDeDatos.usuarioDao())
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    @Test
    fun antesDeCrearloNoHayUsuarioLocal() = runBlocking {
        assertNull(repositorio.usuarioLocal().first())
    }

    @Test
    fun laPrimeraVezSeCreaElUsuarioLocalConElNombrePorDefecto() = runBlocking {
        val id = repositorio.asegurarUsuarioLocal("Yo", ahora = Instant.ofEpochMilli(1_000))

        val usuario = repositorio.usuarioLocal().first()!!
        assertEquals(UsuarioRepository.ID_USUARIO_LOCAL, id)
        assertEquals("Yo", usuario.nombre)
        assertEquals("", usuario.biografia)
        assertEquals("", usuario.estado)
        assertNull(usuario.avatarUri)
        assertEquals(Instant.ofEpochMilli(1_000), usuario.fechaAlta)
    }

    @Test
    fun soloSeCreaUnaVezAunqueSeLlameVariasVecesALaVez() = runBlocking {
        List(5) { async { repositorio.asegurarUsuarioLocal("Yo") } }.awaitAll()

        val total = baseDeDatos.openHelper.readableDatabase
            .query("SELECT COUNT(*) FROM usuarios")
            .use { cursor -> cursor.moveToFirst(); cursor.getInt(0) }
        assertEquals(1, total)
    }

    @Test
    fun volverACrearloNoBorraLoEditado() = runBlocking {
        repositorio.asegurarUsuarioLocal("Yo")
        repositorio.actualizarPerfil("Yeray", "Fan de los 2000", "Estudiando")

        // Lo que pasa al reiniciar la app
        repositorio.asegurarUsuarioLocal("Yo")

        val usuario = repositorio.usuarioLocal().first()!!
        assertEquals("Yeray", usuario.nombre)
        assertEquals("Fan de los 2000", usuario.biografia)
        assertEquals("Estudiando", usuario.estado)
    }

    @Test
    fun cambiarElAvatarDevuelveElAnteriorParaBorrarlo() = runBlocking {
        repositorio.asegurarUsuarioLocal("Yo")

        val primero = repositorio.cambiarAvatar("file:///avatar1.jpg")
        val segundo = repositorio.cambiarAvatar("file:///avatar2.jpg")

        assertNull(primero)
        assertEquals("file:///avatar1.jpg", segundo)
        assertEquals("file:///avatar2.jpg", repositorio.usuarioLocal().first()!!.avatarUri)
    }

    @Test
    fun editarElPerfilNoCambiaElAvatar() = runBlocking {
        repositorio.asegurarUsuarioLocal("Yo")
        repositorio.cambiarAvatar("file:///avatar.jpg")

        repositorio.actualizarPerfil("Yeray", "", "")

        assertEquals("file:///avatar.jpg", repositorio.usuarioLocal().first()!!.avatarUri)
    }
}
