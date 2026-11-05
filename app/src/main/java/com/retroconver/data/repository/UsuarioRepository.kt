package com.retroconver.data.repository

import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Única puerta de acceso al usuario local y a su perfil.
 *
 * La app no tiene cuentas (RF-04): trabaja con un único usuario local que siempre tiene el id
 * [ID_USUARIO_LOCAL]. Se crea al arrancar la app con [asegurarUsuarioLocal]; como el id es fijo
 * y la inserción ignora los duplicados, nunca puede haber dos aunque se llame varias veces
 * a la vez o se reinicie la app.
 *
 * Para publicar, comentar o escribir en el tablón, el autor es [ID_USUARIO_LOCAL].
 */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /**
     * Crea el usuario local con [nombrePorDefecto] si todavía no existe y devuelve su id.
     * Si ya existe no cambia nada, así no se pierde lo que se haya editado en el perfil.
     * `ahora` solo se pasa en las pruebas.
     */
    suspend fun asegurarUsuarioLocal(nombrePorDefecto: String, ahora: Instant = Instant.now()): Long {
        usuarioDao.insertarSiNoExiste(
            UsuarioEntity(id = ID_USUARIO_LOCAL, nombre = nombrePorDefecto, fechaAlta = ahora),
        )
        return ID_USUARIO_LOCAL
    }

    /** El usuario local. Emite cada vez que cambia el perfil, y `null` mientras no se ha creado. */
    fun usuarioLocal(): Flow<UsuarioEntity?> = usuarioDao.observar(ID_USUARIO_LOCAL)

    /** Guarda nombre, biografía y estado. Validarlos es cosa del ViewModel. */
    suspend fun actualizarPerfil(nombre: String, biografia: String, estado: String) {
        usuarioDao.actualizarDatos(ID_USUARIO_LOCAL, nombre, biografia, estado)
    }

    /**
     * Pone [avatarUri] como avatar del usuario local y devuelve la URI del avatar anterior
     * (o `null` si no tenía), para que quien llama borre ese fichero.
     */
    suspend fun cambiarAvatar(avatarUri: String?): String? {
        val anterior = usuarioDao.obtener(ID_USUARIO_LOCAL)?.avatarUri
        usuarioDao.actualizarAvatar(ID_USUARIO_LOCAL, avatarUri)
        return anterior
    }

    companion object {
        /** Id del usuario local: el autor de todo lo que se publica desde la app. */
        const val ID_USUARIO_LOCAL = 1L
    }
}
