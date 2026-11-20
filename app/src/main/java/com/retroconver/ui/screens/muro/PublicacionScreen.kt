package com.retroconver.ui.screens.muro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.R
import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.ui.components.Avatar
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.screens.galeria.CabeceraGaleria
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroLinkBlue
import com.retroconver.ui.theme.RetroTextDark

/**
 * Detalle de una publicación con sus comentarios (E4-07.1, #66). Deslizando hacia abajo se
 * vuelve a descargar. Abajo se escribe un comentario (E4-07.2, #67).
 */
@Composable
fun PublicacionScreen(
    alVolver: () -> Unit,
    viewModel: PublicacionViewModel = viewModel(factory = PublicacionViewModel.Factory),
    alAbrirPerfil: ((String) -> Unit)? = null,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    PublicacionContenido(
        estado = estado,
        alVolver = alVolver,
        alActualizar = viewModel::actualizar,
        alCambiarTexto = viewModel::cambiarTexto,
        alComentar = viewModel::comentar,
        alBorrarPublicacion = viewModel::borrarPublicacion,
        alBorrarComentario = viewModel::borrarComentario,
        alAbrirPerfil = alAbrirPerfil,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PublicacionContenido(
    estado: PublicacionUiState,
    alVolver: () -> Unit,
    alActualizar: () -> Unit,
    modifier: Modifier = Modifier,
    alCambiarTexto: (String) -> Unit = {},
    alComentar: () -> Unit = {},
    alBorrarPublicacion: () -> Unit = {},
    alBorrarComentario: (String) -> Unit = {},
    alAbrirPerfil: ((String) -> Unit)? = null,
) {
    var preguntarPublicacion by rememberSaveable { mutableStateOf(false) }
    var comentarioABorrar by rememberSaveable { mutableStateOf<String?>(null) }
    // Al borrar la publicación ya no hay nada que ver aquí: se vuelve al muro
    LaunchedEffect(estado.borrado.publicacionBorrada) {
        if (estado.borrado.publicacionBorrada) alVolver()
    }
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGaleria(titulo = stringResource(R.string.muro_publicacion_titulo), alVolver = alVolver)
        // Sin conexión ya avisa la franja amarilla; si hay copia, no hace falta repetirlo
        val error = estado.error?.takeUnless { it == R.string.red_sin_conexion && estado.publicacion != null }
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
        estado.borrado.error?.let { mensaje ->
            Text(
                text = stringResource(mensaje),
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
                val publicacion = estado.publicacion
                when {
                    publicacion != null -> {
                        item { PublicacionCard(publicacion, alAbrirAutor = alAbrirPerfil) }
                        // Solo se borra lo propio (RF-29); el servidor lo comprueba también
                        if (publicacion.autor.id == estado.borrado.miId) {
                            item {
                                RetroButton(
                                    texto = stringResource(R.string.muro_borrar_publicacion),
                                    onClick = { if (!estado.borrado.borrando) preguntarPublicacion = true },
                                )
                            }
                        }
                        item {
                            Text(
                                text = stringResource(R.string.muro_comentarios_titulo),
                                style = MaterialTheme.typography.titleMedium,
                                color = RetroGlossyDark,
                                modifier = Modifier.semantics { heading() },
                            )
                        }
                        if (estado.comentarios.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.muro_sin_comentarios),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RetroGrayText,
                                )
                            }
                        } else {
                            items(estado.comentarios, key = { it.comentario.id }) { comentario ->
                                ComentarioFila(
                                    item = comentario,
                                    puedeBorrar = comentario.autor.id == estado.borrado.miId,
                                    alBorrar = { if (!estado.borrado.borrando) comentarioABorrar = comentario.comentario.id },
                                )
                            }
                        }
                    }
                    // Descargado y sigue sin estar: se ha borrado
                    !estado.actualizando -> item {
                        Text(
                            text = stringResource(R.string.muro_publicacion_no_existe),
                            style = MaterialTheme.typography.bodyLarge,
                            color = RetroTextDark,
                        )
                    }
                    else -> Unit // primera descarga en curso
                }
            }
        }
        if (estado.publicacion != null) {
            CampoComentario(estado.formulario, alCambiarTexto, alComentar)
        }
    }
    if (preguntarPublicacion) {
        ConfirmarBorrado(
            titulo = stringResource(R.string.muro_borrar_publicacion_pregunta),
            explicacion = stringResource(R.string.muro_borrar_publicacion_explicacion),
            alConfirmar = {
                preguntarPublicacion = false
                alBorrarPublicacion()
            },
            alCancelar = { preguntarPublicacion = false },
        )
    }
    comentarioABorrar?.let { id ->
        ConfirmarBorrado(
            titulo = stringResource(R.string.muro_borrar_comentario_pregunta),
            explicacion = stringResource(R.string.muro_borrar_comentario_explicacion),
            alConfirmar = {
                comentarioABorrar = null
                alBorrarComentario(id)
            },
            alCancelar = { comentarioABorrar = null },
        )
    }
}

