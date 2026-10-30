package com.retroconver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.retroconver.R
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroProfileCardBg
import com.retroconver.ui.theme.RetroProfileCardBorder
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Avatar cuadrado con borde fino, como en MSN y Tuenti.
 *
 * De momento muestra la inicial del nombre. Cuando haya fotos de perfil (E3-06.3, #55)
 * se le añade un parámetro con la imagen.
 */
@Composable
fun Avatar(
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp,
) {
    val inicial = nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val descripcion = stringResource(R.string.avatar_de, nombre)
    Box(
        modifier = modifier
            .size(tamano)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(RetroProfileCardBg)
            .border(1.dp, RetroProfileCardBorder, MaterialTheme.shapes.extraSmall)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = inicial,
            color = RetroGlossyDark,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarPreview() {
    RetroconverTheme {
        Avatar(nombre = "Yeray", modifier = Modifier.padding(8.dp))
    }
}
