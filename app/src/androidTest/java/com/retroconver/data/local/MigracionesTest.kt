package com.retroconver.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migraciones de Room (Migraciones.kt): al actualizar la app se conserva la copia local.
 *
 * Cada prueba crea una base de datos con el esquema de una versión anterior (app/schemas),
 * le mete datos, la migra y comprueba que el resultado es igual que el esquema nuevo y que
 * los datos siguen ahí.
 */
@RunWith(AndroidJUnit4::class)
class MigracionesTest {

    @get:Rule
    val ayudante = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    private val contexto: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun borrar() {
        contexto.deleteDatabase(NOMBRE)
    }

    private fun insertarPerfil(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO usuarios (id, nombre, biografia, estado, tema, fechaAlta) " +
                "VALUES ('yo', 'Yeray', 'Hola', 'Estudiando', 'noche', 0)",
        )
    }

    @Test
    fun de5a6SeConservanLosDatosYAparecenLasColumnasNuevas() {
        ayudante.createDatabase(NOMBRE, 5).use { db ->
            insertarPerfil(db)
            db.execSQL("INSERT INTO albumes (id, propietarioId, titulo, descripcion, fechaCreacion) VALUES ('a1', 'yo', 'Viaje', '', 0)")
        }

        ayudante.runMigrationsAndValidate(NOMBRE, 6, true, MIGRACION_5_6).use { db ->
            db.query("SELECT nombre, tema, tipografia FROM usuarios WHERE id = 'yo'").use { fila ->
                assertTrue(fila.moveToFirst())
                assertEquals("Yeray", fila.getString(0))
                assertEquals("noche", fila.getString(1))
                assertTrue("La tipografía empieza vacía", fila.isNull(2))
            }
            db.query("SELECT COUNT(*) FROM albumes").use { fila ->
                fila.moveToFirst()
                assertEquals(1, fila.getInt(0))
            }
        }
    }

    @Test
    fun de6a7SeCreaLaTablaDeAmistades() {
        ayudante.createDatabase(NOMBRE, 6).use { db -> insertarPerfil(db) }

        ayudante.runMigrationsAndValidate(NOMBRE, 7, true, MIGRACION_6_7).use { db ->
            db.execSQL("INSERT INTO usuarios (id, nombre, fechaAlta) VALUES ('ana', 'Ana', 0)")
            db.execSQL("INSERT INTO amistades (id, solicitanteId, destinatarioId, aceptada, fechaCreacion) VALUES ('1', 'yo', 'ana', 1, 0)")
            db.query("SELECT COUNT(*) FROM amistades").use { fila ->
                fila.moveToFirst()
                assertEquals(1, fila.getInt(0))
            }
        }
    }

    @Test
    fun de7a8SeCreaLaTablaDelTablon() {
        ayudante.createDatabase(NOMBRE, 7).use { db -> insertarPerfil(db) }

        ayudante.runMigrationsAndValidate(NOMBRE, 8, true, MIGRACION_7_8).use { db ->
            db.query("SELECT nombre FROM usuarios WHERE id = 'yo'").use { fila ->
                assertTrue(fila.moveToFirst())
                assertEquals("Yeray", fila.getString(0))
            }
            db.execSQL("INSERT INTO usuarios (id, nombre, fechaAlta) VALUES ('ana', 'Ana', 0)")
            db.execSQL("INSERT INTO mensajes (id, perfilId, autorId, texto, fechaCreacion) VALUES ('1', 'yo', 'ana', 'Hola', 0)")
            db.query("SELECT COUNT(*) FROM mensajes").use { fila ->
                fila.moveToFirst()
                assertEquals(1, fila.getInt(0))
            }
        }
    }

    /** Lo que hace la app al abrirse después de actualizarla: Room con todas las migraciones. */
    @Test
    fun laAppAbreUnaCopiaDeLaVersion5SinBorrarla() = runBlocking {
        ayudante.createDatabase(NOMBRE, 5).use { db -> insertarPerfil(db) }

        val room = abrirComoLaApp()
        try {
            assertEquals("Yeray", room.usuarioDao().obtener("yo")?.nombre)
        } finally {
            room.close()
        }
    }

    /** Las versiones 1 a 4 eran sin servidor: esa copia no sirve y se crea de nuevo, vacía. */
    @Test
    fun desdeUnaVersionSinServidorLaCopiaSeCreaDeNuevo() = runBlocking {
        ayudante.createDatabase(NOMBRE, 4).use { db -> insertarPerfil(db) }

        val room = abrirComoLaApp()
        try {
            assertNull(room.usuarioDao().obtener("yo"))
        } finally {
            room.close()
        }
    }

    private fun abrirComoLaApp(): AppDatabase =
        Room.databaseBuilder(contexto, AppDatabase::class.java, NOMBRE)
            .addMigrations(*MIGRACIONES)
            .fallbackToDestructiveMigrationFrom(true, *VERSIONES_SIN_MIGRACION)
            .build()

    private companion object {
        const val NOMBRE = "migraciones-prueba.db"
    }
}
