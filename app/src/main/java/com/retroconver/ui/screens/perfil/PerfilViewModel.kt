package com.retroconver.ui.screens.perfil

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.imagenes.AlmacenImagenes
import com.retroconver.data.local.SolicitudConPersona
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.AmistadesRepository
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que muestra la pantalla de perfil. */
data class PerfilUiState(
    /** Perfil de la sesión; `null` mientras no hay copia en Room. */
    val usuario: UsuarioEntity? = null,
    /** No hay copia del perfil y no se ha podido descargar: id del mensaje, o `null`. */
    val errorCarga: Int? = null,
    /** Se está reescalando y guardando la foto nueva del avatar. */
    val guardandoFoto: Boolean = false,
    /** La última foto elegida no se ha podido usar o subir: id del mensaje, o `null`. */
    val errorFoto: Int? = null,
    /** Se está cerrando la sesión: el botón se desactiva. */
    val cerrandoSesion: Boolean = false,
    /** Amigos y solicitudes de amistad (EX-04, #129). */
    val amistades: AmistadesDelPerfil = AmistadesDelPerfil(),
)

/** Amistades de la cuenta de la sesión que enseña su perfil (EX-04, #129). */
data class AmistadesDelPerfil(
    val amigos: List<UsuarioEntity> = emptyList(),
    val recibidas: List<SolicitudConPersona> = emptyList(),
    val enviadas: List<SolicitudConPersona> = emptyList(),
    /** Solicitud que se está contestando o cancelando: sus botones se desactivan. */
    val enCurso: String? = null,
    /** No se ha podido contestar la última: id del mensaje, o `null`. */
    val error: Int? = null,
)

/**
 * Estado de la pantalla de perfil (E3-06.1, #53). Room avisa de cada cambio del usuario,
 * así que al volver de editar el perfil o al cambiar la foto se ve enseguida.
 * Al abrirse descarga el perfil del servidor (E4-05.1, #121).
 */
class PerfilViewModel(
    private val usuarios: UsuarioRepository,
    private val imagenes: AlmacenImagenes,
    private val sesion: SesionRepository,
    private val amistades: AmistadesRepository,
) : ViewModel() {

    private val foto = MutableStateFlow(EstadoFoto())
    private val cerrandoSesion = MutableStateFlow(false)
    private val errorCarga = MutableStateFlow<Int?>(null)
    private val amistadEnCurso = MutableStateFlow<String?>(null)
    private val errorAmistad = MutableStateFlow<Int?>(null)

    private val datosAmistades = combine(
        amistades.misAmigos(),
        amistades.solicitudesRecibidas(),
        amistades.solicitudesEnviadas(),
        amistadEnCurso,
        errorAmistad,
    ) { amigos, recibidas, enviadas, enCurso, error ->
        AmistadesDelPerfil(amigos, recibidas, enviadas, enCurso, error)
    }

    val estado: StateFlow<PerfilUiState> = combine(
        usuarios.usuarioActual(),
        foto,
        cerrandoSesion,
        errorCarga,
        datosAmistades,
    ) { usuario, estadoFoto, cerrando, error, deAmistades ->
        PerfilUiState(
            usuario = usuario,
            // Si hay copia en Room se enseña aunque no se haya podido actualizar
            errorCarga = if (usuario == null) error else null,
            guardandoFoto = estadoFoto.guardando,
            errorFoto = estadoFoto.error,
            cerrandoSesion = cerrando,
            amistades = deAmistades,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PerfilUiState(),
    )

    init {
        actualizar()
    }

    /** Descarga el perfil y las amistades del servidor. La pantalla se actualiza sola porque lee de Room. */
    fun actualizar() {
        errorCarga.value = null
        viewModelScope.launch {
            val resultado = usuarios.refrescarActual()
            if (resultado is ResultadoRed.Fallo) errorCarga.value = resultado.mensaje()
        }
        viewModelScope.launch { amistades.refrescarMias() }
    }

    /** Acepta una solicitud de amistad recibida (EX-04, #129). */
    fun aceptarAmistad(idAmistad: String) = contestar(idAmistad) { amistades.aceptar(idAmistad) }

    /** Rechaza una solicitud recibida o cancela una enviada. */
    fun borrarSolicitud(idAmistad: String) = contestar(idAmistad) { amistades.borrar(idAmistad) }

    private fun contestar(idAmistad: String, accion: suspend () -> ResultadoRed<Unit>) {
        if (amistadEnCurso.value != null) return
        viewModelScope.launch {
            amistadEnCurso.value = idAmistad
            errorAmistad.value = null
            val resultado = accion()
            if (resultado is ResultadoRed.Fallo) {
                errorAmistad.value = resultado.mensaje()
                amistades.refrescarMias()
            }
            amistadEnCurso.value = null
        }
    }

    /**
     * Pone como avatar la foto elegida en el selector (E3-06.3 y E4-05.2): la reescala a 720p,
     * la sube al servidor y borra el avatar anterior. La copia a 720p del móvil se borra al
     * terminar, porque desde ese momento la imagen se carga del servidor.
     */
    fun cambiarAvatar(origen: Uri) {
        if (foto.value.guardando) return
        viewModelScope.launch {
            foto.value = EstadoFoto(guardando = true)
            val copia = runCatching { imagenes.guardar(origen) }.getOrNull()
            if (copia == null) {
                foto.value = EstadoFoto(error = R.string.perfil_error_foto)
                return@launch
            }
            val resultado = usuarios.cambiarAvatar(copia.toUri().toFile())
            imagenes.borrar(copia)
            foto.value = if (resultado is ResultadoRed.Fallo) EstadoFoto(error = resultado.mensaje()) else EstadoFoto()
        }
    }

    /** Oculta el mensaje de error de la foto. */
    fun ocultarErrorFoto() {
        foto.value = EstadoFoto()
    }

    /**
     * Cierra la sesión (E4-04.3, #119) después de que la persona lo confirme. No hace falta
     * navegar: al borrarse la sesión, la app vuelve sola al inicio de sesión.
     */
    fun cerrarSesion() {
        if (cerrandoSesion.value) return
        cerrandoSesion.value = true
        viewModelScope.launch { sesion.cerrarSesion() }
    }

    private data class EstadoFoto(val guardando: Boolean = false, val error: Int? = null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                PerfilViewModel(
                    app.container.usuarioRepository,
                    app.container.almacenImagenes,
                    app.container.sesionRepository,
                    app.container.amistadesRepository,
                )
            }
        }
    }
}
