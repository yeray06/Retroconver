package com.retroconver.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.AmistadesRepository
import com.retroconver.data.repository.RelacionAmistad
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.theme.LocalColoresPerfil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * Buscador de amigos (EX-04, #129): desde el perfil propio se busca a alguien por su nombre y se
 * le pide amistad sin tener que entrar en su perfil. Así las amistades funcionan aunque todavía
 * no exista la pantalla de perfiles ajenos (E4-08).
 */

/** Una persona encontrada y la relación que tiene con la cuenta de la sesión. */
data class PersonaEncontrada(val persona: UsuarioEntity, val relacion: RelacionAmistad)

/** Lo que enseña el buscador de amigos. */
data class BuscarAmigosUiState(
    /** `null` mientras no se ha buscado nada. */
    val resultados: List<PersonaEncontrada>? = null,
    val buscando: Boolean = false,
    /** Id de la persona a la que se está pidiendo amistad o aceptando: sus botones se desactivan. */
    val enCurso: String? = null,
    val error: Int? = null,
)

/** Ventana del buscador, encima del perfil propio. */
@Composable
fun BuscarAmigosDialog(
    alCerrar: () -> Unit,
    viewModel: BuscarAmigosViewModel = viewModel(factory = BuscarAmigosViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    // El texto vive en la pantalla y no en el ViewModel, para que el cursor no salte al escribir
    var texto by rememberSaveable { mutableStateOf("") }
    val cerrar = {
        viewModel.limpiar()
        alCerrar()
    }
    Dialog(onDismissRequest = cerrar) {
        BuscarAmigosContenido(
            texto = texto,
            estado = estado,
            alCambiarTexto = { texto = it.take(LimitesPerfil.NOMBRE_MAX) },
            alBuscar = { viewModel.buscar(texto) },
            alPedir = viewModel::pedir,
            alAceptar = viewModel::aceptar,
            alCerrar = cerrar,
        )
    }
}

/** Lo que dibuja [BuscarAmigosDialog]: el campo de búsqueda y las personas encontradas. */
@Composable
internal fun BuscarAmigosContenido(
    texto: String,
    estado: BuscarAmigosUiState,
    alCambiarTexto: (String) -> Unit,
    alBuscar: () -> Unit,
    alPedir: (String) -> Unit,
    alAceptar: (PersonaEncontrada) -> Unit,
    alCerrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresPerfil.current
    val sePuedeBuscar = texto.isNotBlank() && !estado.buscando
    RetroCard(modifier = modifier, color = colores.tarjeta, borde = colores.bordeTarjeta) {
        Text(
            text = stringResource(R.string.amistades_buscar_amigos),
            style = MaterialTheme.typography.titleMedium,
            color = colores.titulo,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.amistades_buscar_ayuda),
            style = MaterialTheme.typography.bodyMedium,
            color = colores.textoSecundario,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        OutlinedTextField(
            value = texto,
            onValueChange = alCambiarTexto,
            label = { Text(stringResource(R.string.amistades_buscar_nombre)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { if (sePuedeBuscar) alBuscar() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Button(onClick = alBuscar, enabled = sePuedeBuscar) {
                Text(stringResource(if (estado.buscando) R.string.amistades_buscando else R.string.amistades_buscar))
            }
            TextButton(onClick = alCerrar) { Text(stringResource(R.string.amistades_cerrar)) }
        }
        val aviso = when {
            estado.error != null -> stringResource(estado.error)
            estado.resultados?.isEmpty() == true -> stringResource(R.string.amistades_nadie)
            else -> null
        }
        if (aviso != null) {
            Text(
                text = aviso,
                style = MaterialTheme.typography.bodyMedium,
                color = if (estado.error != null) colores.error else colores.textoSecundario,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        val resultados = estado.resultados.orEmpty()
        if (resultados.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                resultados.forEach { encontrada ->
                    FilaEncontrada(
                        encontrada = encontrada,
                        ocupada = estado.enCurso == encontrada.persona.id,
                        alPedir = alPedir,
                        alAceptar = alAceptar,
                    )
                }
            }
        }
    }
}

/** Una persona encontrada con lo que se puede hacer según la relación que hay con ella. */
@Composable
private fun FilaEncontrada(
    encontrada: PersonaEncontrada,
    ocupada: Boolean,
    alPedir: (String) -> Unit,
    alAceptar: (PersonaEncontrada) -> Unit,
) {
    val colores = LocalColoresPerfil.current
    val nombre = encontrada.persona.nombre
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilaPersona(persona = encontrada.persona, modifier = Modifier.weight(1f))
        when (encontrada.relacion) {
            RelacionAmistad.EsMiPerfil -> Unit
            RelacionAmistad.Ninguna -> {
                val descripcion = stringResource(R.string.amistades_anadir_a, nombre)
                Button(
                    onClick = { alPedir(encontrada.persona.id) },
                    enabled = !ocupada,
                    modifier = Modifier.semantics { contentDescription = descripcion },
                ) {
                    Text(stringResource(if (ocupada) R.string.amistades_un_momento else R.string.amistades_anadir_corto))
                }
            }
            is RelacionAmistad.SolicitudRecibida -> {
                val descripcion = stringResource(R.string.amistades_aceptar_de, nombre)
                Button(
                    onClick = { alAceptar(encontrada) },
                    enabled = !ocupada,
                    modifier = Modifier.semantics { contentDescription = descripcion },
                ) {
                    Text(stringResource(if (ocupada) R.string.amistades_un_momento else R.string.amistades_aceptar))
                }
            }
            is RelacionAmistad.SolicitudEnviada -> Text(
                text = stringResource(R.string.amistades_enviada),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.textoSecundario,
            )
            is RelacionAmistad.Amigos -> Text(
                text = stringResource(R.string.amistades_son_amigos),
                style = MaterialTheme.typography.labelLarge,
                color = colores.titulo,
            )
        }
    }
}

/**
 * Estado del buscador de amigos (EX-04, #129). La relación con cada persona se lee de Room, así
 * que al pedir amistad o aceptar, su botón cambia solo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BuscarAmigosViewModel(
    private val usuarios: UsuarioRepository,
    private val amistades: AmistadesRepository,
) : ViewModel() {

    private val encontradas = MutableStateFlow<List<UsuarioEntity>?>(null)
    private val buscando = MutableStateFlow(false)
    private val enCurso = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<Int?>(null)

    private val resultados: Flow<List<PersonaEncontrada>?> = encontradas.flatMapLatest { lista ->
        when {
            lista == null -> flowOf<List<PersonaEncontrada>?>(null)
            lista.isEmpty() -> flowOf<List<PersonaEncontrada>?>(emptyList())
            else -> combine(
                lista.map { persona -> amistades.relacionCon(persona.id).map { PersonaEncontrada(persona, it) } },
            ) { filas -> filas.toList() }
        }
    }

    val estado: StateFlow<BuscarAmigosUiState> =
        combine(resultados, buscando, enCurso, error) { lista, cargando, ocupada, fallo ->
            BuscarAmigosUiState(resultados = lista, buscando = cargando, enCurso = ocupada, error = fallo)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BuscarAmigosUiState())

    init {
        // Para que cada persona salga con su relación al día (por si alguien ha aceptado)
        viewModelScope.launch { amistades.refrescarMias() }
    }

    /** Busca a las personas cuyo nombre contiene [texto]. */
    fun buscar(texto: String) {
        if (texto.isBlank() || buscando.value) return
        viewModelScope.launch {
            buscando.value = true
            error.value = null
            val resultado = usuarios.buscar(texto)
            if (resultado is ResultadoRed.Correcto) encontradas.value = resultado.datos
            if (resultado is ResultadoRed.Fallo) error.value = resultado.mensaje()
            buscando.value = false
        }
    }

    fun pedir(idPersona: String) = ejecutar(idPersona) { amistades.pedirAmistad(idPersona) }

    fun aceptar(encontrada: PersonaEncontrada) {
        val solicitud = encontrada.relacion as? RelacionAmistad.SolicitudRecibida ?: return
        ejecutar(encontrada.persona.id) { amistades.aceptar(solicitud.idAmistad) }
    }

    /** Al cerrar el buscador: la próxima vez se abre vacío. */
    fun limpiar() {
        encontradas.value = null
        error.value = null
    }

    private fun ejecutar(idPersona: String, accion: suspend () -> ResultadoRed<Unit>) {
        if (enCurso.value != null) return
        viewModelScope.launch {
            enCurso.value = idPersona
            error.value = null
            val resultado = accion()
            if (resultado is ResultadoRed.Fallo) {
                error.value = resultado.mensaje()
                // Puede que la otra persona haya hecho algo mientras tanto: se trae lo último
                amistades.refrescarMias()
            }
            enCurso.value = null
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                BuscarAmigosViewModel(app.container.usuarioRepository, app.container.amistadesRepository)
            }
        }
    }
}
