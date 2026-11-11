package com.retroconver.ui.screens.acceso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.sesion.ResultadoRegistro
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.launch

/**
 * Formulario de inicio de sesión (E4-04.2, #118).
 *
 * Si todo va bien no hace falta navegar: la sesión queda guardada y la app cambia sola al muro.
 * Los campos son estado de Compose, como en [com.retroconver.ui.screens.perfil.EditarPerfilViewModel].
 */
class InicioSesionViewModel(private val sesion: SesionRepository) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var errores by mutableStateOf(ErroresAcceso())
        private set

    /** Se está esperando al servidor: el botón se desactiva para no enviar dos veces. */
    var enviando by mutableStateOf(false)
        private set

    /** Id del texto del error del servidor (`strings_red.xml`), o `null`. */
    var errorServidor by mutableStateOf<Int?>(null)
        private set

    fun cambiarEmail(valor: String) {
        email = valor
        errores = errores.copy(email = null)
        errorServidor = null
    }

    fun cambiarContrasena(valor: String) {
        contrasena = valor
        errores = errores.copy(contrasena = null)
        errorServidor = null
    }

    fun entrar() {
        if (enviando) return
        val resultado = validarInicioSesion(email, contrasena)
        errores = resultado
        if (resultado.hayErrores) return
        enviando = true
        errorServidor = null
        viewModelScope.launch {
            val respuesta = sesion.iniciarSesion(email, contrasena)
            if (respuesta is ResultadoRed.Fallo) errorServidor = respuesta.mensaje()
            enviando = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                InicioSesionViewModel(app.container.sesionRepository)
            }
        }
    }
}

/**
 * Formulario de registro (E4-04.2, #118). Si el proyecto pide confirmar el email,
 * [emailPorConfirmar] guarda a qué dirección se ha enviado el enlace.
 */
class RegistroViewModel(private val sesion: SesionRepository) : ViewModel() {

    var nombre by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var errores by mutableStateOf(ErroresAcceso())
        private set
    var enviando by mutableStateOf(false)
        private set
    var errorServidor by mutableStateOf<Int?>(null)
        private set

    /** La cuenta está creada pero falta confirmar el email: la pantalla lo explica. */
    var emailPorConfirmar by mutableStateOf<String?>(null)
        private set

    fun cambiarNombre(valor: String) {
        nombre = valor
        errores = errores.copy(nombre = null)
        errorServidor = null
    }

    fun cambiarEmail(valor: String) {
        email = valor
        errores = errores.copy(email = null)
        errorServidor = null
    }

    fun cambiarContrasena(valor: String) {
        contrasena = valor
        errores = errores.copy(contrasena = null)
        errorServidor = null
    }

    fun crearCuenta() {
        if (enviando) return
        val resultado = validarRegistro(nombre, email, contrasena)
        errores = resultado
        if (resultado.hayErrores) return
        enviando = true
        errorServidor = null
        viewModelScope.launch {
            when (val respuesta = sesion.registrar(nombre, email, contrasena)) {
                is ResultadoRed.Correcto -> {
                    if (respuesta.datos == ResultadoRegistro.CONFIRMAR_EMAIL) {
                        emailPorConfirmar = email.trim()
                        contrasena = ""
                    }
                    // Con DENTRO la app cambia sola al muro
                }
                is ResultadoRed.Fallo -> errorServidor = respuesta.mensaje()
            }
            enviando = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                RegistroViewModel(app.container.sesionRepository)
            }
        }
    }
}
