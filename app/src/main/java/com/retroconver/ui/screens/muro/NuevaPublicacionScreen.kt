package com.retroconver.ui.screens.muro

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.retroconver.R
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.components.rememberSelectorDeImagen
import com.retroconver.ui.theme.RetroCardBorder
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Formulario de nueva publicación (E3-07, #60 y #61): texto obligatorio con contador y foto
 * opcional que se ve antes de publicar y se puede quitar. Al publicar, o al cancelar,
 * [alTerminar] vuelve al muro, que ya muestra la publicación arriba porque la lee de Room.
 */
@Composable
fun NuevaPublicacionScreen(
    alTerminar: () -> Unit,
    viewModel: NuevaPublicacionViewModel = viewModel(factory = NuevaPublicacionViewModel.Factory),
) {
    LaunchedEffect(viewModel.publicado) {
        if (viewModel.publicado) alTerminar()
    }
    val elegirFoto = rememberSelectorDeImagen { uri -> viewModel.elegirImagen(uri) }
    NuevaPublicacionContenido(
        texto = viewModel.texto,
        error = viewModel.error,
        imagenUri = viewModel.imagenUri,
        guardandoImagen = viewModel.guardandoImagen,
        errorImagen = viewModel.errorImagen,
        errorPublicar = viewModel.errorPublicar,
        publicando = viewModel.publicando,
        alCambiarTexto = viewModel::cambiarTexto,
        alElegirImagen = elegirFoto,
        alQuitarImagen = viewModel::quitarImagen,
        alPublicar = viewModel::publicar,
        alCancelar = alTerminar,
    )
}

@Composable
internal fun NuevaPublicacionContenido(
    texto: String,
    error: ErrorPublicacion?,
    imagenUri: String?,
    guardandoImagen: Boolean,
    errorImagen: Boolean,
    errorPublicar: Boolean,
    publicando: Boolean,
    alCambiarTexto: (String) -> Unit,
    alElegirImagen: () -> Unit,
    alQuitarImagen: () -> Unit,
    alPublicar: () -> Unit,
    alCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        RetroCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.publicar_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = RetroGlossyDark,
                modifier = Modifier.semantics { heading() },
            )
            // Texto con contador. Si hay error, el mensaje sustituye al contador y el campo se marca en rojo
            OutlinedTextField(
                value = texto,
                onValueChange = alCambiarTexto,
                label = { Text(stringResource(R.string.publicar_etiqueta)) },
                enabled = !publicando,
                isError = error != null,
                minLines = 4,
                supportingText = {
                    Text(
                        text = when (error) {
                            ErrorPublicacion.VACIO -> stringResource(R.string.publicar_error_vacio)
                            ErrorPublicacion.DEMASIADO_LARGO ->
                                stringResource(R.string.publicar_error_demasiado_largo, LimitesPublicacion.TEXTO_MAX)
                            null -> stringResource(
                                R.string.publicar_contador,
                                texto.trim().length,
                                LimitesPublicacion.TEXTO_MAX,
                            )
                        },
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            Spacer(Modifier.height(12.dp))
            when {
                guardandoImagen -> Text(
                    text = stringResource(R.string.publicar_guardando_foto),
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroGrayText,
                )
                imagenUri == null -> RetroButton(
                    texto = stringResource(R.string.publicar_anadir_foto),
                    onClick = alElegirImagen,
                )
                else -> {
                    // Vista previa con las mismas medidas que en la tarjeta del muro
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = stringResource(R.string.publicar_vista_previa),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .border(1.dp, RetroCardBorder, MaterialTheme.shapes.extraSmall),
                    )
                    Spacer(Modifier.height(8.dp))
                    RetroButton(texto = stringResource(R.string.publicar_quitar_foto), onClick = alQuitarImagen)
                }
            }
            if (errorImagen) {
                Text(
                    text = stringResource(R.string.publicar_error_foto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (errorPublicar) {
                Text(
                    text = stringResource(R.string.publicar_error_guardar),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RetroButton(texto = stringResource(R.string.publicar_cancelar), onClick = alCancelar)
                Button(onClick = alPublicar, enabled = !publicando && !guardandoImagen) {
                    Text(stringResource(R.string.publicar_publicar))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun NuevaPublicacionVaciaPreview() {
    RetroconverTheme {
        NuevaPublicacionContenido(
            texto = "",
            error = ErrorPublicacion.VACIO,
            imagenUri = null,
            guardandoImagen = false,
            errorImagen = false,
            errorPublicar = false,
            publicando = false,
            alCambiarTexto = {},
            alElegirImagen = {},
            alQuitarImagen = {},
            alPublicar = {},
            alCancelar = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun NuevaPublicacionConFotoPreview() {
    RetroconverTheme {
        NuevaPublicacionContenido(
            texto = "¡Adiós Nueva York! Menudo viaje.",
            error = null,
            imagenUri = "file:///no-existe.jpg", // en la vista previa no se carga, solo se ve el hueco
            guardandoImagen = false,
            errorImagen = false,
            errorPublicar = false,
            publicando = false,
            alCambiarTexto = {},
            alElegirImagen = {},
            alQuitarImagen = {},
            alPublicar = {},
            alCancelar = {},
        )
    }
}
