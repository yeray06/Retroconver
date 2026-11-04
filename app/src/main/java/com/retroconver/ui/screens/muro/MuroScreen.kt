package com.retroconver.ui.screens.muro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.ui.theme.RetroconverTheme

/** Muro con las publicaciones, de la más reciente a la más antigua (RF-09). */
@Composable
fun MuroScreen(viewModel: MuroViewModel = viewModel(factory = MuroViewModel.Factory)) {
    val publicaciones by viewModel.publicaciones.collectAsStateWithLifecycle()
    MuroContenido(publicaciones)
}

/**
 * Dibuja el muro a partir de la lista que le llega: nada mientras carga (`null`),
 * el mensaje de «vacío» si no hay publicaciones, o la lista de tarjetas.
 */
@Composable
internal fun MuroContenido(
    publicaciones: List<PublicacionConAutor>?,
    modifier: Modifier = Modifier,
) {
    when {
        publicaciones == null -> Unit
        publicaciones.isEmpty() -> MuroVacio(modifier)
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(publicaciones, key = { it.publicacion.id }) { publicacion ->
                PublicacionCard(publicacion)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun MuroConPublicacionesPreview() {
    RetroconverTheme {
        MuroContenido(publicacionesDeEjemplo())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun MuroVacioEnPantallaPreview() {
    RetroconverTheme {
        MuroContenido(emptyList())
    }
}
