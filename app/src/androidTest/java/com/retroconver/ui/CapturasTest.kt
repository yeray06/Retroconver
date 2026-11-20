package com.retroconver.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.retroconver.R
import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.local.PublicacionEntity
import com.retroconver.data.local.SolicitudConPersona
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.repository.RelacionAmistad
import com.retroconver.navigation.BarraNavegacion
import com.retroconver.navigation.Destino
import com.retroconver.ui.components.RetroHeader
import com.retroconver.ui.screens.acceso.ErroresAcceso
import com.retroconver.ui.screens.acceso.InicioSesionContenido
import com.retroconver.ui.screens.acceso.RegistroContenido
import com.retroconver.ui.screens.galeria.AlbumContenido
import com.retroconver.ui.screens.galeria.AlbumUiState
import com.retroconver.ui.screens.galeria.AlbumesContenido
import com.retroconver.ui.screens.galeria.AlbumesUiState
import com.retroconver.ui.screens.galeria.FotoContenido
import com.retroconver.ui.screens.galeria.FotoUiState
import com.retroconver.ui.screens.galeria.ProgresoSubida
import com.retroconver.ui.screens.muro.MuroContenido
import com.retroconver.ui.screens.perfil.AmistadesDelPerfil
import com.retroconver.ui.screens.perfil.BuscarAmigosContenido
import com.retroconver.ui.screens.perfil.BuscarAmigosUiState
import com.retroconver.ui.screens.perfil.EditarPerfilContenido
import com.retroconver.ui.screens.perfil.ErroresPerfil
import com.retroconver.ui.screens.perfil.PerfilContenido
import com.retroconver.ui.screens.perfil.PerfilUiState
import com.retroconver.ui.screens.perfil.PersonaEncontrada
import com.retroconver.ui.theme.RetroconverTheme
import com.retroconver.ui.theme.TemaPerfil
import com.retroconver.ui.theme.TipografiaPerfil
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.Locale

/**
 * Capturas de las pantallas principales para el README (EF-01, #26).
 *
 * No son pruebas: dibujan cada pantalla con datos de ejemplo y guardan la imagen en
 * `Android/data/com.retroconver/files/capturas`. Solo se ejecutan si se pide con el argumento
 * `capturas=true` (lo hace .github/workflows/capturas.yml); en el resto de ejecuciones se saltan.
 */
@RunWith(AndroidJUnit4::class)
class CapturasTest {

