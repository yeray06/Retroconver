package com.retroconver.ui.screens.galeria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.GaleriaRepository
import com.retroconver.navigation.RutasGaleria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que muestra la foto a pantalla completa. */
data class FotoUiState(
    val foto: FotoEntity? = null,
    /** Título del álbum, para la cabecera y para TalkBack. */
    val tituloAlbum: String = "",
    /** Es una foto propia: se puede borrar. */
    val esMia: Boolean = false,
    val borrando: Boolean = false,
    /** No se ha podido borrar: id del mensaje, o `null`. */
    val error: Int? = null,
    /** Ya se ha borrado: la pantalla vuelve al álbum. */
    val borrada: Boolean = false,
)

/** Foto a pantalla completa (E4-10.3, #70) y borrado con confirmación (E4-10.4, #71). */
@OptIn(ExperimentalCoroutinesApi::class)
class FotoViewModel(
    private val galeria: GaleriaRepository,
    fotoId: String,
) : ViewModel() {

    private val idPropio = MutableStateFlow<String?>(null)
    private val borrado = MutableStateFlow(EstadoBorrado())

    private val foto = galeria.foto(fotoId)
    private val tituloAlbum = foto.flatMapLatest { actual ->
        if (actual == null) flowOf("") else galeria.album(actual.albumId).map { it?.titulo.orEmpty() }
    }

    val estado: StateFlow<FotoUiState> = combine(foto, tituloAlbum, idPropio, borrado) { actual, titulo, propio, estadoBorrado ->
        FotoUiState(
            foto = actual,
            tituloAlbum = titulo,
            esMia = actual != null && actual.propietarioId == propio,
            borrando = estadoBorrado.borrando,
            error = estadoBorrado.error,
            borrada = estadoBorrado.borrada,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FotoUiState())

    init {
        viewModelScope.launch { idPropio.value = galeria.idActual() }
    }

    /** Borra la foto del servidor, su archivo y su copia, después de que la persona lo confirme. */
    fun borrar() {
        val actual = estado.value.foto ?: return
        if (borrado.value.borrando) return
        viewModelScope.launch {
            borrado.value = EstadoBorrado(borrando = true)
            borrado.value = when (val resultado = galeria.borrarFoto(actual)) {
                is ResultadoRed.Correcto -> EstadoBorrado(borrada = true)
                is ResultadoRed.Fallo -> EstadoBorrado(error = resultado.mensaje())
            }
        }
    }

    private data class EstadoBorrado(val borrando: Boolean = false, val error: Int? = null, val borrada: Boolean = false)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                FotoViewModel(
                    galeria = app.container.galeriaRepository,
                    fotoId = checkNotNull(createSavedStateHandle().get<String>(RutasGaleria.ARG_FOTO)),
                )
            }
        }
    }
}
