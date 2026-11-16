package com.retroconver.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
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
import com.retroconver.ui.theme.EstiloDePerfil
import com.retroconver.ui.theme.LocalColoresPerfil
import com.retroconver.ui.theme.RetroCardBorder
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroTextDark
import com.retroconver.ui.theme.RetroconverTheme
import com.retroconver.ui.theme.TemaPerfil
import com.retroconver.ui.theme.TipografiaPerfil

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
        tema = viewModel.tema,
        tipografia = viewModel.tipografia,
        alCambiarTema = viewModel::cambiarTema,
        alCambiarTipografia = viewModel::cambiarTipografia,
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
    tema: TemaPerfil = TemaPerfil.CLASICO,
    tipografia: TipografiaPerfil = TipografiaPerfil.NORMAL,
    alCambiarTema: (TemaPerfil) -> Unit = {},
    alCambiarTipografia: (TipografiaPerfil) -> Unit = {},
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
            AspectoDelPerfil(
                nombre = nombre,
                estado = estado,
                tema = tema,
                tipografia = tipografia,
                habilitado = habilitado,
                alCambiarTema = alCambiarTema,
                alCambiarTipografia = alCambiarTipografia,
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
 * Tema y tipografía del perfil (EX-03, #30), con una vista previa de cómo lo verán los demás.
 * Cada grupo es una lista de opciones de las que se elige una: TalkBack dice cuál está marcada.
 */
@Composable
private fun AspectoDelPerfil(
    nombre: String,
    estado: String,
    tema: TemaPerfil,
    tipografia: TipografiaPerfil,
    habilitado: Boolean,
    alCambiarTema: (TemaPerfil) -> Unit,
    alCambiarTipografia: (TipografiaPerfil) -> Unit,
) {
    HorizontalDivider(modifier = Modifier.padding(top = 20.dp, bottom = 12.dp), color = RetroCardBorder)
    Text(
        text = stringResource(R.string.estilo_titulo),
        style = MaterialTheme.typography.titleMedium,
        color = RetroGlossyDark,
        modifier = Modifier.semantics { heading() },
    )
    Text(
        text = stringResource(R.string.estilo_explicacion),
        style = MaterialTheme.typography.bodyMedium,
        color = RetroGrayText,
    )
    TituloGrupo(stringResource(R.string.estilo_tema))
    Column(modifier = Modifier.selectableGroup()) {
        TemaPerfil.entries.forEach { opcion ->
            Opcion(
                seleccionada = opcion == tema,
                habilitada = habilitado,
                alElegir = { alCambiarTema(opcion) },
            ) {
                MuestraDeTema(opcion)
                Text(
                    text = stringResource(opcion.nombre),
                    style = MaterialTheme.typography.bodyLarge,
                    color = RetroTextDark,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
    TituloGrupo(stringResource(R.string.estilo_tipografia))
    Column(modifier = Modifier.selectableGroup()) {
        TipografiaPerfil.entries.forEach { opcion ->
            Opcion(
                seleccionada = opcion == tipografia,
                habilitada = habilitado,
                alElegir = { alCambiarTipografia(opcion) },
            ) {
                Text(
                    text = stringResource(opcion.nombre),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = opcion.familia),
                    color = RetroTextDark,
                )
            }
        }
    }
    TituloGrupo(stringResource(R.string.estilo_vista_previa))
    VistaPrevia(nombre = nombre, estado = estado, tema = tema, tipografia = tipografia)
}

@Composable
private fun TituloGrupo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = RetroGlossyDark,
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

/** Fila de una opción: toda la fila se pulsa y mide al menos 48 dp de alto. */
@Composable
private fun Opcion(
    seleccionada: Boolean,
    habilitada: Boolean,
    alElegir: () -> Unit,
    contenido: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = seleccionada, enabled = habilitada, role = Role.RadioButton, onClick = alElegir),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // El clic lo recoge la fila entera
        RadioButton(selected = seleccionada, onClick = null, enabled = habilitada)
        Spacer(Modifier.width(8.dp))
        contenido()
    }
}

/** Cuadradito con los colores del tema: el fondo, una tarjeta y el color del nombre. */
@Composable
private fun MuestraDeTema(tema: TemaPerfil) {
    val colores = tema.colores
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(MaterialTheme.shapes.small)
            .background(colores.fondo)
            .border(1.dp, colores.bordeTarjeta, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(colores.tarjeta)
                .border(2.dp, colores.nombre),
        )
    }
}

/** Una tarjeta pequeña con el nombre y el estado tal como se verán en el perfil. */
@Composable
private fun VistaPrevia(nombre: String, estado: String, tema: TemaPerfil, tipografia: TipografiaPerfil) {
    EstiloDePerfil(tema, tipografia) {
        val colores = LocalColoresPerfil.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colores.fondo)
                .padding(12.dp),
        ) {
            RetroCard(color = colores.tarjeta, borde = colores.bordeTarjeta) {
                Text(
                    text = nombre.trim().ifEmpty { stringResource(R.string.editar_nombre) },
                    style = MaterialTheme.typography.titleLarge,
                    color = colores.nombre,
                )
                Text(
                    text = estado.trim().takeIf { it.isNotEmpty() }?.let { "«$it»" }
                        ?: stringResource(R.string.estilo_vista_previa_estado),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colores.texto,
                )
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
