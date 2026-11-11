package com.retroconver

import android.content.Context
import androidx.room.Room
import com.retroconver.data.imagenes.AlmacenImagenes
import com.retroconver.data.local.AppDatabase
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.ClienteSupabase
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.FuenteDeToken
import com.retroconver.data.remote.MonitorConexion
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.repository.PublicacionRepository
import com.retroconver.data.repository.UsuarioRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Contenedor de dependencias de la app. Crea la base de datos, el cliente de red y los
 * repositorios una sola vez y los comparte con todas las pantallas.
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

    /** Usuario local y datos de su perfil. */
    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(baseDeDatos.usuarioDao())
    }

    /** Publicaciones del muro y sus comentarios. */
    val publicacionRepository: PublicacionRepository by lazy {
        PublicacionRepository(baseDeDatos.publicacionDao(), baseDeDatos.comentarioDao())
    }

    /** Copias de las imágenes a 720p en el almacenamiento interno (avatar, publicaciones y galería). */
    val almacenImagenes: AlmacenImagenes by lazy { AlmacenImagenes(contextoApp) }

    // ---------- Servidor Supabase (E4-03, #113) ----------

    /** URL y clave pública del servidor, sacadas de local.properties. */
    val configuracionSupabase: ConfiguracionSupabase = ConfiguracionSupabase.desdeBuildConfig()

    /** Token de la sesión para cada petición. Hasta que haya sesión (E4-04) se usa solo la clave pública. */
    private val fuenteDeToken = FuenteDeToken { null }

    /** Cliente HTTP con la clave y el token en cada petición. */
    val clienteHttp: OkHttpClient by lazy {
        ClienteSupabase.crearClienteHttp(configuracionSupabase, fuenteDeToken)
    }

    private val retrofit: Retrofit by lazy {
        ClienteSupabase.crearRetrofit(configuracionSupabase.urlBase, clienteHttp)
    }

    /** Registro, inicio y cierre de sesión. */
    val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }

    /** Tabla `perfiles`. */
    val perfilService: PerfilService by lazy { retrofit.create(PerfilService::class.java) }

    /** Imágenes del bucket `imagenes`. */
    val almacenamientoService: AlmacenamientoService by lazy { retrofit.create(AlmacenamientoService::class.java) }

    /** Si el móvil tiene red, para la franja de «Sin conexión». */
    val monitorConexion: MonitorConexion by lazy { MonitorConexion(contextoApp) }

    // Cada módulo añade aquí su repositorio cuando lo cree.
}
