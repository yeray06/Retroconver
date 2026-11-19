package com.retroconver.ui

import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickLabel
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.retroconver.R
import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.data.local.ComentarioEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.MensajeConAutor
import com.retroconver.data.local.MensajeEntity
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.local.PublicacionEntity
import com.retroconver.data.local.SolicitudConPersona
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.repository.RelacionAmistad
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
import com.retroconver.ui.screens.mensajes.MensajesContenido
import com.retroconver.ui.screens.mensajes.TablonContenido
import com.retroconver.ui.screens.mensajes.TablonUiState
import com.retroconver.ui.screens.muro.BorradoUi
import com.retroconver.ui.screens.muro.MuroContenido
import com.retroconver.ui.screens.muro.PublicacionContenido
import com.retroconver.ui.screens.muro.PublicacionUiState
import com.retroconver.ui.screens.perfil.AmistadesDelPerfil
import com.retroconver.ui.screens.perfil.BotonAmistadContenido
import com.retroconver.ui.screens.perfil.BotonAmistadUiState
import com.retroconver.ui.screens.perfil.BuscarAmigosContenido
import com.retroconver.ui.screens.perfil.BuscarAmigosUiState
import com.retroconver.ui.screens.perfil.EditarPerfilContenido
import com.retroconver.ui.screens.perfil.ErrorCampo
import com.retroconver.ui.screens.perfil.ErroresPerfil
import com.retroconver.ui.screens.perfil.PerfilContenido
import com.retroconver.ui.screens.perfil.PerfilUiState
import com.retroconver.ui.screens.perfil.PersonaEncontrada
import com.retroconver.ui.screens.personas.PerfilDePersonaContenido
import com.retroconver.ui.screens.personas.PersonasContenido
import com.retroconver.ui.screens.personas.PersonasUiState
import com.retroconver.ui.theme.EstiloDePerfil
import com.retroconver.ui.theme.FondoPerfil
import com.retroconver.ui.theme.RetroconverTheme
import com.retroconver.ui.theme.TemaPerfil
import com.retroconver.ui.theme.TipografiaPerfil
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
 *
 * Las comprobaciones automáticas necesitan Android 14 (API 34) o superior: en un emulador más
 * antiguo estas pruebas se saltan. La CI usa API 34.
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 34)
class AccesibilidadTest {

    @get:Rule
    val regla = createAndroidComposeRule<ComponentActivity>()

    private fun texto(@StringRes id: Int, vararg argumentos: Any): String = regla.activity.getString(id, *argumentos)

