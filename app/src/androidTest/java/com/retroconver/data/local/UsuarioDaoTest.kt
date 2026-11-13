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

/**
 * Copia local de los perfiles con una base de datos en memoria (E4-05.1, #121).
 * Los ids son los UUID de las cuentas y lo que llega del servidor se guarda con `@Upsert`.
 */
@RunWith(AndroidJUnit4::class)
class UsuarioDaoTest {

    private lateinit var baseDeDatos: AppDatabase
    private lateinit var usuarios: UsuarioDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        usuarios = baseDeDatos.usuarioDao()
    }

    @After
    fun cerrarBaseDeDatos() = baseDeDatos.close()

    private fun perfil(nombre: String = "Yeray", avatar: String? = null) = UsuarioEntity(
        id = YERAY,
        nombre = nombre,
        biografia = "Fan de los 2000",
        avatarRuta = avatar,
        avatarUri = avatar?.let { "https://servidor/$it" },
        fechaAlta = Instant.ofEpochMilli(1_000),
    )

    @Test
    fun sinCopiaNoHayUsuario() = runBlocking {
        assertNull(usuarios.observar(YERAY).first())
    }

    @Test
    fun guardarCreaElPerfilConTodosSusDatos() = runBlocking {
        usuarios.guardar(perfil(avatar = "$YERAY/a.jpg"))

        val guardado = usuarios.observar(YERAY).first()!!
        assertEquals("Yeray", guardado.nombre)
        assertEquals("Fan de los 2000", guardado.biografia)
        assertEquals("", guardado.estado)
        assertEquals("$YERAY/a.jpg", guardado.avatarRuta)
        assertEquals("https://servidor/$YERAY/a.jpg", guardado.avatarUri)
        assertEquals(Instant.ofEpochMilli(1_000), guardado.fechaAlta)
    }

    @Test
    fun guardarOtraVezActualizaSinDuplicar() = runBlocking {
        usuarios.guardar(perfil(nombre = "Yeray"))
        usuarios.guardar(perfil(nombre = "Yeray S."))

        assertEquals("Yeray S.", usuarios.obtener(YERAY)!!.nombre)
        val total = baseDeDatos.openHelper.readableDatabase
            .query("SELECT COUNT(*) FROM usuarios")
            .use { cursor -> cursor.moveToFirst(); cursor.getInt(0) }
        assertEquals(1, total)
    }

    @Test
    fun actualizarElPerfilDesdeElServidorNoBorraSusPublicaciones() = runBlocking {
        usuarios.guardar(perfil())
        baseDeDatos.publicacionDao().insertar(
            PublicacionEntity(id = "p1", usuarioId = YERAY, texto = "hola", timestamp = Instant.now()),
        )

        // @Upsert actualiza la fila; un REPLACE la borraría y se llevaría las publicaciones en cascada
        usuarios.guardar(perfil(nombre = "Yeray S."))

        assertEquals(1, baseDeDatos.publicacionDao().observarTodas().first().size)
    }

    @Test
    fun guardarVariosALaVez() = runBlocking {
        usuarios.guardarTodos(
            listOf(
                perfil(),
                UsuarioEntity(id = DANI, nombre = "Dani", fechaAlta = Instant.now()),
            ),
        )

        assertEquals("Dani", usuarios.obtener(DANI)!!.nombre)
        assertEquals("Yeray", usuarios.obtener(YERAY)!!.nombre)
    }

    @Test
    fun editarElPerfilNoCambiaElAvatar() = runBlocking {
        usuarios.guardar(perfil(avatar = "$YERAY/a.jpg"))

        usuarios.actualizarDatos(YERAY, "Yeray", "", "Estudiando")

        val guardado = usuarios.obtener(YERAY)!!
        assertEquals("Estudiando", guardado.estado)
        assertEquals("$YERAY/a.jpg", guardado.avatarRuta)
    }

    @Test
    fun cambiarElAvatarNoCambiaLosDatos() = runBlocking {
        usuarios.guardar(perfil(avatar = "$YERAY/a.jpg"))

        usuarios.actualizarAvatar(YERAY, avatarRuta = "$YERAY/b.jpg", avatarUri = "https://servidor/$YERAY/b.jpg")

        val guardado = usuarios.obtener(YERAY)!!
        assertEquals("$YERAY/b.jpg", guardado.avatarRuta)
        assertEquals("https://servidor/$YERAY/b.jpg", guardado.avatarUri)
        assertEquals("Fan de los 2000", guardado.biografia)
    }

    @Test
    fun quitarElAvatar() = runBlocking {
        usuarios.guardar(perfil(avatar = "$YERAY/a.jpg"))

        usuarios.actualizarAvatar(YERAY, avatarRuta = null, avatarUri = null)

        assertNull(usuarios.obtener(YERAY)!!.avatarUri)
    }

    private companion object {
        const val YERAY = "6f1c2a1e-0000-4000-8000-000000000001"
        const val DANI = "6f1c2a1e-0000-4000-8000-000000000002"
    }
}
