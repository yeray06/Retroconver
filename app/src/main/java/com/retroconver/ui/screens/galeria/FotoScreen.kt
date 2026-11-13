package com.retroconver.ui.screens.galeria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.retroconver.R

/**
 * Una foto a pantalla completa, sobre fondo negro y sin recortar (E4-10.3, #70).
 * Si es propia, la cabecera tiene «Borrar», que pide confirmación (E4-10.4, #71).
 */
@Composable
fun FotoScreen(
    alVolver: () -> Unit,
    viewModel: FotoViewModel = viewModel(factory = FotoViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var preguntarBorrar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(estado.borrada) {
        if (estado.borrada) alVolver()
    }
    FotoContenido(
        estado = estado,
        alVolver = alVolver,
        alBorrar = { preguntarBorrar = true },
    )
    if (preguntarBorrar) {
        AlertDialog(
            onDismissRequest = { preguntarBorrar = false },
            title = { Text(stringResource(R.string.galeria_borrar_pregunta)) },
            text = { Text(stringResource(R.string.galeria_borrar_explicacion)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        preguntarBorrar = false
                        viewModel.borrar()
                    },
                ) { Text(stringResource(R.string.galeria_borrar_foto)) }
            },
            dismissButton = {
                TextButton(onClick = { preguntarBorrar = false }) { Text(stringResource(R.string.galeria_cancelar)) }
            },
        )
    }
}

@Composable
internal fun FotoContenido(
    estado: FotoUiState,
    alVolver: () -> Unit,
    alBorrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGaleria(
            titulo = estado.tituloAlbum,
            alVolver = alVolver,
            accion = if (estado.esMia) {
                {
                    TextButton(onClick = alBorrar, enabled = !estado.borrando) {
                        Text(
                            text = stringResource(if (estado.borrando) R.string.galeria_borrando else R.string.galeria_borrar_foto),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            } else {
                null
            },
        )
        estado.error?.let { error ->
            Text(
                text = stringResource(error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            val foto = estado.foto
            if (foto != null) {
                AsyncImage(
                    model = foto.uri,
                    contentDescription = stringResource(R.string.galeria_foto_del_album, estado.tituloAlbum),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