    /** Dibuja [contenido] con el tema de la app y le pasa las comprobaciones de accesibilidad. */
    @RequiresApi(34)
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
        regla.onNodeWithContentDescription(texto(R.string.acceso_descripcion_ver)).performClick()
        regla.onNodeWithContentDescription(texto(R.string.acceso_descripcion_ocultar)).performClick()
        regla.onNodeWithText(texto(R.string.acceso_entrar)).performClick()

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
        regla.onNodeWithText(texto(R.string.acceso_crear_cuenta)).performClick()
        regla.onNodeWithText(texto(R.string.acceso_ir_a_entrar)).performClick()

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
    fun perfilCompleto() = comprobar {
        PerfilContenido(estado = PerfilUiState(usuario = yo), alCambiarFoto = {}, alEditar = {}, alCerrarSesion = {})
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

    /** Cada tema clásico (EX-03, #30) tiene que pasar las mismas comprobaciones que el de la guía. */
    @Test
    fun perfilConCadaTemaYTipografia() {
        var tema by mutableStateOf(TemaPerfil.CLASICO)
        regla.setContent {
            RetroconverTheme {
                PerfilContenido(
                    estado = PerfilUiState(
                        usuario = yo.copy(tema = tema.clave, tipografia = TipografiaPerfil.entries[tema.ordinal % TipografiaPerfil.entries.size].clave),
                        errorFoto = R.string.red_sin_conexion,
                    ),
                    alCambiarFoto = {},
                    alEditar = {},
                    alCerrarSesion = {},
                )
            }
        }
        regla.enableAccessibilityChecks()
        TemaPerfil.entries.forEach { siguiente ->
            tema = siguiente
            regla.waitForIdle()
            regla.onRoot().tryPerformAccessibilityChecks()
        }
    }

    @Test
    fun editarElAspectoDelPerfil() = comprobar {
        EditarPerfilContenido(
            nombre = "Yeray",
            biografia = "",
            estado = "Personalizando mi perfil",
            errores = ErroresPerfil(),
            habilitado = true,
            alCambiarNombre = {},
            alCambiarBiografia = {},
            alCambiarEstado = {},
            alGuardar = {},
            alCancelar = {},
            tema = TemaPerfil.NOCHE,
            tipografia = TipografiaPerfil.MANUSCRITA,
        )
    }

    @Test
    fun cadaTemaSeAnunciaComoUnaOpcionMarcadaONo() {
        var elegido by mutableStateOf(TemaPerfil.CLASICO)
        regla.setContent {
            RetroconverTheme {
                EditarPerfilContenido(
                    nombre = "Yeray",
                    biografia = "",
                    estado = "",
                    errores = ErroresPerfil(),
                    habilitado = true,
                    alCambiarNombre = {},
                    alCambiarBiografia = {},
                    alCambiarEstado = {},
                    alGuardar = {},
                    alCancelar = {},
                    tema = elegido,
                    alCambiarTema = { elegido = it },
                )
            }
        }
        regla.onNodeWithText(texto(R.string.tema_lima)).performScrollTo().performClick()
        assertEquals(TemaPerfil.LIMA, elegido)
        regla.onNode(hasText(texto(R.string.tema_lima)) and isSelected(), useUnmergedTree = false).assertExists()
    }

    // ---------- Amistades (EX-04) ----------

    private val ana = UsuarioEntity(id = "ana", nombre = "Ana", fechaAlta = Instant.EPOCH)
    private val bea = UsuarioEntity(id = "bea", nombre = "Bea", fechaAlta = Instant.EPOCH)
    private val cris = UsuarioEntity(id = "cris", nombre = "Cris", fechaAlta = Instant.EPOCH)
    private val dani = UsuarioEntity(id = "dani", nombre = "Dani", fechaAlta = Instant.EPOCH)

    @Test
    fun perfilConAmigosYSolicitudes() = comprobar {
        PerfilContenido(
            estado = PerfilUiState(
                usuario = yo,
                amistades = AmistadesDelPerfil(
                    amigos = listOf(ana),
                    recibidas = listOf(SolicitudConPersona("s1", bea)),
                    enviadas = listOf(SolicitudConPersona("s2", cris)),
                    enCurso = "s1",
                    error = R.string.red_sin_conexion,
                ),
            ),
            alCambiarFoto = {},
            alEditar = {},
            alCerrarSesion = {},
            alAbrirPerfil = {},
        )
    }

    @Test
    fun cadaBotonDeUnaSolicitudDiceDeQuienEs() {
        var aceptada: String? = null
        regla.setContent {
            RetroconverTheme {
                PerfilContenido(
                    estado = PerfilUiState(
                        usuario = yo,
                        amistades = AmistadesDelPerfil(recibidas = listOf(SolicitudConPersona("s1", bea), SolicitudConPersona("s2", cris))),
                    ),
                    alCambiarFoto = {},
                    alEditar = {},
                    alCerrarSesion = {},
                    alAceptarAmistad = { aceptada = it },
                )
            }
        }
        regla.onNodeWithContentDescription(texto(R.string.amistades_aceptar_de, "Cris")).performScrollTo().performClick()
        assertEquals("s2", aceptada)
    }

    @Test
    fun botonDeAmistadEnCadaEstado() {
        var relacion by mutableStateOf<RelacionAmistad>(RelacionAmistad.Ninguna)
        regla.setContent {
            RetroconverTheme {
                BotonAmistadContenido(
                    estado = BotonAmistadUiState(relacion = relacion, error = R.string.red_sin_conexion),
                    alPedir = {},
                    alAceptar = {},
                    alBorrar = {},
                )
            }
        }
        regla.enableAccessibilityChecks()
        listOf(
            RelacionAmistad.Ninguna,
            RelacionAmistad.SolicitudEnviada("1"),
            RelacionAmistad.SolicitudRecibida("1"),
            RelacionAmistad.Amigos("1"),
        ).forEach { siguiente ->
            relacion = siguiente
            regla.waitForIdle()
            regla.onRoot().tryPerformAccessibilityChecks()
        }
    }

    @Test
    fun elPerfilPropioAbreElBuscadorDeAmigos() {
        var abierto = false
        regla.setContent {
            RetroconverTheme {
                PerfilContenido(
                    estado = PerfilUiState(usuario = yo),
                    alCambiarFoto = {},
                    alEditar = {},
                    alCerrarSesion = {},
                    alBuscarAmigos = { abierto = true },
                )
            }
        }
        regla.onNodeWithText(texto(R.string.amistades_buscar_amigos)).performScrollTo().performClick()
        assertTrue(abierto)
    }

    @Test
    fun buscadorDeAmigosConCadaRelacion() = comprobar {
        BuscarAmigosContenido(
            texto = "a",
            estado = BuscarAmigosUiState(
                resultados = listOf(
                    PersonaEncontrada(ana, RelacionAmistad.Ninguna),
                    PersonaEncontrada(bea, RelacionAmistad.SolicitudRecibida("s1")),
                    PersonaEncontrada(cris, RelacionAmistad.SolicitudEnviada("s2")),
                    PersonaEncontrada(dani, RelacionAmistad.Amigos("s3")),
                ),
                enCurso = "bea",
                error = R.string.red_sin_conexion,
            ),
            alCambiarTexto = {},
            alBuscar = {},
            alPedir = {},
            alAceptar = {},
            alCerrar = {},
        )
    }

    @Test
    fun buscadorDeAmigosSinResultados() = comprobar {
        BuscarAmigosContenido(
            texto = "Zoe",
            estado = BuscarAmigosUiState(resultados = emptyList()),
            alCambiarTexto = {},
            alBuscar = {},
            alPedir = {},
            alAceptar = {},
            alCerrar = {},
        )
    }

    @Test
    fun cadaBotonDelBuscadorDiceAQuienSePideAmistad() {
        var pedida: String? = null
        regla.setContent {
            RetroconverTheme {
                BuscarAmigosContenido(
                    texto = "a",
                    estado = BuscarAmigosUiState(
                        resultados = listOf(
                            PersonaEncontrada(ana, RelacionAmistad.Ninguna),
                            PersonaEncontrada(cris, RelacionAmistad.Ninguna),
                        ),
                    ),
                    alCambiarTexto = {},
                    alBuscar = {},
                    alPedir = { pedida = it },
                    alAceptar = {},
                    alCerrar = {},
                )
            }
        }
        regla.onNodeWithContentDescription(texto(R.string.amistades_anadir_a, "Cris")).performClick()
        assertEquals("cris", pedida)
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

        regla.onNodeWithContentDescription(texto(R.string.galeria_foto_n, 2, 2)).performClick()
        regla.onNodeWithContentDescription(texto(R.string.galeria_volver_descripcion)).assertExists()
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

    // ---------- Muro, perfiles de otras personas, tablón y color de fondo (E4-08, E4-09, E4-11, E4-13) ----------

    private fun publicacion(id: String, autor: UsuarioEntity, comentarios: Int = 0) = PublicacionConAutor(
        PublicacionEntity(id, autor.id, "Esta noche quedada en el MSN, que nadie falte.", timestamp = Instant.parse("2026-11-18T10:00:00Z")),
        autor = autor,
        numComentarios = comentarios,
    )

    private fun mensaje(id: String, de: UsuarioEntity, texto: String) = MensajeConAutor(
        MensajeEntity(id, "yo", de.id, texto, Instant.parse("2026-11-18T10:00:00Z")),
        de,
    )

    @Test
    fun muroConPublicacionesYAutoresQueAbrenSuPerfil() = comprobar {
        MuroContenido(publicaciones = listOf(publicacion("p1", ana, comentarios = 2), publicacion("p2", bea)), alAbrirPerfil = {})
    }

    @Test
    fun muroVacioConError() = comprobar {
        MuroContenido(publicaciones = emptyList(), error = R.string.red_sin_permiso)
    }

    @Test
    fun tocarElAutorDeUnaPublicacionAbreSuPerfil() {
        var abierto: String? = null
        regla.setContent {
            RetroconverTheme {
                MuroContenido(publicaciones = listOf(publicacion("p1", ana), publicacion("p2", bea)), alAbrirPerfil = { abierto = it })
            }
        }
        regla.onNode(hasClickLabel(texto(R.string.personas_ver_perfil_de, "Bea"))).performClick()
        assertEquals("bea", abierto)
    }

    @Test
    fun detalleDeUnaPublicacionConComentarios() = comprobar {
        PublicacionContenido(
            estado = PublicacionUiState(
                publicacion = publicacion("p1", yo, comentarios = 1),
                comentarios = listOf(
                    ComentarioConAutor(ComentarioEntity("c1", "p1", "ana", "¡Allí estaré!", Instant.parse("2026-11-18T11:00:00Z")), ana),
                ),
                actualizando = false,
                borrado = BorradoUi(miId = "yo"),
            ),
            alVolver = {},
            alActualizar = {},
            alAbrirPerfil = {},
        )
    }

    @Test
    fun tablonPropioConMensajesQueSePuedenBorrar() = comprobar {
        TablonContenido(
            estado = TablonUiState(
                mensajes = listOf(mensaje("m1", ana, "¡Feliz cumple!"), mensaje("m2", yo, "Gracias a todos")),
                miId = "yo",
                esMiTablon = true,
                borrando = "m1",
                error = R.string.red_sin_permiso,
            ),
            texto = "Hola",
            alCambiarTexto = {},
            alPublicar = {},
            alBorrar = {},
        )
    }

    @Test
    fun tablonVacioEnElTemaNoche() = comprobar {
        EstiloDePerfil(TemaPerfil.NOCHE, TipografiaPerfil.NORMAL) {
            TablonContenido(estado = TablonUiState(miId = "yo"), texto = "", alCambiarTexto = {}, alPublicar = {}, alBorrar = {})
        }
    }

    @Test
    fun cadaBorrarDelTablonDiceDeQuienEsElMensajeYPideConfirmacion() {
        var borrado: String? = null
        regla.setContent {
            RetroconverTheme {
                TablonContenido(
                    estado = TablonUiState(mensajes = listOf(mensaje("m1", ana, "Hola"), mensaje("m2", bea, "Adiós")), miId = "yo", esMiTablon = true),
                    texto = "",
                    alCambiarTexto = {},
                    alPublicar = {},
                    alBorrar = { borrado = it },
                )
            }
        }
        regla.onNodeWithContentDescription(texto(R.string.tablon_borrar_de, "Bea")).performClick()
        regla.onNodeWithText(texto(R.string.tablon_borrar_pregunta)).assertExists()
        regla.onNode(hasText(texto(R.string.tablon_borrar)) and hasAnyAncestor(isDialog())).performClick()
        assertEquals("m2", borrado)
    }

    @Test
    fun seccionMensajesConElTablonPropio() = comprobar {
        MensajesContenido(
            miId = "yo",
            tablon = {
                TablonContenido(
                    estado = TablonUiState(mensajes = listOf(mensaje("m1", ana, "¡Hola Yeray!")), miId = "yo", esMiTablon = true),
                    texto = "",
                    alCambiarTexto = {},
                    alPublicar = {},
                    alBorrar = {},
                )
            },
        )
    }

    @Test
    fun listaDePersonasConBuscador() = comprobar {
        PersonasContenido(
            texto = "a",
            estado = PersonasUiState(personas = listOf(ana, bea, dani)),
            alCambiarTexto = {},
            alBuscar = {},
            alAbrirPerfil = {},
            alVolver = {},
        )
    }

    @Test
    fun tocarUnaPersonaAbreSuPerfil() {
        var abierto: String? = null
        regla.setContent {
            RetroconverTheme {
                PersonasContenido(
                    texto = "",
                    estado = PersonasUiState(personas = listOf(ana, bea)),
                    alCambiarTexto = {},
                    alBuscar = {},
                    alAbrirPerfil = { abierto = it },
                    alVolver = {},
                )
            }
        }
        regla.onNodeWithText("Bea").performClick()
        assertEquals("bea", abierto)
    }

    @Test
    fun perfilDeOtraPersonaSinBotonesDeEditar() {
        regla.setContent {
            RetroconverTheme {
                PerfilDePersonaContenido(
                    estado = PerfilUiState(
                        usuario = ana.copy(biografia = "Fan de los tablones", tema = "lima", colorFondo = "lavanda"),
                        amistades = AmistadesDelPerfil(amigos = listOf(bea)),
                    ),
                    alVolver = {},
                    alReintentar = {},
                    alAbrirPerfil = {},
                    acciones = {
                        BotonAmistadContenido(
                            estado = BotonAmistadUiState(relacion = RelacionAmistad.Ninguna),
                            alPedir = {},
                            alAceptar = {},
                            alBorrar = {},
                        )
                    },
                    tablon = {
                        TablonContenido(estado = TablonUiState(miId = "yo"), texto = "", alCambiarTexto = {}, alPublicar = {}, alBorrar = {})
                    },
                )
            }
        }
        regla.enableAccessibilityChecks()
        regla.onRoot().tryPerformAccessibilityChecks()
        regla.onNodeWithText(texto(R.string.perfil_editar)).assertDoesNotExist()
        regla.onNodeWithText(texto(R.string.perfil_cerrar_sesion)).assertDoesNotExist()
        regla.onNodeWithText(texto(R.string.amistades_anadir)).assertExists()
    }

    @Test
    fun cadaColorDeFondoSeAnunciaConSuNombreYSeElige() {
        var elegido by mutableStateOf<FondoPerfil?>(null)
        regla.setContent {
            RetroconverTheme {
                EditarPerfilContenido(
                    nombre = "Yeray",
                    biografia = "",
                    estado = "",
                    errores = ErroresPerfil(),
                    habilitado = true,
                    alCambiarNombre = {},
                    alCambiarBiografia = {},
                    alCambiarEstado = {},
                    alGuardar = {},
                    alCancelar = {},
                    fondo = elegido,
                    alCambiarFondo = { elegido = it },
                )
            }
        }
        regla.onNodeWithContentDescription(texto(R.string.fondo_menta)).performScrollTo().performClick()
        assertEquals(FondoPerfil.MENTA, elegido)
        regla.onNode(hasContentDescription(texto(R.string.fondo_menta)) and isSelected()).assertExists()
    }
}
