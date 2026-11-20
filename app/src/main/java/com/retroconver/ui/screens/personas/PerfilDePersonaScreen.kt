package com.retroconver.ui.screens.personas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.AmistadesRepository
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.navigation.RutasPersonas
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.screens.galeria.CabeceraGaleria
import com.retroconver.ui.screens.mensajes.Tablon
import com.retroconver.ui.screens.perfil.AmistadesDelPerfil
import com.retroconver.ui.screens.perfil.BotonAmistad
import com.retroconver.ui.screens.perfil.PerfilContenido
import com.retroconver.ui.screens.perfil.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que enseña el perfil de otra persona. */
data class PerfilDePersonaUiState(
    /** Lo mismo que el perfil propio: la persona y sus amigos. */
    val perfil: PerfilUiState = PerfilUiState(),
    /** Es el perfil de la cuenta de la sesión: se abre el propio, con sus botones. */
    val esMiPerfil: Boolean = false,
)

/**
 * Perfil de otra persona (E4-08.1, #127): el mismo diseño que el propio, con su tema, su
 * tipografía y su fondo, pero sin botones de editar. Debajo de su nombre, el botón de amistad
 * (EX-04) y sus álbumes; abajo, su lista de amigos y su tablón (E4-09).
 */
@Composable
fun PerfilDePersonaScreen(
    alVolver: () -> Unit,
    alAbrirPerfil: (String) -> Unit,
    alVerAlbumes: (String) -> Unit,
    alEsMiPerfil: () -> Unit,
    viewModel: PerfilDePersonaViewModel = viewModel(factory = PerfilDePersonaViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.esMiPerfil) {
        if (estado.esMiPerfil) alEsMiPerfil()
    }
    PerfilDePersonaContenido(
        estado = estado.perfil,
        alVolver = alVolver,
        alReintentar = viewModel::actualizar,
        alAbrirPerfil = alAbrirPerfil,
        acciones = { id ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonAmistad(idPersona = id)
                RetroButton(texto = stringResource(R.string.personas_ver_albumes), onClick = { alVerAlbumes(id) })
            }
        },
        tablon = { id -> Tablon(idPerfil = id) },
    )
}

/** Cabecera con el nombre y «Volver», y debajo el perfil en modo de otra persona. */
@Composable
internal fun PerfilDePersonaContenido(
    estado: PerfilUiState,
    alVolver: () -> Unit,
    alReintentar: () -> Unit,
    alAbrirPerfil: (String) -> Unit,
    acciones: @Composable (idPersona: String) -> Unit,
    modifier: Modifier = Modifier,
    tablon: (@Composable (String) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGaleria(
            titulo = estado.usuario?.nombre ?: stringResource(R.string.personas_perfil),
            alVolver = alVolver,
        )
        PerfilContenido(
            estado = estado,
            alCambiarFoto = {},
            alEditar = {},
            alCerrarSesion = {},
            alReintentar = alReintentar,
            alAbrirPerfil = alAbrirPerfil,
            esPropio = false,
            accionesDeOtraPersona = { estado.usuario?.let { acciones(it.id) } },
            tablon = tablon,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Estado del perfil de otra persona (E4-08.1, #127). Lee de Room, así que se ve la copia aunque no haya red. */
class PerfilDePersonaViewModel(
    private val idPersona: String,
    private val usuarios: UsuarioRepository,
    private val amistades: AmistadesRepository,
) : ViewModel() {

    private val errorCarga = MutableStateFlow<Int?>(null)
    private val esMio = MutableStateFlow(false)

    val estado: StateFlow<PerfilDePersonaUiState> =
        combine(usuarios.usuario(idPersona), amistades.amigosDe(idPersona), errorCarga, esMio) { persona, amigos, error, mio ->
            PerfilDePersonaUiState(
                perfil = PerfilUiState(
                    usuario = persona,
                    errorCarga = if (persona == null) error else null,
                    amistades = AmistadesDelPerfil(amigos = amigos),
                ),
                esMiPerfil = mio,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilDePersonaUiState())

    init {
        viewModelScope.launch { esMio.value = usuarios.idActual() == idPersona }
        actualizar()
    }

    /** Descarga su perfil y sus amigos del servidor. La pantalla se actualiza sola porque lee de Room. */
    fun actualizar() {
        errorCarga.value = null
        viewModelScope.launch {
            val resultado = usuarios.refrescar(idPersona)
            if (resultado is ResultadoRed.Fallo) errorCarga.value = resultado.mensaje()
        }
        viewModelScope.launch { amistades.refrescarAmigosDe(idPersona) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                PerfilDePersonaViewModel(
                    idPersona = checkNotNull(createSavedStateHandle().get<String>(RutasPersonas.ARG_PERSONA)),
                    usuarios = app.container.usuarioRepository,
                    amistades = app.container.amistadesRepository,
                )
            }
        }
    }
}
