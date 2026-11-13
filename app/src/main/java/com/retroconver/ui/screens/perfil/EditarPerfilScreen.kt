package com.retroconver.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.R
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Edición del perfil (E3-06.2, #54). Al guardar vuelve al perfil, que ya muestra los
 * datos nuevos porque los lee de Room. [alTerminar] vuelve atrás (guardando o cancelando).
 */
@Composable
fun EditarPerfilScreen(
    alTerminar: () -> Unit,
    viewModel: EditarPerfilViewModel = viewModel(factory = EditarPerfilViewModel.Factory),
) {
    LaunchedEffect(viewModel.guardado) {
        if (viewModel.guardado) alTerminar()
    }
    EditarPerfilContenido(
        nombre = viewModel.nombre,
        biografia = viewModel.biografia,
        estado = viewModel.estado,
        errores = viewModel.errores,
        errorGuardar = viewModel.errorGuardar,
        habilitado = viewModel.cargado && !viewModel.guardando,
        alCambiarNombre = viewModel::cambiarNombre,
        alCambiarBiografia = viewModel::cambiarBiografia,
        alCambiarEstado = viewModel::cambiarEstado,
        alGuardar = viewModel::guardar,
        alCancelar = alTerminar,
    )
}

@Composable
internal fun EditarPerfilContenido(
    nombre: String,
    biografia: String,
    estado: String,
    errores: ErroresPerfil,
    habilitado: Boolean,
    alCambiarNombre: (String) -> Unit,
    alCambiarBiografia: (String) -> Unit,
    alCambiarEstado: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    errorGuardar: Int? = null,
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
                text = stringResource(R.string.editar_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = RetroGlossyDark,
                modifier = Modifier.semantics { heading() },
            )
            Campo(
                etiqueta = stringResource(R.string.editar_nombre),
                valor = nombre,
                error = errores.nombre,
                limite = LimitesPerfil.NOMBRE_MAX,
                habilitado = habilitado,
                alCambiar = alCambiarNombre,
                capitalizacion = KeyboardCapitalization.Words,
            )
            Campo(
                etiqueta = stringResource(R.string.editar_estado),
                valor = estado,
                error = errores.estado,
                limite = LimitesPerfil.ESTADO_MAX,
                habilitado = habilitado,
                alCambiar = alCambiarEstado,
            )
            Campo(
                etiqueta = stringResource(R.string.editar_biografia),
                valor = biografia,
                error = errores.biografia,
                limite = LimitesPerfil.BIOGRAFIA_MAX,
                habilitado = habilitado,
                alCambiar = alCambiarBiografia,
                unaLinea = false,
            )
            if (errorGuardar != null) {
                Text(
                    text = stringResource(errorGuardar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    // TalkBack lo lee en cuanto aparece
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RetroButton(texto = stringResource(R.string.editar_cancelar), onClick = alCancelar)
                Button(onClick = alGuardar, enabled = habilitado) {
                    Text(stringResource(R.string.editar_guardar))
                }
            }
        }
    }
}

/**
 * Campo de texto con su contador de caracteres. Si hay error, el mensaje sustituye
 * al contador y el campo se marca en rojo; TalkBack lee el error al llegar al campo.
 */
@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    error: ErrorCampo?,
    limite: Int,
    habilitado: Boolean,
    alCambiar: (String) -> Unit,
    unaLinea: Boolean = true,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        isError = error != null,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        supportingText = {
            Text(
                text = when (error) {
                    ErrorCampo.OBLIGATORIO -> stringResource(R.string.error_nombre_obligatorio)
                    ErrorCampo.DEMASIADO_LARGO -> stringResource(R.string.error_demasiado_largo, limite)
                    null -> stringResource(R.string.editar_contador, valor.trim().length, limite)
                },
            )
        },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalizacion,
            imeAction = if (unaLinea) ImeAction.Next else ImeAction.Default,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun EditarPerfilPreview() {
    RetroconverTheme {
        EditarPerfilContenido(
            nombre = "",
            biografia = "Echo de menos los tablones de Tuenti.",
            estado = "Escuchando música en el MSN",
            errores = ErroresPerfil(nombre = ErrorCampo.OBLIGATORIO),
            habilitado = true,
            alCambiarNombre = {},
            alCambiarBiografia = {},
            alCambiarEstado = {},
            alGuardar = {},
            alCancelar = {},
        )
    }
}
