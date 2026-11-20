package com.retroconver.ui.screens.mensajes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Sección Mensajes (E4-09, #22): el tablón de la cuenta de la sesión, con lo que le han escrito
 * otras personas. Para escribir en el tablón de alguien se entra en su perfil.
 */
@Composable
fun MensajesScreen(viewModel: MensajesViewModel = viewModel(factory = MensajesViewModel.Factory)) {
    val miId by viewModel.miId.collectAsStateWithLifecycle()
    MensajesContenido(miId = miId, tablon = { id -> Tablon(idPerfil = id) })
}

/** Explicación arriba y debajo el tablón de [miId] (nada hasta que se sabe qué cuenta es). */
@Composable
internal fun MensajesContenido(
    miId: String?,
    tablon: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RetroCard {
            Text(
                text = stringResource(R.string.mensajes_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = RetroGlossyDark,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.mensajes_explicacion),
                style = MaterialTheme.typography.bodyMedium,
                color = RetroGrayText,
            )
        }
        if (miId != null) tablon(miId)
    }
}

/** Solo necesita saber de qué cuenta es el tablón. */
class MensajesViewModel(usuarios: UsuarioRepository) : ViewModel() {

    val miId: StateFlow<String?> = usuarios.idCuenta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                MensajesViewModel(app.container.usuarioRepository)
            }
        }
    }
}
