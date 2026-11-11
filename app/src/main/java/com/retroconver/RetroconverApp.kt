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
        // Provisional hasta E4-05.1 (#121): el perfil y el muro todavía usan el usuario local de Room.
        // Cada vez que se abre una sesión se asegura que existe, porque al cerrar sesión se borra.
        ambitoApp.launch {
            container.sesionRepository.estado.collect { estado ->
                if (estado is EstadoSesion.Iniciada) {
                    container.usuarioRepository.asegurarUsuarioLocal(getString(R.string.usuario_local_nombre))
                }
            }
        }
    }
}
