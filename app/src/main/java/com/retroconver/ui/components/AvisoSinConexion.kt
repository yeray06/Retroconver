package com.retroconver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retroconver.R
import com.retroconver.ui.theme.RetroAvisoBorde
import com.retroconver.ui.theme.RetroAvisoFondo
import com.retroconver.ui.theme.RetroTextDark
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Franja amarilla bajo la cabecera mientras no hay red (RNF-06), como los avisos de Tuenti.
 * TalkBack la lee en cuanto aparece.
 */
@Composable
fun AvisoSinConexion(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.red_aviso_sin_conexion),
            style = MaterialTheme.typography.bodySmall,
            color = RetroTextDark,
            modifier = Modifier
                .fillMaxWidth()
                .background(RetroAvisoFondo)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(RetroAvisoBorde),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvisoSinConexionPreview() {
    RetroconverTheme { AvisoSinConexion() }
}
