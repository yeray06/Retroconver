package com.retroconver.ui.screens.galeria

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.retroconver.R
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.ui.components.rememberSelectorDeImagenes
import com.retroconver.ui.theme.RetroCardBackground
import com.retroconver.ui.theme.RetroCardBorder
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroProfileCardBg
import com.retroconver.ui.theme.RetroconverTheme
import java.time.Instant

/**
 * Fotos de un álbum en una cuadrícula de 3 columnas (E4-10.3, #70). Arriba, la cabecera con
 * «← Volver» y el título del álbum, como en el boceto de docs/diseño/galeria.md.
 * En los álbumes propios se pueden añadir fotos (E4-10.4, #71); al pulsar una, se abre a
 * pantalla completa, donde se puede borrar.
 */
@Composable
fun AlbumScreen(
    alVolver: () -> Unit,
    alAbrirFoto: (String) -> Unit,
    viewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val elegirFotos = rememberSelectorDeImagenes(maximo = LimitesGaleria.FOTOS_POR_SUBIDA) { uris ->
        viewModel.subirFotos(uris)
    }
    AlbumContenido(
        estado = estado,
        alVolver = alVolver,
        alAbrirFoto = alAbrirFoto,
        alAnadirFotos = {
            viewModel.ocultarFalloSubida()
            elegirFotos()
        },
        alActualizar = viewModel::actualizar,
    )
}

@Composable
internal fun AlbumContenido(
    estado: AlbumUiState,
    alVolver: () -> Unit,
    alAbrirFoto: (String) -> Unit,
    alAnadirFotos: () -> Unit,
    alActualizar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val album = estado.album
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGaleria(titulo = album?.titulo.orEmpty(), alVolver = alVolver)
        when {
            // Room todavía no ha respondido: no se enseña el «no existe»
            !estado.cargado -> Unit
            // Se ha borrado (desde otro móvil, por ejemplo) o ya no existe en el servidor
            album == null -> Text(
                text = stringResource(R.string.galeria_album_no_existe),
                style = MaterialTheme.typography.bodyLarge,
                color = RetroGrayText,
                modifier = Modifier.padding(16.dp),
            )
            else -> {
                Avisos(estado)
                CuadriculaFotos(
                    estado = estado,
                    alAbrirFoto = alAbrirFoto,
                    alAnadirFotos = alAnadirFotos,
                    alActualizar = alActualizar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuadriculaFotos(
    estado: AlbumUiState,
    alAbrirFoto: (String) -> Unit,
    alAnadirFotos: () -> Unit,
    alActualizar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = estado.actualizando,
        onRefresh = alActualizar,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (estado.esMio) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val progreso = estado.subida
                    Column {
                        Button(
                            onClick = alAnadirFotos,
                            enabled = progreso == null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                when {
                                    progreso == null -> stringResource(R.string.galeria_anadir_fotos)
                                    progreso.total == 0 -> stringResource(R.string.galeria_subida_en_cola)
                                    else -> stringResource(R.string.galeria_subiendo, progreso.actual, progreso.total)
                                },
                            )
                        }
                        if (progreso != null) {
                            // La subida va en segundo plano (#147): no hace falta esperar aquí
                            Text(
                                text = stringResource(R.string.galeria_subida_sigue),
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroGrayText,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
            if (estado.fotos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(
                            if (estado.esMio) R.string.galeria_album_vacio_mio else R.string.galeria_album_vacio_otro,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = RetroGrayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 32.dp),
                    )
                }
            }
            itemsIndexed(estado.fotos, key = { _, foto -> foto.id }) { indice, foto ->
                Miniatura(
                    foto = foto,
                    descripcion = stringResource(R.string.galeria_foto_n, indice + 1, estado.fotos.size),
                    alPulsar = { alAbrirFoto(foto.id) },
                )
            }
        }
    }
}

/** Errores al actualizar o al subir fotos. TalkBack los lee al aparecer. */
@Composable
private fun Avisos(estado: AlbumUiState) {
    val falloSubida = estado.falloSubida
    // Sin conexión ya avisa la franja amarilla
    val errorActualizar = estado.error?.takeUnless { it == R.string.red_sin_conexion }
    if (falloSubida == null && errorActualizar == null) return
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        if (falloSubida != null) {
            Text(
                text = stringResource(R.string.galeria_error_subida, falloSubida.fallidas, falloSubida.total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            falloSubida.motivo?.let {
                Text(text = stringResource(it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
        if (errorActualizar != null) {
            Text(text = stringResource(errorActualizar), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

/** Una foto de la cuadrícula: cuadrada, recortada al centro y con borde fino. */
@Composable
private fun Miniatura(foto: FotoEntity, descripcion: String, alPulsar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(RetroProfileCardBg)
            .border(1.dp, RetroCardBorder)
            .clickable(role = Role.Image, onClick = alPulsar)
            .semantics { contentDescription = descripcion },
    ) {
        AsyncImage(
            model = foto.uri,
            contentDescription = null, // ya la describe el Box
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Cabecera de navegación de la galería: «← Volver» y el título, sobre una franja blanca.
 * Se usa en el detalle del álbum y en la foto a pantalla completa.
 */
@Composable
internal fun CabeceraGaleria(
    titulo: String,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null,
) {
    val descripcionVolver = stringResource(R.string.galeria_volver_descripcion)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RetroCardBackground)
            .border(width = 1.dp, color = RetroCardBorder)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = alVolver,
            modifier = Modifier.semantics { contentDescription = descripcionVolver },
        ) {
            Text(stringResource(R.string.galeria_volver))
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = RetroGlossyDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        accion?.invoke()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun AlbumVacioPreview() {
    RetroconverTheme {
        AlbumContenido(
            estado = AlbumUiState(
                cargado = true,
                album = AlbumEntity("1", "yo", "Viaje a Londres", fechaCreacion = Instant.now()),
                esMio = true,
                falloSubida = FalloSubida(1, 3, R.string.red_sin_conexion),
            ),
            alVolver = {},
            alAbrirFoto = {},
            alAnadirFotos = {},
            alActualizar = {},
        )
    }
}
