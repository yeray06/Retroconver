package com.retroconver.data.repository

import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.remote.map
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Única puerta de acceso a los perfiles (E4-05.1, #121).
 *
 * El servidor es la fuente de verdad y Room guarda la última copia: las pantallas leen de Room
 * ([usuarioActual], [usuario]) y se actualizan solas cuando [refrescar] trae datos nuevos.
 *
 * El usuario actual es el de la sesión abierta: su id es el UUID de la cuenta. Ya no hay
 * un usuario local con id fijo (E3-01.1).
 */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val perfilService: PerfilService,
    private val sesion: SesionRepository,
    private val configuracion: ConfiguracionSupabase,
) {

    /** Id de la cuenta con la sesión abierta, o `null` si no hay sesión. Es el autor de lo que se publica. */
    suspend fun idActual(): String? = sesion.sesionActual()?.idCuenta

    /**
     * Perfil de la sesión abierta. Emite cada vez que cambia, y `null` mientras no hay copia en Room
     * (por ejemplo, justo después de entrar y antes de descargarlo).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun usuarioActual(): Flow<UsuarioEntity?> = sesion.idCuenta.flatMapLatest { id ->
        if (id == null) flowOf(null) else usuarioDao.observar(id)
    }

    /** Perfil de cualquier persona de la red (para ver perfiles ajenos, E4-08). */
    fun usuario(id: String): Flow<UsuarioEntity?> = usuarioDao.observar(id)

    /**
     * Descarga el perfil [id] del servidor y actualiza la copia de Room. Sin conexión no cambia
     * nada y la pantalla sigue con la copia que hay.
     */
    suspend fun refrescar(id: String): ResultadoRed<Unit> =
        llamarAlServidor { perfilService.obtener(igual(id)) }
            .map { filas ->
                filas.firstOrNull()?.let { usuarioDao.guardar(it.aEntidad(configuracion)) }
                Unit
            }

    /** [refrescar] el perfil de la sesión abierta. */
    suspend fun refrescarActual(): ResultadoRed<Unit> {
        val id = idActual() ?: return ResultadoRed.SesionCaducada
        return refrescar(id)
    }

    /**
     * Guarda nombre, biografía y estado en la copia local. Validarlos es cosa del ViewModel.
     * Provisional: en E4-05.2 (#122) se guardan primero en el servidor.
     */
    suspend fun actualizarPerfil(nombre: String, biografia: String, estado: String) {
        val id = idActual() ?: return
        usuarioDao.actualizarDatos(id, nombre, biografia, estado)
    }

    /**
     * Pone [avatarUri] como avatar en la copia local y devuelve el anterior (o `null`), para que
     * quien llama borre ese fichero. Provisional: en E4-05.2 (#122) el avatar se sube al servidor.
     */
    suspend fun cambiarAvatar(avatarUri: String?): String? {
        val id = idActual() ?: return null
        val anterior = usuarioDao.obtener(id)?.avatarUri
        usuarioDao.actualizarAvatar(id, avatarRuta = null, avatarUri = avatarUri)
        return anterior
    }
}