/** Pide confirmación antes de borrar algo propio, porque no se puede deshacer. */
@Composable
private fun ConfirmarBorrado(titulo: String, explicacion: String, alConfirmar: () -> Unit, alCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(titulo) },
        text = { Text(explicacion) },
        confirmButton = { TextButton(onClick = alConfirmar) { Text(stringResource(R.string.muro_borrar_confirmar)) } },
        dismissButton = { TextButton(onClick = alCancelar) { Text(stringResource(R.string.muro_borrar_cancelar)) } },
    )
}

/** Campo para escribir un comentario, con contador y el botón de enviar. */
@Composable
private fun CampoComentario(
    formulario: FormularioComentario,
    alCambiarTexto: (String) -> Unit,
    alComentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorEnvio = formulario.errorEnvio
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Si hay error, el mensaje sustituye al contador y el campo se marca en rojo
        OutlinedTextField(
            value = formulario.texto,
            onValueChange = alCambiarTexto,
            label = { Text(stringResource(R.string.comentar_etiqueta)) },
            enabled = !formulario.enviando,
            isError = formulario.error != null || errorEnvio != null,
            supportingText = {
                val hayError = formulario.error != null || errorEnvio != null
                Text(
                    text = when {
                        errorEnvio != null -> stringResource(errorEnvio)
                        formulario.error == ErrorPublicacion.VACIO -> stringResource(R.string.comentar_error_vacio)
                        formulario.error == ErrorPublicacion.DEMASIADO_LARGO ->
                            stringResource(R.string.publicar_error_demasiado_largo, LimitesComentario.TEXTO_MAX)
                        else -> stringResource(R.string.publicar_contador, formulario.texto.trim().length, LimitesComentario.TEXTO_MAX)
                    },
                    modifier = if (hayError) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier,
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        RetroButton(
            texto = stringResource(if (formulario.enviando) R.string.comentar_enviando else R.string.comentar_enviar),
            onClick = alComentar,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/** Un comentario: avatar, nombre, fecha y texto. */
@Composable
private fun ComentarioFila(
    item: ComentarioConAutor,
    puedeBorrar: Boolean,
    alBorrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Con el botón de borrar no se junta todo en un elemento, para que TalkBack lo enseñe aparte
    RetroCard(modifier = modifier.semantics(mergeDescendants = !puedeBorrar) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(nombre = item.autor.nombre, tamano = 28.dp, imagenUri = item.autor.avatarUri)
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.autor.nombre,
                style = MaterialTheme.typography.titleSmall,
                color = RetroLinkBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = textoFecha(item.comentario.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = RetroGrayText,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.comentario.texto,
            style = MaterialTheme.typography.bodyMedium,
            color = RetroTextDark,
        )
        if (puedeBorrar) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                RetroButton(texto = stringResource(R.string.muro_borrar_comentario), onClick = alBorrar)
            }
        }
    }
}