    @get:Rule
    val regla = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun soloSiSePiden() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("capturas") == "true")
        // El emulador de la CI está en inglés: las fechas salen en español, como en un móvil de aquí
        Locale.setDefault(Locale.forLanguageTag("es-ES"))
    }

    private fun texto(@StringRes id: Int): String = regla.activity.getString(id)

    private val ahora = Instant.parse("2026-11-16T18:00:00Z")

    /** Imagen de ejemplo con un degradado, guardada como fichero para que Coil la cargue. */
    private fun imagen(nombre: String, de: Int, a: Int): String {
        val fichero = File(regla.activity.cacheDir, "ejemplo-$nombre.png")
        if (!fichero.exists()) {
            val bitmap = Bitmap.createBitmap(480, 480, Bitmap.Config.ARGB_8888)
            val lienzo = Canvas(bitmap)
            lienzo.drawRect(0f, 0f, 480f, 480f, Paint().apply { shader = LinearGradient(0f, 0f, 480f, 480f, de, a, Shader.TileMode.CLAMP) })
            lienzo.drawCircle(340f, 140f, 60f, Paint().apply { color = 0x66FFFFFF.toInt() })
            fichero.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        return fichero.toURI().toString()
    }

    private val yo by lazy {
        UsuarioEntity(
            id = "yo",
            nombre = "Yeray",
            biografia = "Echo de menos los tablones de Tuenti y las tardes en el Messenger.",
            estado = "Escuchando música en el MSN",
            avatarUri = imagen("avatar", 0xFF2B82DF.toInt(), 0xFF8AD4FF.toInt()),
            fechaAlta = Instant.parse("2026-11-01T10:00:00Z"),
        )
    }
    private val ana = UsuarioEntity(id = "ana", nombre = "Ana", fechaAlta = Instant.EPOCH)
    private val bea = UsuarioEntity(id = "bea", nombre = "Bea", fechaAlta = Instant.EPOCH)
    private val dani = UsuarioEntity(id = "dani", nombre = "Dani", fechaAlta = Instant.EPOCH)
    private val marta = UsuarioEntity(id = "marta", nombre = "Marta", fechaAlta = Instant.EPOCH)

    private val colores = listOf(
        0xFF0056B3 to 0xFF8AD4FF,
        0xFFFF9AD5 to 0xFF7A5CB8,
        0xFF2B5F1C to 0xFFD5EFC8,
        0xFFA8156A to 0xFFF9D3E5,
        0xFF004085 to 0xFFC5D7E8,
        0xFF3B97FF to 0xFFFFF4C2,
    )

    private fun fotos(): List<FotoEntity> = colores.mapIndexed { i, (de, a) ->
        FotoEntity(
            id = "f$i",
            albumId = "londres",
            propietarioId = "yo",
            ruta = "yo/f$i.png",
            uri = imagen("foto$i", de.toInt(), a.toInt()),
            fechaAnadida = ahora.minus(Duration.ofMinutes(i.toLong())),
        )
    }

    private val londres = AlbumEntity("londres", "yo", "Viaje a Londres", fechaCreacion = ahora)

    /**
     * Dibuja [contenido] dentro de la cabecera y la barra de navegación de la app, espera a que
     * carguen las imágenes y guarda la captura como `{nombre}.png`.
     */
    private fun capturar(
        nombre: String,
        seccion: Destino? = null,
        antes: () -> Unit = {},
        contenido: @Composable () -> Unit,
    ) {
        regla.setContent {
            RetroconverTheme {
                if (seccion == null) {
                    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) { contenido() }
                } else {
                    Scaffold(
                        topBar = { RetroHeader(titulo = stringResource(R.string.logo_cabecera)) },
                        bottomBar = { BarraEn(seccion) },
                        containerColor = MaterialTheme.colorScheme.background,
                    ) { espacio ->
                        Column(Modifier.padding(espacio)) { contenido() }
                    }
                }
            }
        }
        regla.waitForIdle()
        antes()
        // Coil carga las imágenes en segundo plano: se le da tiempo
        Thread.sleep(2_500)
        regla.waitForIdle()
        val imagen = regla.onRoot().captureToImage().asAndroidBitmap()
        val carpeta = File(regla.activity.getExternalFilesDir(null), "capturas").apply { mkdirs() }
        File(carpeta, "$nombre.png").outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /** La barra de navegación con [seccion] marcada. */
    @Composable
    private fun BarraEn(seccion: Destino) {
        val navegacion = rememberNavController()
        Column {
            NavHost(navegacion, startDestination = seccion.ruta, modifier = Modifier.size(0.dp)) {
                Destino.entries.forEach { destino -> composable(destino.ruta) {} }
            }
            BarraNavegacion(navegacion)
        }
    }

    @Test
    fun inicioDeSesion() = capturar("01-inicio-sesion") {
        InicioSesionContenido(
            hayConexion = true,
            email = "yeray@retroconver.es",
            contrasena = "secreta",
            errores = ErroresAcceso(),
            errorServidor = null,
            enviando = false,
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alEntrar = {},
            alIrARegistro = {},
        )
    }

    @Test
    fun registro() = capturar("02-registro") {
        RegistroContenido(
            hayConexion = true,
            nombre = "Yeray",
            email = "yeray@retroconver.es",
            contrasena = "",
            errores = ErroresAcceso(),
            errorServidor = null,
            enviando = false,
            emailPorConfirmar = null,
            alCambiarNombre = {},
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alCrearCuenta = {},
            alIrAEntrar = {},
        )
    }

    @Test
    fun muro() = capturar("03-muro", Destino.MURO) {
        MuroContenido(
            publicaciones = listOf(
                PublicacionConAutor(
                    PublicacionEntity("p1", "yo", "¡Ya tenemos galería! Mirad las fotos del viaje a Londres.", imagenUri = fotos()[0].uri, timestamp = ahora),
                    autor = yo,
                    numComentarios = 3,
                ),
                PublicacionConAutor(
                    PublicacionEntity("p2", "dani", "Esta noche quedada en el MSN, que nadie falte.", timestamp = ahora.minus(Duration.ofHours(2))),
                    autor = dani,
                    numComentarios = 1,
                ),
            ),
        )
    }

    @Test
    fun perfil() = capturar("04-perfil", Destino.PERFIL) {
        PerfilContenido(
            estado = PerfilUiState(
                usuario = yo,
                amistades = AmistadesDelPerfil(
                    amigos = listOf(ana, dani),
                    recibidas = listOf(SolicitudConPersona("s1", bea)),
                ),
            ),
            alCambiarFoto = {},
            alEditar = {},
            alCerrarSesion = {},
            alBuscarAmigos = {},
        )
    }

    @Test
    fun perfilConOtroTema() = capturar("05-perfil-tema-noche", Destino.PERFIL) {
        PerfilContenido(
            estado = PerfilUiState(
                usuario = yo.copy(
                    estado = "Personalizando mi perfil",
                    tema = TemaPerfil.NOCHE.clave,
                    tipografia = TipografiaPerfil.MAQUINA.clave,
                ),
                amistades = AmistadesDelPerfil(amigos = listOf(ana, bea, dani)),
            ),
            alCambiarFoto = {},
            alEditar = {},
            alCerrarSesion = {},
        )
    }

    @Test
    fun aspectoDelPerfil() = capturar(
        "06-aspecto-del-perfil",
        Destino.PERFIL,
        // Hasta abajo: la tipografía, la vista previa y los botones
        antes = { regla.onNodeWithText(texto(R.string.editar_guardar)).performScrollTo() },
    ) {
        EditarPerfilContenido(
            nombre = "Yeray",
            biografia = yo.biografia,
            estado = "Personalizando mi perfil",
            errores = ErroresPerfil(),
            habilitado = true,
            alCambiarNombre = {},
            alCambiarBiografia = {},
            alCambiarEstado = {},
            alGuardar = {},
            alCancelar = {},
            tema = TemaPerfil.CHICLE,
            tipografia = TipografiaPerfil.MANUSCRITA,
        )
    }

    @Test
    fun albumes() = capturar("07-galeria", Destino.GALERIA) {
        val fotos = fotos()
        AlbumesContenido(
            estado = AlbumesUiState(
                esMia = true,
                albumes = listOf(
                    AlbumConPortada(londres, numFotos = fotos.size, portadaUri = fotos[0].uri),
                    AlbumConPortada(londres.copy(id = "fiesta", titulo = "Fiesta Nochevieja 2008"), numFotos = 4, portadaUri = fotos[1].uri),
                    AlbumConPortada(londres.copy(id = "playa", titulo = "Verano en la playa"), numFotos = 2, portadaUri = fotos[2].uri),
                ),
            ),
            alAbrirAlbum = {},
            alCrearAlbum = {},
            alActualizar = {},
        )
    }

    @Test
    fun album() = capturar("08-album", Destino.GALERIA) {
        AlbumContenido(
            estado = AlbumUiState(
                cargado = true,
                album = londres,
                fotos = fotos(),
                esMio = true,
                subida = ProgresoSubida(2, 5),
            ),
            alVolver = {},
            alAbrirFoto = {},
            alAnadirFotos = {},
            alActualizar = {},
        )
    }

    @Test
    fun foto() = capturar("09-foto", Destino.GALERIA) {
        FotoContenido(
            estado = FotoUiState(foto = fotos()[1], tituloAlbum = londres.titulo, esMia = true),
            alVolver = {},
            alBorrar = {},
        )
    }

    @Test
    fun buscarAmigos() = capturar("10-buscar-amigos", Destino.PERFIL) {
        BuscarAmigosContenido(
            texto = "a",
            estado = BuscarAmigosUiState(
                resultados = listOf(
                    PersonaEncontrada(ana, RelacionAmistad.Amigos("a1")),
                    PersonaEncontrada(bea, RelacionAmistad.SolicitudRecibida("s1")),
                    PersonaEncontrada(dani, RelacionAmistad.Amigos("a2")),
                    PersonaEncontrada(marta, RelacionAmistad.Ninguna),
                ),
            ),
            alCambiarTexto = {},
            alBuscar = {},
            alPedir = {},
            alAceptar = {},
            alCerrar = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
