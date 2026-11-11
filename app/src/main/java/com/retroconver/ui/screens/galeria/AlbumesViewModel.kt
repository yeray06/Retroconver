package com.retroconver.ui.screens.galeria

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.GaleriaRepository
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.navigation.RutasGaleria
import com.retroconver.ui.screens.perfil.ErrorCampo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que muestra la lista de álbumes. */
data class AlbumesUiState(
    /** Álbumes con su portada y número de fotos; `null` mientras Room responde por primera vez. */
    val albumes: List<AlbumConPortada>? = null,
    /** Son los álbumes de la cuenta de la sesión: se pueden crear álbumes. */
    val esMia: Boolean = false,
    /** Nombre de la persona dueña, para «Álbumes de Dani». */
    val nombreDueno: String? = null,
    /** Se está descargando del servidor (deslizar para actualizar). */
    val actualizando: Boolean = false,
    /** No se ha podido actualizar: id del mensaje, o `null`. Se sigue viendo la copia de Room. */
    val error: Int? = null,
)

/**
 * Lista de álbumes de una persona (E4-10.2, #69). Sin [propietarioPedido] son los de la cuenta
 * de la sesión; con él, los de otra persona en solo lectura (se abre desde su perfil, E4-08).
 *
 * Lee de Room y al abrirse descarga del servidor, como dice docs/arquitectura.md.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlbumesViewModel(
    private val galeria: GaleriaRepository,
    private val usuarios: UsuarioRepository,
    private val propietarioPedido: String?,
) : ViewModel() {

    private val idPropio = MutableStateFlow<String?>(null)
    private val actualizando = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)

    /** De quién son los álbumes: la persona pedida o, si no hay, la de la sesión. */
    private val propietario = idPropio.map { propietarioPedido ?: it }.filterNotNull().distinctUntilChanged()

    val estado: StateFlow<AlbumesUiState> = propietario.flatMapLatest { id ->
        combine(galeria.albumesDe(id), usuarios.usuario(id), idPropio, actualizando, error) { albumes, dueno, propio, cargando, fallo ->
            AlbumesUiState(
                albumes = albumes,
                esMia = id == propio,
                nombreDueno = dueno?.nombre,
                actualizando = cargando,
                error = fallo,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlbumesUiState())

    // ---------- Formulario del álbum nuevo (estado de Compose, como los demás formularios) ----------

    var dialogoAbierto by mutableStateOf(false)
        private set
    var titulo by mutableStateOf("")
        private set
    var errorTitulo by mutableStateOf<ErrorCampo?>(null)
        private set
    var creando by mutableStateOf(false)
        private set
    var errorCrear by mutableStateOf<Int?>(null)
        private set

    /** Id del álbum recién creado: la pantalla lo abre y avisa con [albumAbierto]. */
    var albumCreado by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            idPropio.value = galeria.idActual()
            actualizar()
        }
    }

    /** Descarga los álbumes del servidor. La lista se actualiza sola porque lee de Room. */
    fun actualizar() {
        val id = propietarioPedido ?: idPropio.value ?: return
        if (actualizando.value) return
        viewModelScope.launch {
            actualizando.value = true
            error.value = null
            val resultado = galeria.refrescarAlbumesDe(id)
            if (resultado is ResultadoRed.Fallo) error.value = resultado.mensaje()
            actualizando.value = false
        }
    }

    fun abrirDialogo() {
        titulo = ""
        errorTitulo = null
        errorCrear = null
        dialogoAbierto = true
    }

    fun cerrarDialogo() {
        if (!creando) dialogoAbierto = false
    }

    fun cambiarTitulo(valor: String) {
        titulo = valor
        errorTitulo = null
        errorCrear = null
    }

    /** Valida el nombre y crea el álbum en el servidor. Si va bien, se abre para añadir fotos. */
    fun crearAlbum() {
        if (creando) return
        val problema = validarTituloAlbum(titulo)
        errorTitulo = problema
        if (problema != null) return
        creando = true
        viewModelScope.launch {
            when (val resultado = galeria.crearAlbum(titulo.trim())) {
                is ResultadoRed.Correcto -> {
                    dialogoAbierto = false
                    albumCreado = resultado.datos
                }
                is ResultadoRed.Fallo -> errorCrear = resultado.mensaje()
            }
            creando = false
        }
    }

    fun albumAbierto() {
        albumCreado = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                AlbumesViewModel(
                    galeria = app.container.galeriaRepository,
                    usuarios = app.container.usuarioRepository,
                    propietarioPedido = createSavedStateHandle().get<String>(RutasGaleria.ARG_PROPIETARIO),
                )
            }
        }
    }
}
