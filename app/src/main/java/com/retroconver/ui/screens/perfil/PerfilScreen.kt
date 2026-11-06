package com.retroconver.ui.screens.perfil

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.R
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.ui.components.Avatar
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.components.rememberSelectorDeImagen
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroLinkBlue
import com.retroconver.ui.theme.RetroProfileCardBg
import com.retroconver.ui.theme.RetroProfileCardBorder
import com.retroconver.ui.theme.RetroTextDark
import com.retroconver.ui.theme.RetroconverTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Perfil de la persona usuaria (E3-06, #14): avatar, nombre, estado y biografía guardados en Room.
 * Desde aquí se cambia la foto del avatar y se abre la edición del resto de datos.
 */
@Composable
fun PerfilScreen(
    alEditar: () -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val elegirFoto = rememberSelectorDeImagen { uri -> viewModel.cambiarAvatar(uri) }
    PerfilContenido(
        estado = estado,
        alCambiarFoto = {
            viewModel.ocultarErrorFoto()
            elegirFoto()
        },
        alEditar = alEditar,
    )
}

/** Dibuja el perfil a partir del estado: nada mientras carga, y la ficha cuando llega el usuario. */
@Composable
internal fun PerfilContenido(
    estado: PerfilUiState,
    alCambiarFoto: () -> Unit,
    alEditar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val usuario = estado.usuario ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CabeceraPerfil(usuario, estado, alCambiarFoto, alEditar)
        TarjetaEstado(usuario.estado)
        RetroCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.perfil_sobre_mi),
                style = MaterialTheme.typography.titleMedium,
                color = RetroGlossyDark,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            if (usuario.biografia.isBlank()) {
                TextoVacio(stringResource(R.string.perfil_sin_biografia))
            } else {
                Text(text = usuario.biografia, style = MaterialTheme.typography.bodyLarge, color = RetroTextDark)
            }
        }
    }
}

/** Avatar grande con el nombre, la fecha de alta y los botones del perfil. */
@Composable
private fun CabeceraPerfil(
    usuario: UsuarioEntity,
    estado: PerfilUiState,
    alCambiarFoto: () -> Unit,
    alEditar: () -> Unit,
) {
    RetroCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(nombre = usuario.nombre, imagenUri = usuario.avatarUri, tamano = 96.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = RetroLinkBlue,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.perfil_miembro_desde, fechaLarga(usuario.fechaAlta)),
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroGrayText,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RetroButton(
                texto = stringResource(if (estado.guardandoFoto) R.string.perfil_guardando_foto else R.string.perfil_cambiar_foto),
                onClick = alCambiarFoto,
            )
            RetroButton(texto = stringResource(R.string.perfil_editar), onClick = alEditar)
        }
        if (estado.errorFoto) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.perfil_error_foto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                // TalkBack lo lee en cuanto aparece
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

/** Frase del estado en la tarjeta azul clara del prototipo, como el mensaje personal de MSN. */
@Composable
private fun TarjetaEstado(texto: String) {
    Surface(
        color = RetroProfileCardBg,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RetroProfileCardBorder, MaterialTheme.shapes.medium),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.perfil_estado),
                style = MaterialTheme.typography.labelMedium,
                color = RetroGlossyDark,
            )
            Spacer(Modifier.height(4.dp))
            if (texto.isBlank()) {
                TextoVacio(stringResource(R.string.perfil_sin_estado))
            } else {
                Text(text = "«$texto»", style = MaterialTheme.typography.bodyLarge, color = RetroTextDark)
            }
        }
    }
}

@Composable
private fun TextoVacio(texto: String) {
    Text(text = texto, style = MaterialTheme.typography.bodyMedium, color = RetroGrayText, fontStyle = FontStyle.Italic)
}

/** Fecha en formato largo del idioma del móvil, por ejemplo «5 de noviembre de 2026». */
@Composable
private fun fechaLarga(fecha: Instant): String = remember(fecha) {
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withZone(ZoneId.systemDefault())
        .format(fecha)
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun PerfilPreview() {
    RetroconverTheme {
        PerfilContenido(
            estado = PerfilUiState(
                usuario = UsuarioEntity(
                    id = 1,
                    nombre = "Yeray",
                    biografia = "Echo de menos los tablones de Tuenti y las tardes en el Messenger.",
                    estado = "Escuchando música en el MSN",
                    fechaAlta = Instant.parse("2026-11-05T10:00:00Z"),
                ),
            ),
            alCambiarFoto = {},
            alEditar = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun PerfilRecienCreadoPreview() {
    RetroconverTheme {
        PerfilContenido(
            estado = PerfilUiState(usuario = UsuarioEntity(id = 1, nombre = "Yo", fechaAlta = Instant.now()), errorFoto = true),
            alCambiarFoto = {},
            alEditar = {},
        )
    }
}
