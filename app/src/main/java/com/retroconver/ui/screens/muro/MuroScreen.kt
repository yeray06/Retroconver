package com.retroconver.ui.screens.muro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.R
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Muro con las publicaciones de la cuenta y sus amigos (RF-09 y RF-31), de la más reciente a la
 * más antigua.
 * Al abrirse descarga las nuevas; deslizando hacia abajo se vuelve a actualizar.
 */
@Composable
fun MuroScreen(
    alAbrirPublicacion: (String) -> Unit = {},
    viewModel: MuroViewModel = viewModel(factory = MuroViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    MuroContenido(
        publicaciones = estado.publicaciones,
        actualizando = estado.actualizando,
        error = estado.error,
        alActualizar = viewModel::actualizar,
        alAbrirPublicacion = alAbrirPublicacion,
    )
}

/**
 * Dibuja el muro a partir de la lista que le llega: nada mientras carga (`null`),
 * el mensaje de «vacío» si no hay publicaciones, o la lista de tarjetas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MuroContenido(
    publicaciones: List<PublicacionConAutor>?,
    modifier: Modifier = Modifier,
    actualizando: Boolean = false,
    error: Int? = null,
    alActualizar: () -> Unit = {},
    alAbrirPublicacion: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Sin conexión ya avisa la franja amarilla; si hay copia, no hace falta repetirlo
        val mensaje = error?.takeUnless { it == R.string.red_sin_conexion && !publicaciones.isNullOrEmpty() }
        if (mensaje != null) {
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
            isRefreshing = actualizando,
            onRefresh = alActualizar,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                publicaciones == null -> Unit
                // En una lista desplazable, para que también se pueda deslizar para actualizar con el muro vacío
                publicaciones.isEmpty() -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { MuroVacio(Modifier.fillParentMaxSize()) }
                }
                else -> ListaMuro(publicaciones, alAbrirPublicacion)
            }
        }
    }
}

/**
 * Lista de tarjetas. Si llega una publicación más reciente que la última vista (la que se
 * acaba de crear en el formulario), la lista vuelve arriba para que se vea.
 */
@Composable
private fun ListaMuro(
    publicaciones: List<PublicacionConAutor>,
    alAbrirPublicacion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val etiquetaAbrir = stringResource(R.string.muro_abrir_publicacion)
    val estado = rememberLazyListState()
    val idMasReciente = publicaciones.first().publicacion.id
    var idVisto by rememberSaveable { mutableStateOf(idMasReciente) }
    LaunchedEffect(idMasReciente) {
        if (idMasReciente != idVisto) {
            idVisto = idMasReciente
            estado.scrollToItem(0)
        }
    }
    LazyColumn(
        state = estado,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(publicaciones, key = { it.publicacion.id }) { publicacion ->
            PublicacionCard(
                item = publicacion,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.Button, onClickLabel = etiquetaAbrir) {
                        alAbrirPublicacion(publicacion.publicacion.id)
                    },
            )
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
