package com.retroconver.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retroconver.R
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Pantalla vacía con el nombre de la sección. Sirve para probar la navegación
 * mientras se construye cada módulo; cada pantalla real la sustituye.
 */
@Composable
fun PantallaProvisional(
    titulo: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.pantalla_en_construccion),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PantallaProvisionalPreview() {
    RetroconverTheme {
        PantallaProvisional(titulo = "Muro")
    }
}
