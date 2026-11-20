package com.retroconver.ui.screens.mensajes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.local.MensajeConAutor
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.MensajesRepository
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.ui.components.Avatar
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.screens.muro.ErrorPublicacion
import com.retroconver.ui.screens.muro.textoFecha
import com.retroconver.ui.theme.LocalColoresPerfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * Tablón de mensajes de un perfil (E4-09, #22), como el de Tuenti: cualquiera escribe en el
 * tablón de cualquiera, y cada mensaje lo puede borrar quien lo escribió o el dueño del tablón.
 * Sale en el perfil propio, en el de otras personas y en la sección Mensajes.
 */

/** Lo que enseña el tablón. */
data class TablonUiState(
    /** Mensajes con su autor, el más reciente primero. */
    val mensajes: List<MensajeConAutor> = emptyList(),
    /** Id de la cuenta de la sesión: puede borrar lo que ha escrito. */
    val miId: String? = null,
    /** Es el tablón de la cuenta de la sesión: puede borrar cualquier mensaje. */
    val esMiTablon: Boolean = false,
    /** Se está publicando un mensaje: el botón se desactiva. */
    val enviando: Boolean = false,
    /** Mensaje que se está borrando. */
    val borrando: String? = null,
    /** No se ha podido descargar, escribir o borrar: id del mensaje de error, o `null`. */
    val error: Int? = null,
)

/** Tablón de [idPerfil], con su propio ViewModel. Se pone dentro de un perfil: `Tablon(idPerfil = id)`. */
@Composable
fun Tablon(idPerfil: String, modifier: Modifier = Modifier) {
    val viewModel: TablonViewModel = viewModel(key = "tablon-$idPerfil", factory = TablonViewModel.factory(idPerfil))
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    // El texto vive en la pantalla y no en el ViewModel, para que el cursor no salte al escribir
    var texto by rememberSaveable(idPerfil) { mutableStateOf("") }
    TablonContenido(
        estado = estado,
        texto = texto,
        alCambiarTexto = { texto = it },
        alPublicar = { viewModel.escribir(texto) { texto = "" } },
        alBorrar = viewModel::borrar,
        modifier = modifier,
    )
}

/** Lo que dibuja [Tablon]. Usa los colores del perfil en el que está (EX-03). */
@Composable
internal fun TablonContenido(
    estado: TablonUiState,
    texto: String,
    alCambiarTexto: (String) -> Unit,
    alPublicar: () -> Unit,
    alBorrar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresPerfil.current
    var mensajeABorrar by rememberSaveable { mutableStateOf<String?>(null) }
    val errorTexto = validarMensaje(texto).takeIf { texto.isNotEmpty() }
    RetroCard(modifier = modifier.fillMaxWidth(), color = colores.tarjeta, borde = colores.bordeTarjeta) {
        Text(
            text = stringResource(R.string.tablon_titulo, estado.mensajes.size),
            style = MaterialTheme.typography.titleMedium,
            color = colores.titulo,
            modifier = Modifier.semantics { heading() },
        )
        OutlinedTextField(
            value = texto,
            onValueChange = alCambiarTexto,
            label = { Text(stringResource(R.string.tablon_escribir)) },
            isError = errorTexto == ErrorPublicacion.DEMASIADO_LARGO,
            minLines = 2,
            supportingText = {
                Text(
                    if (errorTexto == ErrorPublicacion.DEMASIADO_LARGO) {
                        stringResource(R.string.tablon_demasiado_largo, LimitesMensaje.TEXTO_MAX)
                    } else {
                        stringResource(R.string.tablon_contador, texto.trim().length, LimitesMensaje.TEXTO_MAX)
                    },
                )
            },
            // Los colores del perfil: en el tema Noche la tarjeta es oscura
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colores.texto,
                unfocusedTextColor = colores.texto,
                cursorColor = colores.texto,
                focusedBorderColor = colores.titulo,
                unfocusedBorderColor = colores.bordeTarjeta,
                focusedLabelColor = colores.titulo,
                unfocusedLabelColor = colores.textoSecundario,
                focusedSupportingTextColor = colores.textoSecundario,
                unfocusedSupportingTextColor = colores.textoSecundario,
                errorSupportingTextColor = colores.error,
                errorLabelColor = colores.error,
                errorBorderColor = colores.error,
                errorTextColor = colores.texto,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        Button(
            onClick = alPublicar,
            enabled = texto.isNotBlank() && errorTexto == null && !estado.enviando,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(stringResource(if (estado.enviando) R.string.tablon_publicando else R.string.tablon_publicar))
        }
        if (estado.error != null) {
            Text(
                text = stringResource(estado.error),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.error,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (estado.mensajes.isEmpty()) {
            Text(
                text = stringResource(R.string.tablon_vacio),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.textoSecundario,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        estado.mensajes.forEach { item ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colores.bordeTarjeta)
            val puedeBorrar = estado.esMiTablon || item.mensaje.autorId == estado.miId
            FilaMensaje(
                item = item,
                puedeBorrar = puedeBorrar,
                borrando = estado.borrando == item.mensaje.id,
                alBorrar = { mensajeABorrar = item.mensaje.id },
            )
        }
    }
    mensajeABorrar?.let { id ->
        AlertDialog(
            onDismissRequest = { mensajeABorrar = null },
            title = { Text(stringResource(R.string.tablon_borrar_pregunta)) },
            text = { Text(stringResource(R.string.tablon_borrar_explicacion)) },
            confirmButton = {
                TextButton(onClick = {
                    mensajeABorrar = null
                    alBorrar(id)
                }) { Text(stringResource(R.string.tablon_borrar)) }
            },
            dismissButton = {
                TextButton(onClick = { mensajeABorrar = null }) { Text(stringResource(R.string.tablon_cancelar)) }
            },
        )
    }
}

/** Un mensaje: avatar y nombre de quien lo escribe, cuándo, el texto y «Borrar» si se puede. */
@Composable
private fun FilaMensaje(
    item: MensajeConAutor,
    puedeBorrar: Boolean,
    borrando: Boolean,
    alBorrar: () -> Unit,
) {
    val colores = LocalColoresPerfil.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(nombre = item.autor.nombre, imagenUri = item.autor.avatarUri, tamano = 36.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.autor.nombre,
                style = MaterialTheme.typography.titleSmall,
                color = colores.nombre,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = textoFecha(item.mensaje.fechaCreacion),
                style = MaterialTheme.typography.bodySmall,
                color = colores.textoSecundario,
            )
        }
        Text(
            text = item.mensaje.texto,
            style = MaterialTheme.typography.bodyLarge,
            color = colores.texto,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (puedeBorrar) {
            val descripcion = stringResource(R.string.tablon_borrar_de, item.autor.nombre)
            RetroButton(
                texto = stringResource(if (borrando) R.string.tablon_borrando else R.string.tablon_borrar),
                onClick = { if (!borrando) alBorrar() },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics { contentDescription = descripcion },
            )
        }
    }
}

/** Estado del tablón de un perfil (E4-09, #73 y #74). Lee de Room, así que al escribir o borrar se actualiza solo. */
class TablonViewModel(
    private val idPerfil: String,
    private val mensajes: MensajesRepository,
    private val usuarios: UsuarioRepository,
) : ViewModel() {

    private val miId = MutableStateFlow<String?>(null)
    private val enviando = MutableStateFlow(false)
    private val borrando = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<Int?>(null)

    val estado: StateFlow<TablonUiState> =
        combine(mensajes.tablon(idPerfil), miId, enviando, borrando, error) { lista, yo, publicando, borrandoId, fallo ->
            TablonUiState(
                mensajes = lista,
                miId = yo,
                esMiTablon = yo == idPerfil,
                enviando = publicando,
                borrando = borrandoId,
                error = fallo,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TablonUiState())

    init {
        viewModelScope.launch { miId.value = usuarios.idActual() }
        actualizar()
    }

    /** Descarga el tablón del servidor. Sin conexión se sigue viendo la última copia. */
    fun actualizar() {
        viewModelScope.launch {
            val resultado = mensajes.refrescar(idPerfil)
            // Sin conexión ya avisa la franja amarilla
            error.value = (resultado as? ResultadoRed.Fallo)?.takeUnless { it == ResultadoRed.SinConexion }?.mensaje()
        }
    }

    /** Publica [texto] en el tablón y, si va bien, llama a [alTerminar] (para vaciar el campo). */
    fun escribir(texto: String, alTerminar: () -> Unit) {
        if (validarMensaje(texto) != null || enviando.value) return
        viewModelScope.launch {
            enviando.value = true
            error.value = null
            when (val resultado = mensajes.escribir(idPerfil, texto)) {
                is ResultadoRed.Correcto -> alTerminar()
                is ResultadoRed.Fallo -> error.value = resultado.mensaje()
            }
            enviando.value = false
        }
    }

    /** Borra un mensaje, después de que la persona lo haya confirmado. */
    fun borrar(idMensaje: String) {
        if (borrando.value != null) return
        viewModelScope.launch {
            borrando.value = idMensaje
            error.value = null
            val resultado = mensajes.borrar(idMensaje)
            if (resultado is ResultadoRed.Fallo) error.value = resultado.mensaje()
            borrando.value = null
        }
    }

    companion object {
        fun factory(idPerfil: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                TablonViewModel(idPerfil, app.container.mensajesRepository, app.container.usuarioRepository)
            }
        }
    }
}
