package com.retroconver

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
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
import com.retroconver.data.sesion.RenovadorDeSesion
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/** Fichero de DataStore con la sesión (tokens e id de la cuenta). Uno solo para toda la app. */
private val Context.almacenSesion: DataStore<Preferences> by preferencesDataStore(name = "sesion")

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

    /**
     * Sesión de la persona usuaria (E4-04). Da el token a cada petición y, al cerrar sesión,
     * borra la copia local: la base de datos y las imágenes.
     */
    val sesionRepository: SesionRepository by lazy {
        SesionRepository(
            almacen = contextoApp.almacenSesion,
            auth = authService,
            alTerminarSesion = { borrarDatosLocales() },
        )
    }

    /** Cliente HTTP con la clave y el token de la sesión en cada petición. Si el token caduca, lo renueva. */
    val clienteHttp: OkHttpClient by lazy {
        ClienteSupabase.crearClienteHttp(configuracionSupabase, sesionRepository, RenovadorDeSesion(sesionRepository))
    }

    private val retrofit: Retrofit by lazy {
        ClienteSupabase.crearRetrofit(configuracionSupabase.urlBase, clienteHttp)
    }

    /**
     * Registro, inicio, renovación y cierre de sesión. Va en su propio cliente, sin el token
     * de la sesión: así renovar el token no depende del token que ha caducado.
     */
    val authService: AuthService by lazy {
        val clienteAuth = ClienteSupabase.crearClienteHttp(configuracionSupabase, FuenteDeToken { null })
        ClienteSupabase.crearRetrofit(configuracionSupabase.urlBase, clienteAuth).create(AuthService::class.java)
    }

    /** Tabla `perfiles`. */
    val perfilService: PerfilService by lazy { retrofit.create(PerfilService::class.java) }

    /** Imágenes del bucket `imagenes`. */
    val almacenamientoService: AlmacenamientoService by lazy { retrofit.create(AlmacenamientoService::class.java) }

    /** Si el móvil tiene red, para la franja de «Sin conexión». */
    val monitorConexion: MonitorConexion by lazy { MonitorConexion(contextoApp) }

    /** Al salir no quedan en el móvil datos de esa persona (RF-25): ni la copia de Room ni sus imágenes. */
    private suspend fun borrarDatosLocales() = withContext(Dispatchers.IO) {
        baseDeDatos.clearAllTables()
        almacenImagenes.borrarTodo()
    }

    // Cada módulo añade aquí su repositorio cuando lo cree.
}
