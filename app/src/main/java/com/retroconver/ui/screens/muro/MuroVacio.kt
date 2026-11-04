package com.retroconver.ui.screens.muro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retroconver.R
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroTextDark
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Estado vacío del muro (RF-10): invita a «romper el hielo» con el primer momento.
 * La ilustración es un emoji, como los iconos de los bocetos (💬, 🖼️).
 */
@Composable
fun MuroVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Decorativo: TalkBack solo lee el texto
        Text(text = "🧊", fontSize = 64.sp, modifier = Modifier.clearAndSetSemantics { })
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.muro_vacio_titulo),
            style = MaterialTheme.typography.headlineMedium,
            color = RetroTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.muro_vacio_mensaje),
            style = MaterialTheme.typography.bodyLarge,
            color = RetroGrayText,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun MuroVacioPreview() {
    RetroconverTheme {
        MuroVacio()
    }
}
