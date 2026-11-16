package com.retroconver.data.repository

import com.retroconver.data.local.AmistadDao
import com.retroconver.data.local.AmistadEntity
import com.retroconver.data.local.SolicitudConPersona
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AceptarAmistadDto
import com.retroconver.data.remote.AmistadDto
import com.retroconver.data.remote.AmistadesService
import com.retroconver.data.remote.NuevaAmistadDto
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.remote.map
import com.retroconver.data.remote.participa
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Cómo está la relación entre la cuenta de la sesión y otra persona (EX-04, #129). */
sealed interface RelacionAmistad {
    /** Es el perfil propio: no hay nada que pedir. */
    data object EsMiPerfil : RelacionAmistad

    data object Ninguna : RelacionAmistad

    data class SolicitudEnviada(val idAmistad: String) : RelacionAmistad

    data class SolicitudRecibida(val idAmistad: String) : RelacionAmistad

    data class Amigos(val idAmistad: String) : RelacionAmistad
}

/** La relación de [yo] con [otra] según la fila de [amistad] que hay entre las dos, si la hay. */
fun relacionEntre(yo: String?, otra: String, amistad: AmistadEntity?): RelacionAmistad = when {
    yo == null -> RelacionAmistad.Ninguna
    yo == otra -> RelacionAmistad.EsMiPerfil
    amistad == null -> RelacionAmistad.Ninguna
    amistad.aceptada -> RelacionAmistad.Amigos(amistad.id)
    amistad.solicitanteId == yo -> RelacionAmistad.SolicitudEnviada(amistad.id)
    else -> RelacionAmistad.SolicitudRecibida(amistad.id)
}

/**
 * Amistades y solicitudes (EX-04, #129), con el mismo patrón que el resto de módulos
 * (docs/arquitectura.md, apartado 2): las pantallas leen de Room y, al escribir, primero se
 * guarda en el servidor y después lo que devuelve.
 *
 * Cada amistad llega con los perfiles de sus dos personas, que se guardan antes en Room para
 * poder enseñar su nombre y su avatar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AmistadesRepository(
    private val amistadDao: AmistadDao,
    private val servicio: AmistadesService,
    private val usuarios: UsuarioRepository,
    private val sesion: SesionRepository,
) {

    /** [consulta] para la cuenta de la sesión, o [vacio] si no hay sesión. */
    private fun <T> deLaSesion(vacio: T, consulta: (String) -> Flow<T>): Flow<T> =
        sesion.idCuenta.flatMapLatest { id -> if (id == null) flowOf(vacio) else consulta(id) }

    /** Amigos de la cuenta de la sesión, por orden alfabético. */
    fun misAmigos(): Flow<List<UsuarioEntity>> = deLaSesion(emptyList(), amistadDao::observarAmigosDe)

    /** Amigos de cualquier persona, para su perfil. Hay que [refrescarAmigosDe] antes. */
    fun amigosDe(id: String): Flow<List<UsuarioEntity>> = amistadDao.observarAmigosDe(id)

    /** Solicitudes que ha recibido la cuenta de la sesión y no ha contestado. */
    fun solicitudesRecibidas(): Flow<List<SolicitudConPersona>> =
        deLaSesion(emptyList(), amistadDao::observarSolicitudesRecibidas)

    /** Solicitudes que ha enviado la cuenta de la sesión y siguen sin contestar. */
    fun solicitudesEnviadas(): Flow<List<SolicitudConPersona>> =
        deLaSesion(emptyList(), amistadDao::observarSolicitudesEnviadas)

    /**
     * La cuenta de la sesión y sus amigos. El muro de amigos (RF-31) enseña solo lo que
     * publican estas personas.
     */
    fun circulo(): Flow<Set<String>> =
        deLaSesion(emptySet()) { id -> amistadDao.observarIdsDeAmigos(id).map { it.toSet() + id } }

    /** Relación con [otra] persona, para el botón de su perfil. */
    fun relacionCon(otra: String): Flow<RelacionAmistad> = sesion.idCuenta.flatMapLatest { yo ->
        if (yo == null) {
            flowOf(RelacionAmistad.Ninguna)
        } else {
            amistadDao.observarEntre(yo, otra).map { relacionEntre(yo, otra, it) }
        }
    }

    /** Descarga las amistades y solicitudes de la cuenta de la sesión. */
    suspend fun refrescarMias(): ResultadoRed<Unit> {
        val yo = usuarios.idActual() ?: return ResultadoRed.SesionCaducada
        return llamarAlServidor { servicio.de(participa(yo)) }.map { filas ->
            guardarPerfiles(filas)
            amistadDao.sincronizarTodasDe(yo, filas.map { it.aEntidad() })
        }
    }

    /** Descarga los amigos de otra persona (solo las amistades aceptadas, que son públicas). */
    suspend fun refrescarAmigosDe(id: String): ResultadoRed<Unit> =
        llamarAlServidor { servicio.de(participa(id), filtroAceptada = "is.true") }.map { filas ->
            guardarPerfiles(filas)
            amistadDao.sincronizarAmistadesDe(id, filas.map { it.aEntidad() })
        }

    /** Pide amistad a [id]. Queda pendiente hasta que la acepte. */
    suspend fun pedirAmistad(id: String): ResultadoRed<Unit> =
        guardarFilaDevuelta(llamarAlServidor { servicio.pedir(NuevaAmistadDto(id)) })

    /** Acepta una solicitud recibida. */
    suspend fun aceptar(idAmistad: String): ResultadoRed<Unit> =
        guardarFilaDevuelta(llamarAlServidor { servicio.aceptar(igual(idAmistad), AceptarAmistadDto()) })

    /**
     * Rechaza o cancela una solicitud, o deja de ser amigos. Si el servidor no devuelve la fila,
     * ya la había borrado la otra persona: también se quita de Room.
     */
    suspend fun borrar(idAmistad: String): ResultadoRed<Unit> =
        llamarAlServidor { servicio.borrar(igual(idAmistad)) }.map { amistadDao.borrar(idAmistad) }

    private suspend fun guardarPerfiles(filas: List<AmistadDto>) {
        usuarios.guardarDelServidor(filas.flatMap { listOfNotNull(it.solicitante, it.destinatario) }.distinctBy { it.id })
    }

    /** Con `Prefer: return=representation` el servidor devuelve la fila; si no, no había permiso. */
    private suspend fun guardarFilaDevuelta(resultado: ResultadoRed<List<AmistadDto>>): ResultadoRed<Unit> =
        when (resultado) {
            is ResultadoRed.Fallo -> resultado
            is ResultadoRed.Correcto -> {
                val fila = resultado.datos.firstOrNull()
                if (fila == null) {
                    ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
                } else {
                    guardarPerfiles(listOf(fila))
                    amistadDao.guardar(fila.aEntidad())
                    ResultadoRed.Correcto(Unit)
                }
            }
        }

    private companion object {
        const val CODIGO_SIN_PERMISO = "42501"
    }
}
