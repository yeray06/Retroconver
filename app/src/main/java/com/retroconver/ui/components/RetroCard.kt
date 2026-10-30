package com.retroconver.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retroconver.ui.theme.RetroCardBackground
import com.retroconver.ui.theme.RetroCardBorder
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Tarjeta blanca con borde azulado y una sombra muy ligera.
 * Base de las publicaciones, comentarios, álbumes y mensajes.
 */
@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = RetroCardBackground,
        border = BorderStroke(1.dp, RetroCardBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RetroCardPreview() {
    RetroconverTheme {
        RetroCard(modifier = Modifier.padding(8.dp)) {
            Text("Cris Reed", style = MaterialTheme.typography.titleMedium)
            Text("¡Adiós Nueva York! Menudo viaje.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
