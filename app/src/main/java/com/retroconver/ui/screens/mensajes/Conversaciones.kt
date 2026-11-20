package com.retroconver.ui.screens.mensajes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.repository.Conversacion
import com.retroconver.data.repository.MensajesDirectosRepository
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.ui.components.Avatar
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.screens.muro.textoFecha
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroLinkBlue
import com.retroconver.ui.theme.RetroTextDark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que enseña la lista de conversaciones privadas (EX-01, #28). */
data class ConversacionesUiState(
    val miId: String? = null,
    /** Una por persona, la del mensaje más reciente primero. */
    val conversaciones: List<Conversacion> = emptyList(),
)

/**
 * Conversaciones privadas de la cuenta (EX-01, #28), en la sección Mensajes. Cada fila abre el
 * chat con esa persona. Se descargan cada vez que se vuelve a la pantalla.
 */
@Composable
fun Conversaciones(alAbrirChat: (String) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ConversacionesViewModel = viewModel(factory = ConversacionesViewModel.Factory)
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.actualizar() }
    ConversacionesContenido(estado = estado, alAbrirChat = alAbrirChat, modifier = modifier)
}

/** Lo que dibuja [Conversaciones]. */
@Composable
internal fun ConversacionesContenido(
    estado: ConversacionesUiState,
    alAbrirChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    RetroCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.directos_titulo),
            style = MaterialTheme.typography.titleMedium,
            color = RetroGlossyDark,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.directos_explicacion),
            style = MaterialTheme.typography.bodySmall,
            color = RetroGrayText,
        )
        if (estado.conversaciones.isEmpty()) {
            Text(
                text = stringResource(R.string.directos_vacio),
                style = MaterialTheme.typography.bodyMedium,
                color = RetroGrayText,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        estado.conversaciones.forEach { conversacion ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            FilaConversacion(conversacion, estado.miId, alAbrir = { alAbrirChat(conversacion.persona.id) })
        }
    }
}

/** Una conversación: avatar y nombre de la otra persona, el último mensaje y cuándo. */
@Composable
private fun FilaConversacion(conversacion: Conversacion, miId: String?, alAbrir: () -> Unit) {
    val persona = conversacion.persona
    val ultimo = conversacion.ultimo.mensaje
    val texto = if (ultimo.zumbido) stringResource(R.string.directos_zumbido_texto) else ultimo.texto
    val resumen = if (ultimo.remitenteId == miId) stringResource(R.string.directos_tu, texto) else texto
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.directos_abrir, persona.nombre)) { alAbrir() },
    ) {
        Avatar(nombre = persona.nombre, tamano = 36.dp, imagenUri = persona.avatarUri)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = persona.nombre,
                style = MaterialTheme.typography.titleSmall,
                color = RetroLinkBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = resumen,
                style = MaterialTheme.typography.bodyMedium,
                color = RetroTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = textoFecha(ultimo.fechaCreacion),
            style = MaterialTheme.typography.bodySmall,
            color = RetroGrayText,
        )
    }
}

/** Estado de la lista de conversaciones. Lee de Room: al llegar mensajes nuevos se actualiza sola. */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversacionesViewModel(
    private val directos: MensajesDirectosRepository,
    usuarios: UsuarioRepository,
) : ViewModel() {

    private val miId = usuarios.idCuenta()

    val estado: StateFlow<ConversacionesUiState> = combine(
        miId,
        miId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else directos.conversaciones(id) },
    ) { yo, lista -> ConversacionesUiState(miId = yo, conversaciones = lista) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversacionesUiState())

    /** Descarga los mensajes. Sin conexión se sigue viendo la última copia (ya avisa la franja amarilla). */
    fun actualizar() {
        viewModelScope.launch { directos.refrescar() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                ConversacionesViewModel(app.container.mensajesDirectosRepository, app.container.usuarioRepository)
            }
        }
    }
}
