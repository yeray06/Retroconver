package com.retroconver.data.repository

import com.retroconver.data.local.MensajeDirectoConPersonas
import com.retroconver.data.local.MensajeDirectoDao
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.MensajeDirectoDto
import com.retroconver.data.remote.MensajesDirectosService
import com.retroconver.data.remote.NuevoMensajeDirectoDto
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.remote.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Una conversación de la lista: la otra persona y el último mensaje. */
data class Conversacion(
    val persona: UsuarioEntity,
    val ultimo: MensajeDirectoConPersonas,
)

/**
 * Mensajes directos entre amigos (EX-01, #28), con el mismo patrón que el tablón: las pantallas
 * leen de Room y, al enviar, primero el servidor y después su respuesta en Room.
 *
 * No hay tiempo real: la conversación abierta se descarga cada pocos segundos (ChatScreen) y la
 * lista, al volver a la pantalla. Solo se puede escribir a amigos (lo comprueba el servidor).
 */
class MensajesDirectosRepository(
    private val dao: MensajeDirectoDao,
    private val servicio: MensajesDirectosService,
    private val usuarios: UsuarioRepository,
) {

    /** Las conversaciones de [miId], la que tiene el mensaje más reciente primero. */
    fun conversaciones(miId: String): Flow<List<Conversacion>> = dao.observarMios(miId).map { mensajes ->
        // Vienen del más reciente al más antiguo: el primero de cada persona es su último mensaje
        mensajes.distinctBy { it.otraPersona(miId).id }.map { Conversacion(it.otraPersona(miId), it) }
    }

    /** La conversación de [miId] con [otroId], del mensaje más antiguo al más reciente. */
    fun conversacion(miId: String, otroId: String): Flow<List<MensajeDirectoConPersonas>> =
        dao.observarConversacion(miId, otroId)

    /** Descarga los [LIMITE] últimos mensajes de todas mis conversaciones y los guarda en Room. */
    suspend fun refrescar(): ResultadoRed<Unit> =
        llamarAlServidor { servicio.mios(LIMITE) }.map { filas -> guardar(filas) }

    /**
     * Envía [texto] a [destinatarioId]. Con [zumbido], el mensaje hace temblar la conversación de
     * las dos personas. Si no son amigos, el servidor no deja (error de permiso).
     */
    suspend fun enviar(destinatarioId: String, texto: String, zumbido: Boolean = false): ResultadoRed<Unit> {
        val resultado = llamarAlServidor {
            servicio.enviar(NuevoMensajeDirectoDto(destinatarioId, texto.trim(), zumbido))
        }
        if (resultado is ResultadoRed.Fallo) return resultado
        val fila = (resultado as ResultadoRed.Correcto).datos.firstOrNull()
            ?: return ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        guardar(listOf(fila))
        return ResultadoRed.Correcto(Unit)
    }

    /** Id de la cuenta de la sesión. */
    suspend fun idActual(): String? = usuarios.idActual()

    /** Los mensajes apuntan a las dos personas en Room: se guardan antes. */
    private suspend fun guardar(filas: List<MensajeDirectoDto>) {
        val completas = filas.filter { it.remitente != null && it.destinatario != null }
        usuarios.guardarDelServidor(completas.flatMap { listOfNotNull(it.remitente, it.destinatario) }.distinctBy { it.id })
        if (completas.isNotEmpty()) dao.guardar(completas.map { it.aEntidad() })
    }

    companion object {
        /** Cuántos mensajes se descargan cada vez. */
        const val LIMITE = 200

        /** Código de Postgres para «sin permiso» (lo traduce MensajesRed). */
        const val CODIGO_SIN_PERMISO = "42501"
    }
}
