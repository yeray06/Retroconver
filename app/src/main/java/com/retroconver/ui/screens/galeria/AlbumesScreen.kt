package com.retroconver.ui.screens.galeria

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.retroconver.R
import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.local.AlbumEntity
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.screens.perfil.ErrorCampo
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
 * Lista de álbumes de la galería (E4-10.2, #69), con su portada y su número de fotos.
 * En los álbumes propios se pueden crear álbumes nuevos; al crearlo, se abre para añadir fotos.
 */
@Composable
fun AlbumesScreen(
    alAbrirAlbum: (String) -> Unit,
    viewModel: AlbumesViewModel = viewModel(factory = AlbumesViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel.albumCreado) {
        viewModel.albumCreado?.let { id ->
            viewModel.albumAbierto()
            alAbrirAlbum(id)
        }
    }
    AlbumesContenido(
        estado = estado,
        alAbrirAlbum = alAbrirAlbum,
        alCrearAlbum = viewModel::abrirDialogo,
        alActualizar = viewModel::actualizar,
    )
    if (viewModel.dialogoAbierto) {
        DialogoNuevoAlbum(
            titulo = viewModel.titulo,
            error = viewModel.errorTitulo,
            errorServidor = viewModel.errorCrear,
            creando = viewModel.creando,
            alCambiar = viewModel::cambiarTitulo,
            alCrear = viewModel::crearAlbum,
            alCancelar = viewModel::cerrarDialogo,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumesContenido(
    estado: AlbumesUiState,
    alAbrirAlbum: (String) -> Unit,
    alCrearAlbum: () -> Unit,
    alActualizar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val albumes = estado.albumes
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (estado.esMia) {
                    stringResource(R.string.galeria_mis_albumes)
                } else {
                    stringResource(R.string.galeria_albumes_de, estado.nombreDueno ?: stringResource(R.string.galeria_alguien))
                },
                style = MaterialTheme.typography.titleLarge,
                color = RetroGlossyDark,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            if (estado.esMia) {
                Button(onClick = alCrearAlbum) { Text(stringResource(R.string.galeria_nuevo_album)) }
            }
        }
        // Sin conexión ya avisa la franja amarilla; si hay copia, no hace falta repetirlo
        val error = estado.error?.takeUnless { it == R.string.red_sin_conexion && !albumes.isNullOrEmpty() }
        if (error != null) {
            Text(
                text = stringResource(error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        PullToRefreshBox(
            isRefreshing = estado.actualizando,
            onRefresh = alActualizar,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    albumes == null -> Unit // Room todavía no ha respondido: no se enseña el «vacío»
                    albumes.isEmpty() -> item { GaleriaVacia(estado.esMia, estado.nombreDueno) }
                    else -> items(albumes, key = { it.album.id }) { album ->
                        TarjetaAlbum(album, alAbrir = { alAbrirAlbum(album.album.id) })
                    }
                }
            }
        }
    }
}

/** Un álbum de la lista: portada, nombre, número de fotos y fecha. Toda la tarjeta lo abre. */
@Composable
private fun TarjetaAlbum(item: AlbumConPortada, alAbrir: () -> Unit) {
    val etiquetaAbrir = stringResource(R.string.galeria_abrir_album)
    RetroCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClickLabel = etiquetaAbrir, onClick = alAbrir),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Portada(item.portadaUri)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.album.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroLinkBlue,
                )
                Text(
                    text = pluralStringResource(R.plurals.galeria_num_fotos, item.numFotos, item.numFotos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetroTextDark,
                )
                Text(
                    text = stringResource(R.string.galeria_creado_el, fechaLarga(item.album.fechaCreacion)),
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroGrayText,
                )
            }
        }
    }
}

/** Miniatura de la portada con borde fino, como en Tuenti. Decorativa: la tarjeta ya dice qué álbum es. */
@Composable
private fun Portada(uri: String?) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(RetroProfileCardBg)
            .border(1.dp, RetroProfileCardBorder, MaterialTheme.shapes.extraSmall)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        // Se ve mientras carga la portada, o si el álbum está vacío
        Text(text = "🖼️", fontSize = 28.sp)
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** No hay álbumes: invita a crear el primero, o dice que esa persona aún no tiene. */
@Composable
private fun GaleriaVacia(esMia: Boolean, nombreDueno: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "📷", fontSize = 64.sp, modifier = Modifier.clearAndSetSemantics { })
        Spacer(Modifier.height(16.dp))
        if (esMia) {
            Text(
                text = stringResource(R.string.galeria_sin_albumes_titulo),
                style = MaterialTheme.typography.headlineSmall,
                color = RetroTextDark,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.galeria_sin_albumes_mensaje),
                style = MaterialTheme.typography.bodyLarge,
                color = RetroGrayText,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.galeria_sin_albumes_otro, nombreDueno ?: stringResource(R.string.galeria_alguien)),
                style = MaterialTheme.typography.bodyLarge,
                color = RetroGrayText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Pide el nombre del álbum nuevo, que es obligatorio (E4-10.2, #69). */
@Composable
private fun DialogoNuevoAlbum(
    titulo: String,
    error: ErrorCampo?,
    errorServidor: Int?,
    creando: Boolean,
    alCambiar: (String) -> Unit,
    alCrear: () -> Unit,
    alCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(stringResource(R.string.galeria_dialogo_titulo)) },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = alCambiar,
                    label = { Text(stringResource(R.string.galeria_nombre_album)) },
                    singleLine = true,
                    enabled = !creando,
                    isError = error != null,
                    supportingText = {
                        Text(
                            when (error) {
                                ErrorCampo.OBLIGATORIO -> stringResource(R.string.galeria_error_titulo_obligatorio)
                                ErrorCampo.DEMASIADO_LARGO -> stringResource(R.string.galeria_error_titulo_largo, LimitesGaleria.TITULO_MAX)
                                null -> stringResource(R.string.editar_contador, titulo.trim().length, LimitesGaleria.TITULO_MAX)
                            },
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { alCrear() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorServidor != null) {
                    Text(
                        text = stringResource(errorServidor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCrear, enabled = !creando) {
                Text(stringResource(if (creando) R.string.galeria_creando else R.string.galeria_crear))
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar, enabled = !creando) { Text(stringResource(R.string.galeria_cancelar)) }
        },
    )
}

/** Fecha en formato largo del idioma del móvil, por ejemplo «11 de noviembre de 2026». */
@Composable
internal fun fechaLarga(fecha: Instant): String = remember(fecha) {
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withZone(ZoneId.systemDefault())
        .format(fecha)
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun AlbumesPreview() {
    val ahora = Instant.now()
    RetroconverTheme {
        AlbumesContenido(
            estado = AlbumesUiState(
                esMia = true,
                albumes = listOf(
                    AlbumConPortada(AlbumEntity("1", "yo", "Fiesta Nochevieja 2008", fechaCreacion = ahora), 12, null),
                    AlbumConPortada(AlbumEntity("2", "yo", "Viaje a Londres", fechaCreacion = ahora), 1, null),
                ),
            ),
            alAbrirAlbum = {},
            alCrearAlbum = {},
            alActualizar = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun AlbumesVaciosPreview() {
    RetroconverTheme {
        AlbumesContenido(
            estado = AlbumesUiState(esMia = true, albumes = emptyList()),
            alAbrirAlbum = {},
            alCrearAlbum = {},
            alActualizar = {},
        )
    }
}
