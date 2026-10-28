package com.retroconver

import android.app.Application

/**
 * Clase Application de Retroconver. Se crea una sola vez al arrancar la app,
 * antes que cualquier pantalla, y guarda el [AppContainer] con la base de datos
 * y los repositorios.
 */
class RetroconverApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
