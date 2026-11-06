package com.retroconver.ui.screens.muro

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.imagenes.AlmacenImagenes
import com.retroconver.data.repository.PublicacionRepository
import com.retroconver.data.repository.UsuarioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Formulario de nueva publicación con texto e imagen opcional (E3-07, #60 y #61).
 *
 * Los campos son estado de Compose (`mutableStateOf`) y no `StateFlow`, como recomienda Google
 * para los `TextField`: así el cursor no salta mientras se escribe.
 *
 * La foto se guarda a 720p en cuanto se elige, para poder enseñarla antes de publicar. Si al
 * salir del formulario no se ha publicado, esa copia se borra ([onCleared]) y no queda
 * ningún fichero suelto en la app.
 */
class NuevaPublicacionViewModel(
    private val publicaciones: PublicacionRepository,
    private val imagenes: AlmacenImagenes,
) : ViewModel() {

    var texto by mutableStateOf("")
        private set

    /** Error del texto; se calcula al pulsar «Publicar». */
    var error by mutableStateOf<ErrorPublicacion?>(null)
        private set

    /** URI de la copia a 720p de la foto elegida; `null` si no hay foto. */
    var imagenUri by mutableStateOf<String?>(null)
        private set

    /** Se está reescalando y guardando la foto elegida. */
    var guardandoImagen by mutableStateOf(false)
        private set

    /** La última foto elegida no se ha podido usar. */
    var errorImagen by mutableStateOf(false)
        private set

    /** Se está guardando la publicación: el botón se desactiva para no publicar dos veces. */
    var publicando by mutableStateOf(false)
        private set

    /** No se ha podido guardar la publicación. */
    var errorPublicar by mutableStateOf(false)
        private set

    /** Se ha publicado: la pantalla vuelve al muro. */
    var publicado by mutableStateOf(false)
        private set

    // Al escribir se quita el error, para no dejar un aviso que ya no aplica
    fun cambiarTexto(valor: String) {
        texto = valor
        error = null
        errorPublicar = false
    }

    /** Guarda una copia a 720p de la foto elegida en el selector y sustituye a la anterior, si había. */
    fun elegirImagen(origen: Uri) {
        if (guardandoImagen || publicando) return
        errorImagen = false
        guardandoImagen = true
        viewModelScope.launch {
            runCatching { imagenes.guardar(origen) }
                .onSuccess { nueva ->
                    val anterior = imagenUri
                    imagenUri = nueva
                    imagenes.borrar(anterior)
                }
                .onFailure { errorImagen = true }
            guardandoImagen = false
        }
    }

    /** Quita la foto antes de publicar y borra su copia. */
    fun quitarImagen() {
        if (guardandoImagen || publicando) return
        val anterior = imagenUri ?: return
        imagenUri = null
        errorImagen = false
        viewModelScope.launch { imagenes.borrar(anterior) }
    }

    /** Valida el texto y, si está bien, guarda la publicación (sin espacios en los bordes) en Room. */
    fun publicar() {
        if (guardandoImagen || publicando) return
        val resultado = validarPublicacion(texto)
        error = resultado
        if (resultado != null) return
        errorPublicar = false
        publicando = true
        viewModelScope.launch {
            runCatching {
                publicaciones.publicar(
                    usuarioId = UsuarioRepository.ID_USUARIO_LOCAL,
                    texto = texto.trim(),
                    imagenUri = imagenUri,
                )
            }
                .onSuccess { publicado = true }
                .onFailure { errorPublicar = true }
            publicando = false
        }
    }

    override fun onCleared() {
        // Si se sale sin publicar, la foto elegida ya no sirve. Se borra fuera de viewModelScope,
        // que a estas alturas ya está cancelado.
        val sinUsar = if (publicado) null else imagenUri
        if (sinUsar != null) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { imagenes.borrar(sinUsar) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                NuevaPublicacionViewModel(app.container.publicacionRepository, app.container.almacenImagenes)
            }
        }
    }
}
