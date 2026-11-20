package com.retroconver.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Mensajes del tablón con una base de datos en memoria (E4-09.1, #72). */
@RunWith(AndroidJUnit4::class)
class MensajeDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var mensajes: MensajeDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        mensajes = baseDeDatos.mensajeDao()
        runBlocking {
            baseDeDatos.usuarioDao().guardarTodos(
                listOf(YO to "Yeray", ANA to "Ana", BEA to "Bea").map { (id, nombre) ->
                    UsuarioEntity(id = id, nombre = nombre, fechaAlta = Instant.EPOCH)
                },
            )
        }
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    private fun mensaje(id: String, en: String, de: String, segundo: Long = 0) =
        MensajeEntity(id, en, de, "Mensaje $id", Instant.ofEpochSecond(segundo))

    @Test
    fun elTablonSaleConSusAutoresDelMasRecienteAlMasAntiguo() = runBlocking {
        mensajes.guardarTodos(
            listOf(
                mensaje("viejo", YO, ANA, segundo = 1),
                mensaje("nuevo", YO, BEA, segundo = 2),
                mensaje("de-ana", ANA, YO, segundo = 3),
            ),
        )

        val tablon = mensajes.observarTablon(YO).first()

        assertEquals(listOf("nuevo", "viejo"), tablon.map { it.mensaje.id })
        assertEquals(listOf("Bea", "Ana"), tablon.map { it.autor.nombre })
    }

    @Test
    fun loQueLlegaDelServidorSeGuardaSinDuplicar() = runBlocking {
        mensajes.guardar(mensaje("1", YO, ANA))
        mensajes.guardar(mensaje("1", YO, ANA).copy(texto = "Editado en el servidor"))

        assertEquals(listOf("Editado en el servidor"), mensajes.observarTablon(YO).first().map { it.mensaje.texto })
    }

    @Test
    fun sincronizarQuitaLosBorradosSinTocarOtrosTablones() = runBlocking {
        mensajes.guardarTodos(listOf(mensaje("queda", YO, ANA), mensaje("borrado", YO, BEA), mensaje("de-ana", ANA, BEA)))

        mensajes.sincronizarTablon(YO, listOf(mensaje("queda", YO, ANA), mensaje("nuevo", YO, BEA)))

        assertEquals(setOf("queda", "nuevo"), mensajes.observarTablon(YO).first().map { it.mensaje.id }.toSet())
        assertEquals(listOf("de-ana"), mensajes.observarTablon(ANA).first().map { it.mensaje.id })
    }

    @Test
    fun alBorrarUnPerfilSeBorranSuTablonYSusMensajes() = runBlocking {
        mensajes.guardarTodos(listOf(mensaje("en-ana", ANA, YO), mensaje("de-ana", YO, ANA), mensaje("de-bea", YO, BEA)))

        baseDeDatos.openHelper.writableDatabase.execSQL("DELETE FROM usuarios WHERE id = '$ANA'")

        assertEquals(emptyList<Any>(), mensajes.observarTablon(ANA).first())
        assertEquals(listOf("de-bea"), mensajes.observarTablon(YO).first().map { it.mensaje.id })
    }

    private companion object {
        const val YO = "6f1c2a1e-0000-4000-8000-000000000001"
        const val ANA = "6f1c2a1e-0000-4000-8000-000000000002"
        const val BEA = "6f1c2a1e-0000-4000-8000-000000000003"
    }
}
