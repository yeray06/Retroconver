package com.retroconver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retroconver.ui.theme.RetroActionBorder
import com.retroconver.ui.theme.RetroActionBottom
import com.retroconver.ui.theme.RetroActionTop
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGlossyDeepDark
import com.retroconver.ui.theme.RetroGlossyMid
import com.retroconver.ui.theme.RetroGlossyTop
import com.retroconver.ui.theme.RetroconverTheme

private val DegradadoGlossy = Brush.verticalGradient(
    listOf(RetroGlossyTop, RetroGlossyMid, RetroGlossyDark, RetroGlossyDeepDark),
)

/**
 * Cabecera azul "glossy" con el logo en minúsculas, como en Tuenti.
 * Se extiende por debajo de la barra de estado del móvil.
 *
 * @param titulo Texto de la cabecera (el logo).
 * @param accion Botón opcional a la derecha, normalmente un [RetroHeaderBoton].
 */
@Composable
fun RetroHeader(
    titulo: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DegradadoGlossy)
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        accion?.invoke()
    }
}

/**
 * Botón pequeño en relieve para la cabecera, por ejemplo el "+" de nueva publicación.
 *
 * @param texto Lo que se ve en el botón ("+").
 * @param descripcion Lo que lee TalkBack ("Nueva publicación").
 */
@Composable
fun RetroHeaderBoton(
    texto: String,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.verticalGradient(listOf(RetroActionTop, RetroActionBottom)))
            .border(1.dp, RetroActionBorder, MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Preview
@Composable
private fun RetroHeaderPreview() {
    RetroconverTheme {
        RetroHeader(
            titulo = "retroconver",
            accion = { RetroHeaderBoton(texto = "+", descripcion = "Nueva publicación", onClick = {}) },
        )
    }
}
