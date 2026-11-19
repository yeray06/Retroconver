package com.retroconver.ui.screens.muro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.PublicacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que muestra el muro (E4-06.2, #125). */
data class MuroUiState(
    /**
     * Publicaciones de todas las personas con su autor, la más reciente primero. Es `null` mientras
     * Room responde por primera vez; así el muro no parpadea con el mensaje de «vacío» al abrirse.
     */
    val publicaciones: List<PublicacionConAutor>? = null,
    /** Se está descargando del servidor (al abrir o al deslizar para actualizar). */
    val actualizando: Boolean = false,
    /** No se ha podido actualizar: id del mensaje, o `null`. Se sigue viendo la copia de Room. */
    val error: Int? = null,
)

/**
 * Estado del muro. Lee de Room y al abrirse descarga del servidor, como dice
 * docs/arquitectura.md: lo nuevo aparece solo porque Room avisa de cada cambio, y al publicar
 * algo aparece arriba sin recargar nada. No hay tiempo real: se actualiza al abrir la pantalla
 * o al deslizar hacia abajo.
 */
class MuroViewModel(private val repositorio: PublicacionRepository) : ViewModel() {

    private val actualizando = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)

    val estado: StateFlow<MuroUiState> = combine(repositorio.publicaciones(), actualizando, error) { lista, cargando, fallo ->
        MuroUiState(publicaciones = lista, actualizando = cargando, error = fallo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MuroUiState())

    init {
        actualizar()
    }

    /** Descarga el muro del servidor. La lista se actualiza sola porque lee de Room. */
    fun actualizar() {
        if (actualizando.value) return
        viewModelScope.launch {
            actualizando.value = true
            error.value = null
            val resultado = repositorio.refrescar()
            if (resultado is ResultadoRed.Fallo) error.value = resultado.mensaje()
            actualizando.value = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                MuroViewModel(app.container.publicacionRepository)
            }
        }
    }
}
