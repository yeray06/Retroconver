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

/** Amistades y solicitudes con una base de datos en memoria (EX-04, #129). */
@RunWith(AndroidJUnit4::class)
class AmistadDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var amistades: AmistadDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        amistades = baseDeDatos.amistadDao()
        runBlocking {
            baseDeDatos.usuarioDao().guardarTodos(
                listOf(YO to "Yeray", ANA to "ana", BEA to "Bea", CRIS to "Cris").map { (id, nombre) ->
                    UsuarioEntity(id = id, nombre = nombre, fechaAlta = Instant.EPOCH)
                },
            )
        }
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    private fun amistad(id: String, de: String, para: String, aceptada: Boolean, segundo: Long = 0) =
        AmistadEntity(id, de, para, aceptada, Instant.ofEpochSecond(segundo))

    @Test
    fun losAmigosSalenLosPidieraQuienLosPidieraYPorOrdenAlfabetico() = runBlocking {
        amistades.guardarTodas(
            listOf(
                amistad("1", YO, CRIS, aceptada = true),
                amistad("2", ANA, YO, aceptada = true),
                amistad("3", YO, BEA, aceptada = false),
            ),
        )

        // «ana» en minúscula va antes que «Cris»: no distingue mayúsculas
        assertEquals(listOf("ana", "Cris"), amistades.observarAmigosDe(YO).first().map { it.nombre })
        assertEquals(setOf(CRIS, ANA), amistades.observarIdsDeAmigos(YO).first().toSet())
        assertEquals(listOf("Yeray"), amistades.observarAmigosDe(ANA).first().map { it.nombre })
    }

    @Test
    fun lasSolicitudesPendientesSalenConLaOtraPersonaLaMasRecientePrimero() = runBlocking {
        amistades.guardarTodas(
            listOf(
                amistad("vieja", ANA, YO, aceptada = false, segundo = 1),
                amistad("nueva", BEA, YO, aceptada = false, segundo = 2),
                amistad("mia", YO, CRIS, aceptada = false),
            ),
        )

        val recibidas = amistades.observarSolicitudesRecibidas(YO).first()
        assertEquals(listOf("nueva" to "Bea", "vieja" to "ana"), recibidas.map { it.idAmistad to it.persona.nombre })
        val enviadas = amistades.observarSolicitudesEnviadas(YO).first()
        assertEquals(listOf("mia" to "Cris"), enviadas.map { it.idAmistad to it.persona.nombre })
    }

    @Test
    fun laAmistadEntreDosPersonasSeEncuentraEnLosDosSentidos() = runBlocking {
        amistades.guardar(amistad("1", ANA, YO, aceptada = false))

        assertEquals("1", amistades.observarEntre(YO, ANA).first()?.id)
        assertEquals("1", amistades.observarEntre(ANA, YO).first()?.id)
        assertNull(amistades.observarEntre(YO, BEA).first())
    }

    @Test
    fun sincronizarLasPropiasQuitaLasQueYaNoEstanSinTocarLasDeOtros() = runBlocking {
        amistades.guardarTodas(
            listOf(
                amistad("sigue", YO, ANA, aceptada = true),
                amistad("rechazada", BEA, YO, aceptada = false),
                amistad("de-otros", ANA, CRIS, aceptada = true),
            ),
        )

        amistades.sincronizarTodasDe(YO, listOf(amistad("sigue", YO, ANA, aceptada = true), amistad("nueva", YO, CRIS, aceptada = false)))

        assertEquals(listOf("ana"), amistades.observarAmigosDe(YO).first().map { it.nombre })
        assertEquals(emptyList<SolicitudConPersona>(), amistades.observarSolicitudesRecibidas(YO).first())
        assertEquals(listOf("Cris"), amistades.observarSolicitudesEnviadas(YO).first().map { it.persona.nombre })
        assertEquals("de-otros", amistades.observarEntre(ANA, CRIS).first()?.id)
    }

    @Test
    fun sincronizarLosAmigosDeOtraPersonaNoTocaLasSolicitudes() = runBlocking {
        amistades.guardarTodas(listOf(amistad("antigua", ANA, CRIS, aceptada = true), amistad("pendiente", YO, ANA, aceptada = false)))

        amistades.sincronizarAmistadesDe(ANA, emptyList())

        assertEquals(emptyList<UsuarioEntity>(), amistades.observarAmigosDe(ANA).first())
        assertEquals("pendiente", amistades.observarEntre(YO, ANA).first()?.id)
    }

    @Test
    fun siSeBorraElPerfilDeUnaPersonaSeBorranSusAmistades() = runBlocking {
        amistades.guardar(amistad("1", YO, BEA, aceptada = true))

        baseDeDatos.openHelper.writableDatabase.execSQL("DELETE FROM usuarios WHERE id = '$BEA'")

        assertNull(amistades.observarEntre(YO, BEA).first())
    }

    private companion object {
        const val YO = "yo"
        const val ANA = "ana"
        const val BEA = "bea"
        const val CRIS = "cris"
    }
}
