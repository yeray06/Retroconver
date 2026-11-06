package com.retroconver

import android.app.Application
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
        // La primera vez que se abre la app se crea el usuario local (RF-04). Las demás veces no hace nada.
        ambitoApp.launch {
            container.usuarioRepository.asegurarUsuarioLocal(getString(R.string.usuario_local_nombre))
        }
    }
}
