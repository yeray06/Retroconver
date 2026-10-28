package com.retroconver

import android.content.Context
import androidx.room.Room
import com.retroconver.data.local.AppDatabase

/**
 * Contenedor de dependencias de la app. Crea la base de datos y los repositorios
 * una sola vez y los comparte con todas las pantallas.
 *
 * Lo crea [RetroconverApp] al arrancar, y se obtiene desde cualquier ViewModel así:
 *
 * ```
 * class MuroViewModel(private val repo: PublicacionRepository) : ViewModel() {
 *     companion object {
 *         val Factory = viewModelFactory {
 *             initializer {
 *                 val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RetroconverApp
 *                 MuroViewModel(app.container.publicacionRepository)
 *             }
 *         }
 *     }
 * }
 *
 * // En la pantalla:
 * @Composable
 * fun MuroScreen(viewModel: MuroViewModel = viewModel(factory = MuroViewModel.Factory)) { … }
 * ```
 *
 * Más detalle en docs/arquitectura.md, apartado 6.
 */
class AppContainer(context: Context) {

    private val contextoApp = context.applicationContext

    /** Base de datos Room. Se crea la primera vez que alguien la usa y ya no se vuelve a crear. */
    val baseDeDatos: AppDatabase by lazy {
        Room.databaseBuilder(contextoApp, AppDatabase::class.java, AppDatabase.NOMBRE)
            // Mientras desarrollamos, si cambia el esquema la base de datos se borra y se crea de nuevo.
            // Antes de la entrega final se sustituye por migraciones para no perder datos.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    // Cada módulo añade aquí su repositorio cuando lo cree, por ejemplo:
    // val publicacionRepository by lazy { PublicacionRepository(baseDeDatos.publicacionDao()) }
}
