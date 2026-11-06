package com.retroconver.ui.screens.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Formulario para editar nombre, biografía y estado (E3-06.2, #54).
 *
 * Los campos de texto son estado de Compose (`mutableStateOf`) y no `StateFlow`, como
 * recomienda Google para los `TextField`: así el cursor no salta mientras se escribe.
 */
class EditarPerfilViewModel(private val usuarios: UsuarioRepository) : ViewModel() {

    var nombre by mutableStateOf("")
        private set
    var biografia by mutableStateOf("")
        private set
    var estado by mutableStateOf("")
        private set

    /** Errores que se muestran debajo de cada campo. Se calculan al pulsar «Guardar». */
    var errores by mutableStateOf(ErroresPerfil())
        private set

    /** Ya se han cargado los datos actuales del perfil en el formulario. */
    var cargado by mutableStateOf(false)
        private set

    /** Se está guardando: el botón se desactiva para no guardar dos veces. */
    var guardando by mutableStateOf(false)
        private set

    /** Se ha guardado: la pantalla vuelve atrás. */
    var guardado by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val usuario = usuarios.usuarioLocal().filterNotNull().first()
            nombre = usuario.nombre
            biografia = usuario.biografia
            estado = usuario.estado
            cargado = true
        }
    }

    // Al escribir en un campo se quita su error, para no dejar un aviso que ya no aplica
    fun cambiarNombre(valor: String) {
        nombre = valor
        errores = errores.copy(nombre = null)
    }

    fun cambiarBiografia(valor: String) {
        biografia = valor
        errores = errores.copy(biografia = null)
    }

    fun cambiarEstado(valor: String) {
        estado = valor
        errores = errores.copy(estado = null)
    }

    /** Valida y, si todo está bien, guarda en Room sin los espacios de los bordes. */
    fun guardar() {
        if (!cargado || guardando) return
        val resultado = validarPerfil(nombre, biografia, estado)
        errores = resultado
        if (resultado.hayErrores) return
        guardando = true
        viewModelScope.launch {
            usuarios.actualizarPerfil(nombre.trim(), biografia.trim(), estado.trim())
            guardando = false
            guardado = true
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                EditarPerfilViewModel(app.container.usuarioRepository)
            }
        }
    }
}
