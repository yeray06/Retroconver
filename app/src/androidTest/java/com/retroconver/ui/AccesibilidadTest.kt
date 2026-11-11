package com.retroconver.ui

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTouchHeightIsAtLeast
import androidx.compose.ui.test.assertTouchWidthIsAtLeast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.retroconver.R
import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.ui.screens.acceso.ErrorAcceso
import com.retroconver.ui.screens.acceso.ErroresAcceso
import com.retroconver.ui.screens.acceso.InicioSesionContenido
import com.retroconver.ui.screens.acceso.RegistroContenido
import com.retroconver.ui.screens.galeria.AlbumContenido
import com.retroconver.ui.screens.galeria.AlbumUiState
import com.retroconver.ui.screens.galeria.AlbumesContenido
import com.retroconver.ui.screens.galeria.AlbumesUiState
import com.retroconver.ui.screens.galeria.FalloSubida
import com.retroconver.ui.screens.galeria.FotoContenido
import com.retroconver.ui.screens.galeria.FotoUiState
import com.retroconver.ui.screens.perfil.EditarPerfilContenido
import com.retroconver.ui.screens.perfil.ErrorCampo
import com.retroconver.ui.screens.perfil.ErroresPerfil
import com.retroconver.ui.screens.perfil.PerfilContenido
import com.retroconver.ui.screens.perfil.PerfilUiState
import com.retroconver.ui.theme.RetroconverTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Revisión de accesibilidad automática de Perfil, Galería, inicio de sesión y registro
 * (E4-12.2, #78).
 *
 * Cada prueba dibuja una pantalla y le pasa las comprobaciones del Accessibility Test
 * Framework, las mismas que hace el Accessibility Scanner de Google:
 * - etiquetas para TalkBack en imágenes y botones,
 * - contraste del texto,
 * - zonas táctiles de al menos 48 dp,
 * - y que no haya dos elementos pulsables en el mismo sitio.
 *
 * Si encuentra un error, la prueba falla y dice qué elemento es. Las pantallas se prueban con
 * su parte «Contenido», sin ViewModel ni servidor, en los estados más cargados (con errores,
 * sin conexión…).
 */
@RunWith(AndroidJUnit4::class)
class AccesibilidadTest {

    @get:Rule
    val regla = createAndroidComposeRule<ComponentActivity>()

    private fun texto(@StringRes id: Int, vararg argumentos: Any): String = regla.activity.getString(id, *argumentos)

    /** Dibuja [contenido] con el tema de la app y le pasa las comprobaciones de accesibilidad. */
    private fun comprobar(contenido: @Composable () -> Unit) {
        regla.setContent { RetroconverTheme { contenido() } }
        regla.enableAccessibilityChecks()
        regla.onRoot().tryPerformAccessibilityChecks()
    }

    private val yo = UsuarioEntity(
        id = "yo",
        nombre = "Yeray",
        biografia = "Echo de menos los tablones de Tuenti.",
        estado = "Escuchando música en el MSN",
        fechaAlta = Instant.parse("2026-11-01T10:00:00Z"),
    )

    // ---------- Inicio de sesión y registro ----------

    @Test
    fun inicioDeSesionSinConexionYConErrores() = comprobar {
        InicioSesionContenido(
            hayConexion = false,
            email = "yeray@correo",
            contrasena = "",
            errores = ErroresAcceso(email = ErrorAcceso.EMAIL_NO_VALIDO, contrasena = ErrorAcceso.OBLIGATORIO),
            errorServidor = R.string.red_sin_conexion,
            enviando = false,
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alEntrar = {},
            alIrARegistro = {},
        )
    }

    @Test
    fun registroConErrores() = comprobar {
        RegistroContenido(
            hayConexion = true,
            nombre = "",
            email = "yeray@retroconver.es",
            contrasena = "123",
            errores = ErroresAcceso(nombre = ErrorAcceso.OBLIGATORIO, contrasena = ErrorAcceso.CONTRASENA_CORTA),
            errorServidor = R.string.red_email_registrado,
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
    fun registroPendienteDeConfirmarElEmail() = comprobar {
        RegistroContenido(
            hayConexion = true,
            nombre = "Yeray",
            email = "yeray@retroconver.es",
            contrasena = "",
            errores = ErroresAcceso(),
            errorServidor = null,
            enviando = false,
            emailPorConfirmar = "yeray@retroconver.es",
            alCambiarNombre = {},
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alCrearCuenta = {},
            alIrAEntrar = {},
        )
    }

    /**
     * Lo que haría alguien con TalkBack: encontrar cada campo por lo que lee en voz alta,
     * escribir, mostrar la contraseña y pulsar «Entrar». Si algún elemento no tuviera etiqueta,
     * no se podría encontrar.
     */
    @Test
    fun elInicioDeSesionSeCompletaSoloConLoQueLeeTalkBack() {
        var credenciales: Pair<String, String>? = null
        regla.setContent {
            var email by remember { mutableStateOf("") }
            var contrasena by remember { mutableStateOf("") }
            RetroconverTheme {
                InicioSesionContenido(
                    hayConexion = true,
                    email = email,
                    contrasena = contrasena,
                    errores = ErroresAcceso(),
                    errorServidor = null,
                    enviando = false,
                    alCambiarEmail = { email = it },
                    alCambiarContrasena = { contrasena = it },
                    alEntrar = { credenciales = email to contrasena },
                    alIrARegistro = {},
                )
            }
        }

        regla.onNodeWithText(texto(R.string.acceso_email)).performTextInput("yeray@retroconver.es")
        regla.onNodeWithText(texto(R.string.acceso_contrasena)).performTextInput("secreta")
        regla.onNodeWithContentDescription(texto(R.string.acceso_descripcion_ver))
            .assertTouchHeightIsAtLeast(48.dp)
            .assertTouchWidthIsAtLeast(48.dp)
            .performClick()
        regla.onNodeWithContentDescription(texto(R.string.acceso_descripcion_ocultar)).performClick()
        regla.onNodeWithText(texto(R.string.acceso_entrar)).assertTouchHeightIsAtLeast(48.dp).performClick()

        assertEquals("yeray@retroconver.es" to "secreta", credenciales)
    }

    @Test
    fun elRegistroSeCompletaSoloConLoQueLeeTalkBack() {
        var cuentaCreada = false
        var irAEntrar = false
        regla.setContent {
            var nombre by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var contrasena by remember { mutableStateOf("") }
            RetroconverTheme {
                RegistroContenido(
                    hayConexion = true,
                    nombre = nombre,
                    email = email,
                    contrasena = contrasena,
                    errores = ErroresAcceso(),
                    errorServidor = null,
                    enviando = false,
                    emailPorConfirmar = null,
                    alCambiarNombre = { nombre = it },
                    alCambiarEmail = { email = it },
                    alCambiarContrasena = { contrasena = it },
                    alCrearCuenta = { cuentaCreada = nombre == "Yeray" && contrasena.length >= 6 },
                    alIrAEntrar = { irAEntrar = true },
                )
            }
        }

        regla.onNodeWithText(texto(R.string.acceso_nombre)).performTextInput("Yeray")
        regla.onNodeWithText(texto(R.string.acceso_email)).performTextInput("yeray@retroconver.es")
        regla.onNodeWithText(texto(R.string.acceso_contrasena)).performTextInput("secreta")
        regla.onNodeWithText(texto(R.string.acceso_crear_cuenta)).assertTouchHeightIsAtLeast(48.dp).performClick()
        regla.onNodeWithText(texto(R.string.acceso_ir_a_entrar)).assertTouchHeightIsAtLeast(48.dp).performClick()

        assertTrue(cuentaCreada)
        assertTrue(irAEntrar)
    }

    // ---------- Perfil ----------

    @Test
    fun perfilConErrorDeFoto() = comprobar {
        PerfilContenido(
            estado = PerfilUiState(usuario = yo, errorFoto = R.string.red_sin_conexion),
            alCambiarFoto = {},
            alEditar = {},
            alCerrarSesion = {},
        )
    }

    @Test
    fun perfilQueNoSeHaPodidoDescargar() = comprobar {
        PerfilContenido(
            estado = PerfilUiState(errorCarga = R.string.red_sin_conexion),
            alCambiarFoto = {},
            alEditar = {},
            alCerrarSesion = {},
        )
    }

    @Test
    fun losBotonesDelPerfilMidenAlMenos48dp() {
        regla.setContent {
            RetroconverTheme {
                PerfilContenido(estado = PerfilUiState(usuario = yo), alCambiarFoto = {}, alEditar = {}, alCerrarSesion = {})
            }
        }
        listOf(R.string.perfil_cambiar_foto, R.string.perfil_editar, R.string.perfil_cerrar_sesion).forEach { boton ->
            regla.onNodeWithText(texto(boton)).assertTouchHeightIsAtLeast(48.dp).assertTouchWidthIsAtLeast(48.dp)
        }
        regla.onNodeWithContentDescription(texto(R.string.avatar_de, "Yeray")).assertExists()
    }

    @Test
    fun editarPerfilConErrores() = comprobar {
        EditarPerfilContenido(
            nombre = "",
            biografia = "Echo de menos los tablones de Tuenti.",
            estado = "",
            errores = ErroresPerfil(nombre = ErrorCampo.OBLIGATORIO),
            habilitado = true,
            alCambiarNombre = {},
            alCambiarBiografia = {},
            alCambiarEstado = {},
            alGuardar = {},
            alCancelar = {},
            errorGuardar = R.string.red_sin_conexion,
        )
    }

    // ---------- Galería ----------

    private val album = AlbumEntity("a1", "yo", "Viaje a Londres", fechaCreacion = Instant.parse("2026-11-01T10:00:00Z"))

    private fun foto(id: String) = FotoEntity(
        id = id,
        albumId = "a1",
        propietarioId = "yo",
        ruta = "yo/$id.jpg",
        uri = "file:///no-existe/$id.jpg", // no hace falta que cargue: se comprueba la estructura
        fechaAnadida = Instant.parse("2026-11-01T10:00:00Z"),
    )

    @Test
    fun listaDeAlbumes() = comprobar {
        AlbumesContenido(
            estado = AlbumesUiState(
                esMia = true,
                albumes = listOf(
                    AlbumConPortada(album, numFotos = 2, portadaUri = "file:///no-existe/f1.jpg"),
                    AlbumConPortada(album.copy(id = "a2", titulo = "Fiesta Nochevieja 2008"), numFotos = 0, portadaUri = null),
                ),
                error = R.string.red_servidor_caido,
            ),
            alAbrirAlbum = {},
            alCrearAlbum = {},
            alActualizar = {},
        )
    }

    @Test
    fun galeriaVaciaDeOtraPersona() = comprobar {
        AlbumesContenido(
            estado = AlbumesUiState(esMia = false, nombreDueno = "Dani", albumes = emptyList()),
            alAbrirAlbum = {},
            alCrearAlbum = {},
            alActualizar = {},
        )
    }

    @Test
    fun albumPropioConFotosYUnaSubidaFallida() = comprobar {
        AlbumContenido(
            estado = AlbumUiState(
                cargado = true,
                album = album,
                fotos = listOf(foto("f1"), foto("f2"), foto("f3"), foto("f4")),
                esMio = true,
                falloSubida = FalloSubida(1, 3, R.string.red_imagen_demasiado_grande),
            ),
            alVolver = {},
            alAbrirFoto = {},
            alAnadirFotos = {},
            alActualizar = {},
        )
    }

    @Test
    fun cadaFotoDeLaCuadriculaSeAnunciaConSuPosicion() {
        var abierta: String? = null
        regla.setContent {
            RetroconverTheme {
                AlbumContenido(
                    estado = AlbumUiState(cargado = true, album = album, fotos = listOf(foto("f1"), foto("f2")), esMio = false),
                    alVolver = {},
                    alAbrirFoto = { abierta = it },
                    alAnadirFotos = {},
                    alActualizar = {},
                )
            }
        }

        regla.onNodeWithContentDescription(texto(R.string.galeria_foto_n, 2, 2))
            .assertTouchHeightIsAtLeast(48.dp)
            .performClick()
        regla.onNodeWithText(texto(R.string.galeria_volver)).assertTouchHeightIsAtLeast(48.dp)
        // Álbum ajeno: solo lectura
        regla.onNodeWithText(texto(R.string.galeria_anadir_fotos)).assertDoesNotExist()
        assertEquals("f2", abierta)
    }

    @Test
    fun fotoPropiaAPantallaCompleta() = comprobar {
        FotoContenido(
            estado = FotoUiState(foto = foto("f1"), tituloAlbum = "Viaje a Londres", esMia = true, error = R.string.red_sin_permiso),
            alVolver = {},
            alBorrar = {},
        )
    }
}
