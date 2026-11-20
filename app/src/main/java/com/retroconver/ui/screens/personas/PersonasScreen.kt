package com.retroconver.ui.screens.personas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.screens.galeria.CabeceraGaleria
import com.retroconver.ui.screens.perfil.FilaPersona
import com.retroconver.ui.screens.perfil.LimitesPerfil
import com.retroconver.ui.theme.RetroGrayText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que enseña la lista de personas. */
data class PersonasUiState(
    /** `null` mientras llega la primera respuesta. */
    val personas: List<UsuarioEntity>? = null,
    val buscando: Boolean = false,
    val error: Int? = null,
)

/**
 * Personas de la red con buscador (E4-08.2, #128). Al abrirse salen todas por orden alfabético;
 * al buscar, solo las que tienen ese texto en el nombre. Tocar una abre su perfil.
 */
@Composable
fun PersonasScreen(
    alVolver: () -> Unit,
    alAbrirPerfil: (String) -> Unit,
    viewModel: PersonasViewModel = viewModel(factory = PersonasViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    // El texto vive en la pantalla y no en el ViewModel, para que el cursor no salte al escribir
    var texto by rememberSaveable { mutableStateOf("") }
    PersonasContenido(
        texto = texto,
        estado = estado,
        alCambiarTexto = { texto = it.take(LimitesPerfil.NOMBRE_MAX) },
        alBuscar = { viewModel.buscar(texto) },
        alAbrirPerfil = alAbrirPerfil,
        alVolver = alVolver,
    )
}

@Composable
internal fun PersonasContenido(
    texto: String,
    estado: PersonasUiState,
    alCambiarTexto: (String) -> Unit,
    alBuscar: () -> Unit,
    alAbrirPerfil: (String) -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGaleria(titulo = stringResource(R.string.personas_titulo), alVolver = alVolver)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                RetroCard {
                    Text(
                        text = stringResource(R.string.personas_explicacion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RetroGrayText,
                    )
                    OutlinedTextField(
                        value = texto,
                        onValueChange = alCambiarTexto,
                        label = { Text(stringResource(R.string.personas_buscar_nombre)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { if (!estado.buscando) alBuscar() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = alBuscar, enabled = !estado.buscando) {
                            Text(stringResource(if (estado.buscando) R.string.personas_buscando else R.string.personas_buscar))
                        }
                    }
                    val aviso = when {
                        estado.error != null -> stringResource(estado.error)
                        estado.personas?.isEmpty() == true -> stringResource(R.string.personas_nadie)
                        else -> null
                    }
                    if (aviso != null) {
                        Text(
                            text = aviso,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (estado.error != null) MaterialTheme.colorScheme.error else RetroGrayText,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }
            }
            items(estado.personas.orEmpty(), key = { it.id }) { persona ->
                val etiqueta = stringResource(R.string.amistades_ver_perfil)
                RetroCard(
                    modifier = Modifier.clickable(role = Role.Button, onClickLabel = etiqueta) { alAbrirPerfil(persona.id) },
                ) {
                    FilaPersona(persona = persona)
                }
            }
        }
    }
}

/** Estado de la lista de personas (E4-08.2, #128). */
class PersonasViewModel(private val usuarios: UsuarioRepository) : ViewModel() {

    private val personas = MutableStateFlow<List<UsuarioEntity>?>(null)
    private val buscando = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)

    val estado: StateFlow<PersonasUiState> = combine(personas, buscando, error) { lista, cargando, fallo ->
        PersonasUiState(personas = lista, buscando = cargando, error = fallo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonasUiState())

    init {
        buscar("")
    }

    /** Las personas cuyo nombre contiene [texto], o todas si está vacío. */
    fun buscar(texto: String) {
        if (buscando.value) return
        viewModelScope.launch {
            buscando.value = true
            error.value = null
            val resultado = usuarios.personas(texto)
            if (resultado is ResultadoRed.Correcto) personas.value = resultado.datos
            if (resultado is ResultadoRed.Fallo) error.value = resultado.mensaje()
            buscando.value = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                PersonasViewModel(app.container.usuarioRepository)
            }
        }
    }
}
