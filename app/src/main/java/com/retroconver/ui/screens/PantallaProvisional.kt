package com.retroconver.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retroconver.R
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Pantalla provisional con el nombre de la sección dentro de una tarjeta retro.
 * Sirve para probar la navegación y el tema mientras se construye cada módulo;
 * cada pantalla real la sustituye.
 */
@Composable
fun PantallaProvisional(
    titulo: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        RetroCard {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.pantalla_en_construccion),
                style = MaterialTheme.typography.bodyMedium,
                color = RetroGrayText,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantallaProvisionalPreview() {
    RetroconverTheme {
        PantallaProvisional(titulo = "Muro")
    }
}
