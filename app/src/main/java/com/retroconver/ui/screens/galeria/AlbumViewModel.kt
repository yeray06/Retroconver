package com.retroconver.ui.screens.galeria

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.imagenes.AlmacenImagenes
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.GaleriaRepository
import com.retroconver.data.repository.ImagenParaSubir
import com.retroconver.navigation.RutasGaleria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Cuántas fotos de una tanda se llevan subidas («Subiendo 2 de 5…»). */
data class ProgresoSubida(val actual: Int, val total: Int)

/** Resultado de una tanda con fallos: cuántas no se han subido y el motivo de la última. */
data class FalloSubida(val fallidas: Int, val total: Int, val motivo: Int?)

/** Lo que muestra el detalle de un álbum. */
data class AlbumUiState(
    /** Ya ha respondido Room. Hasta entonces no se enseña ni el álbum ni el «no existe». */
    val cargado: Boolean = false,
    val album: AlbumEntity? = null,
    /** Fotos, la más reciente primero. */
    val fotos: List<FotoEntity> = emptyList(),
    /** Es un álbum de la cuenta de la sesión: se pueden añadir y borrar fotos. */
    val esMio: Boolean = false,
    val actualizando: Boolean = false,
    /** No se ha podido actualizar: id del mensaje, o `null`. */
    val error: Int? = null,
    /** Se está subiendo una tanda de fotos, o `null`. */
    val subida: ProgresoSubida? = null,
    val falloSubida: FalloSubida? = null,
)

/**
 * Detalle de un álbum en cuadrícula (E4-10.3, #70) y subida de fotos (E4-10.4, #71).
 * Los álbumes de otras personas se ven igual, pero sin poder añadir ni borrar.
 */
class AlbumViewModel(
    private val galeria: GaleriaRepository,
    private val imagenes: AlmacenImagenes,
    private val albumId: String,
) : ViewModel() {

    private val idPropio = MutableStateFlow<String?>(null)
    private val actualizando = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)
    private val subida = MutableStateFlow<ProgresoSubida?>(null)
    private val falloSubida = MutableStateFlow<FalloSubida?>(null)

    private val datos = combine(galeria.album(albumId), galeria.fotos(albumId), idPropio) { album, fotos, propio ->
        AlbumUiState(cargado = true, album = album, fotos = fotos, esMio = album != null && album.propietarioId == propio)
    }

    val estado: StateFlow<AlbumUiState> = combine(datos, actualizando, error, subida, falloSubida) { base, cargando, fallo, progreso, falloTanda ->
        base.copy(actualizando = cargando, error = fallo, subida = progreso, falloSubida = falloTanda)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlbumUiState())

    init {
        viewModelScope.launch { idPropio.value = galeria.idActual() }
        actualizar()
    }

    /** Descarga el álbum y sus fotos. Si se ha borrado en el servidor, desaparece también aquí. */
    fun actualizar() {
        if (actualizando.value) return
        viewModelScope.launch {
            actualizando.value = true
            error.value = null
            val resultado = galeria.refrescarAlbum(albumId)
            if (resultado is ResultadoRed.Fallo) error.value = resultado.mensaje()
            actualizando.value = false
        }
    }

    /**
     * Sube una tanda de fotos elegidas en el selector, de una en una (E4-10.4, #71). Cada una se
     * reescala a 720p, se sube y se borra la copia del móvil. Si alguna falla, se sigue con las
     * demás y al final se dice cuántas no se han podido subir.
     */
    fun subirFotos(origenes: List<Uri>) {
        if (subida.value != null || origenes.isEmpty()) return
        falloSubida.value = null
        viewModelScope.launch {
            var fallidas = 0
            var motivo: Int? = null
            origenes.forEachIndexed { indice, origen ->
                subida.value = ProgresoSubida(actual = indice + 1, total = origenes.size)
                val copia = runCatching { imagenes.guardar(origen) }.getOrNull()
                if (copia == null) {
                    fallidas++
                    motivo = R.string.galeria_error_foto_no_valida
                    return@forEachIndexed
                }
                val medidas = imagenes.medidas(copia)
                val resultado = galeria.subirFoto(
                    albumId,
                    ImagenParaSubir(Uri.parse(copia).toFile(), ancho = medidas?.first, alto = medidas?.second),
                )
                imagenes.borrar(copia)
                if (resultado is ResultadoRed.Fallo) {
                    fallidas++
                    motivo = resultado.mensaje()
                }
            }
            subida.value = null
            if (fallidas > 0) falloSubida.value = FalloSubida(fallidas, origenes.size, motivo)
        }
    }

    fun ocultarFalloSubida() {
        falloSubida.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                AlbumViewModel(
                    galeria = app.container.galeriaRepository,
                    imagenes = app.container.almacenImagenes,
                    albumId = checkNotNull(createSavedStateHandle().get<String>(RutasGaleria.ARG_ALBUM)),
                )
            }
        }
    }
}
