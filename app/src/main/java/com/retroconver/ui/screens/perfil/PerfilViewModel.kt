package com.retroconver.ui.screens.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.imagenes.AlmacenImagenes
import com.retroconver.data.local.UsuarioEntity
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
    /** Usuario local; `null` mientras Room responde por primera vez. */
    val usuario: UsuarioEntity? = null,
    /** Se está reescalando y guardando la foto nueva del avatar. */
    val guardandoFoto: Boolean = false,
    /** La última foto elegida no se ha podido usar. */
    val errorFoto: Boolean = false,
    /** Se está cerrando la sesión: el botón se desactiva. */
    val cerrandoSesion: Boolean = false,
)

/**
 * Estado de la pantalla de perfil (E3-06.1, #53). Room avisa de cada cambio del usuario,
 * así que al volver de editar el perfil o al cambiar la foto se ve enseguida.
 */
class PerfilViewModel(
    private val usuarios: UsuarioRepository,
    private val imagenes: AlmacenImagenes,
    private val sesion: SesionRepository,
) : ViewModel() {

    private val foto = MutableStateFlow(EstadoFoto())
    private val cerrandoSesion = MutableStateFlow(false)

    val estado: StateFlow<PerfilUiState> = combine(usuarios.usuarioLocal(), foto, cerrandoSesion) { usuario, estadoFoto, cerrando ->
        PerfilUiState(
            usuario = usuario,
            guardandoFoto = estadoFoto.guardando,
            errorFoto = estadoFoto.error,
            cerrandoSesion = cerrando,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PerfilUiState(),
    )

    /**
     * Pone como avatar la foto elegida en el selector (E3-06.3, #55): guarda una copia a 720p,
     * la asigna al usuario y borra el fichero del avatar anterior.
     */
    fun cambiarAvatar(origen: Uri) {
        if (foto.value.guardando) return
        viewModelScope.launch {
            foto.value = EstadoFoto(guardando = true)
            runCatching { imagenes.guardar(origen) }
                .onSuccess { nueva ->
                    val anterior = usuarios.cambiarAvatar(nueva)
                    imagenes.borrar(anterior)
                    foto.value = EstadoFoto()
                }
                .onFailure { foto.value = EstadoFoto(error = true) }
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

    private data class EstadoFoto(val guardando: Boolean = false, val error: Boolean = false)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                PerfilViewModel(app.container.usuarioRepository, app.container.almacenImagenes, app.container.sesionRepository)
            }
        }
    }
}
