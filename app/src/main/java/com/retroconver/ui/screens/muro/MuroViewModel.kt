package com.retroconver.ui.screens.muro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.repository.PublicacionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Estado del muro. Room avisa de cada cambio, así que al publicar algo aparece arriba
 * sin recargar nada.
 */
class MuroViewModel(repositorio: PublicacionRepository) : ViewModel() {

    /**
     * Publicaciones con su autor, la más reciente primero.
     * Es `null` mientras Room responde por primera vez; así el muro no parpadea con el
     * mensaje de «vacío» al abrirse.
     */
    val publicaciones: StateFlow<List<PublicacionConAutor>?> = repositorio.publicaciones()
        .stateIn<List<PublicacionConAutor>?>(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                MuroViewModel(app.container.publicacionRepository)
            }
        }
    }
}
