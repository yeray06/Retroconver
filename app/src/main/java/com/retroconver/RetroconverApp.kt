package com.retroconver

import android.app.Application
import com.retroconver.data.sesion.EstadoSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase Application de Retroconver. Se crea una sola vez al arrancar la app,
 * antes que cualquier pantalla, y guarda el [AppContainer] con la base de datos
 * y los repositorios.
 */
class RetroconverApp : Application() {

    lateinit var container: AppContainer
        private set

    /** Corrutinas que duran lo mismo que la app, para tareas que no son de ninguna pantalla. */
    private val ambitoApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Al abrir la app con sesión, o al entrar, se descarga el perfil propio (E4-05.1, #121).
        // Sin conexión no pasa nada: se sigue con la copia de Room.
        ambitoApp.launch {
            container.sesionRepository.estado.collect { estado ->
                if (estado is EstadoSesion.Iniciada) container.usuarioRepository.refrescar(estado.idCuenta)
            }
        }
    }
}
