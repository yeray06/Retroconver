package com.retroconver.data.repository

import com.retroconver.data.local.MensajeConAutor
import com.retroconver.data.local.MensajeDao
import com.retroconver.data.remote.MensajeDto
import com.retroconver.data.remote.MensajesService
import com.retroconver.data.remote.NuevoMensajeDto
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.remote.map
import kotlinx.coroutines.flow.Flow

/**
 * Tablón de mensajes de cada perfil (E4-09, #22), con el mismo patrón que el resto de módulos
 * (docs/arquitectura.md, apartado 2): la pantalla lee de Room y, al escribir o borrar, primero
 * se hace en el servidor y después se guarda lo que devuelve.
 *
 * Cada mensaje llega con su autor y con el dueño del tablón, que se guardan antes en Room
 * para poder enseñar su nombre y su avatar (y porque el mensaje apunta a los dos).
 */
class MensajesRepository(
    private val mensajeDao: MensajeDao,
    private val servicio: MensajesService,
    private val usuarios: UsuarioRepository,
) {

    /** Mensajes del tablón de [perfilId] con su autor, el más reciente primero. Hay que [refrescar] antes. */
    fun tablon(perfilId: String): Flow<List<MensajeConAutor>> = mensajeDao.observarTablon(perfilId)

    /**
     * Descarga los [LIMITE_TABLON] últimos mensajes del tablón de [perfilId] y deja Room igual
     * que el servidor, también si se ha borrado alguno desde otro móvil. Sin conexión no cambia
     * nada y se sigue viendo la última copia.
     */
    suspend fun refrescar(perfilId: String): ResultadoRed<Unit> =
        llamarAlServidor { servicio.tablon(igual(perfilId), LIMITE_TABLON) }.map { filas ->
            guardarPerfiles(filas)
            mensajeDao.sincronizarTablon(perfilId, filas.map { it.aEntidad() })
        }

    /** Escribe [texto] en el tablón de [perfilId] en nombre de la cuenta de la sesión. */
    suspend fun escribir(perfilId: String, texto: String): ResultadoRed<Unit> =
        when (val resultado = llamarAlServidor { servicio.escribir(NuevoMensajeDto(perfilId, texto.trim())) }) {
            is ResultadoRed.Fallo -> resultado
            is ResultadoRed.Correcto -> {
                // Con `Prefer: return=representation` el servidor devuelve la fila; si no, no había permiso
                val fila = resultado.datos.firstOrNull()
                if (fila == null) {
                    ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
                } else {
                    guardarPerfiles(listOf(fila))
                    mensajeDao.guardar(fila.aEntidad())
                    ResultadoRed.Correcto(Unit)
                }
            }
        }

    /**
     * Borra un mensaje (lo puede borrar su autor o el dueño del tablón). Si el servidor no
     * devuelve la fila es que ya no existía: también se quita de Room.
     */
    suspend fun borrar(idMensaje: String): ResultadoRed<Unit> =
        llamarAlServidor { servicio.borrar(igual(idMensaje)) }.map { mensajeDao.borrar(idMensaje) }

    private suspend fun guardarPerfiles(filas: List<MensajeDto>) {
        usuarios.guardarDelServidor(filas.flatMap { listOfNotNull(it.autor, it.perfil) }.distinctBy { it.id })
    }

    private companion object {
        /** Cuántos mensajes de cada tablón se descargan. */
        const val LIMITE_TABLON = 50

        /** Código de Postgres para «sin permiso» (lo traduce MensajesRed). */
        const val CODIGO_SIN_PERMISO = "42501"
    }
}
