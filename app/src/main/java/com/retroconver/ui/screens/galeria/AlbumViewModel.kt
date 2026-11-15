package com.retroconver.ui.screens.galeria

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.GaleriaRepository
import com.retroconver.data.subidas.GestorSubidasFotos
import com.retroconver.navigation.RutasGaleria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Cuántas fotos de una tanda se llevan subidas («Subiendo 2 de 5…»).
 * Con `total = 0` la tanda está en cola o empezando.
 */
data class ProgresoSubida(val actual: Int, val total: Int)

/** Resultado de una tanda con fallos: cuántas no se han subido y el motivo de la última. */
data class FalloSubida(val fallidas: Int, val total: Int, val motivo: Int?, val id: String? = null)
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
 *
 * Las fotos se suben en segundo plano con WorkManager (#147): la subida sigue aunque se salga
 * del álbum, y al volver se ve por dónde va.
 */
class AlbumViewModel(
    private val galeria: GaleriaRepository,
    private val subidas: GestorSubidasFotos,
    private val albumId: String,
) : ViewModel() {

    private val idPropio = MutableStateFlow<String?>(null)
    private val actualizando = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)

    /** Id del último fallo de subida que la persona ya ha visto y ha ocultado. */
    private val falloOcultado = MutableStateFlow<String?>(null)

    private val datos = combine(galeria.album(albumId), galeria.fotos(albumId), idPropio) { album, fotos, propio ->
        AlbumUiState(cargado = true, album = album, fotos = fotos, esMio = album != null && album.propietarioId == propio)
    }

    val estado: StateFlow<AlbumUiState> = combine(
        datos,
        actualizando,
        error,
        subidas.estado(albumId),
        falloOcultado,
    ) { base, cargando, fallo, enSegundoPlano, ocultado ->
        base.copy(
            actualizando = cargando,
            error = fallo,
            subida = when {
                enSegundoPlano.subiendo -> ProgresoSubida(enSegundoPlano.actual, enSegundoPlano.total)
                enSegundoPlano.enCola -> ProgresoSubida(0, 0)
                else -> null
            },
            falloSubida = enSegundoPlano.ultimoFallo
                ?.takeIf { enSegundoPlano.idUltimoFallo != ocultado }
                ?.let { FalloSubida(it.fallidas, it.total, it.motivo, enSegundoPlano.idUltimoFallo) },
        )
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
     * Sube las fotos elegidas en el selector (E4-10.4, #71) en segundo plano (#147). Cada una se
     * reduce a 720p, se sube y aparece en la cuadrícula. Si alguna falla, se siguen subiendo las
     * demás y al final se dice cuántas no se han podido subir.
     */
    fun subirFotos(origenes: List<Uri>) {
        if (origenes.isEmpty()) return
        ocultarFalloSubida()
        subidas.subir(albumId, origenes)
    }

    fun ocultarFalloSubida() {
        estado.value.falloSubida?.id?.let { falloOcultado.value = it }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                AlbumViewModel(
                    galeria = app.container.galeriaRepository,
                    subidas = app.container.subidasFotos,
                    albumId = checkNotNull(createSavedStateHandle().get<String>(RutasGaleria.ARG_ALBUM)),
                )
            }
        }
    }
}
