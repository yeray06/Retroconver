package com.retroconver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retroconver.ui.theme.RetroButtonBackground
import com.retroconver.ui.theme.RetroButtonBorder
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Botón plano de estilo Web 2.0, como los de comentarios de los bocetos.
 * La zona táctil mide al menos 48 dp aunque el botón se vea más pequeño (RNF-03).
 */
@Composable
fun RetroButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.small)
            .background(RetroButtonBackground)
            .border(1.dp, RetroButtonBorder, MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            color = RetroGrayText,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RetroButtonPreview() {
    RetroconverTheme {
        RetroButton(texto = "💬 3", onClick = {}, modifier = Modifier.padding(8.dp))
    }
}
