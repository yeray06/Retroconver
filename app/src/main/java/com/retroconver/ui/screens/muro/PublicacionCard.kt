package com.retroconver.ui.screens.muro

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.retroconver.R
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.local.PublicacionEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.ui.components.Avatar
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.theme.RetroCardBorder
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroLinkBlue
import com.retroconver.ui.theme.RetroTextDark
import com.retroconver.ui.theme.RetroconverTheme
import java.time.Instant

/**
 * Tarjeta de una publicación del muro: avatar, nombre, fecha, texto y, si la tiene, imagen.
 * Medidas del prototipo final (docs/diseño/diseñofinalvisual): avatar de 36 dp, imagen de 160 dp de alto.
 * Los contadores de comentarios y «Me gusta» se añaden con E4-01.2 (#67) y si se decide el «Me gusta».
 */
@Composable
fun PublicacionCard(
    item: PublicacionConAutor,
    modifier: Modifier = Modifier,
) {
    val publicacion = item.publicacion
    val autor = item.autor
    RetroCard(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(nombre = autor.nombre, tamano = 36.dp, imagenUri = autor.avatarUri)
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = autor.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = RetroLinkBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = textoFecha(publicacion.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroGrayText,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = publicacion.texto,
            style = MaterialTheme.typography.bodyLarge,
            color = RetroTextDark,
        )
        publicacion.imagenUri?.let { imagen ->
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = imagen,
                contentDescription = stringResource(R.string.imagen_de_publicacion, autor.nombre),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(1.dp, RetroCardBorder, MaterialTheme.shapes.extraSmall),
            )
        }
    }
}

/** Datos de ejemplo de los bocetos, solo para las vistas previas. */
internal fun publicacionesDeEjemplo(): List<PublicacionConAutor> {
    val ahora = Instant.now()
    val cris = UsuarioEntity(id = 1, nombre = "Cris Reed", fechaAlta = ahora)
    val simon = UsuarioEntity(id = 2, nombre = "Simon Garcia", fechaAlta = ahora)
    return listOf(
        PublicacionConAutor(
            publicacion = PublicacionEntity(
                id = 2,
                usuarioId = 2,
                texto = "Desayunando en la terraza mientras escucho música en MSN. ¡Hoy va a ser un gran día de 2008!",
                timestamp = ahora.minusSeconds(3 * 60),
            ),
            autor = simon,
            numComentarios = 0,
        ),
        PublicacionConAutor(
            publicacion = PublicacionEntity(
                id = 1,
                usuarioId = 1,
                texto = "¡Adiós Nueva York! Menudo viaje, soy un poquito más pobre de lo que era, pero mereció la pena.",
                timestamp = ahora.minusSeconds(2 * 60 * 60),
            ),
            autor = cris,
            numComentarios = 1,
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFC5D7E8)
@Composable
private fun PublicacionCardPreview() {
    RetroconverTheme {
        PublicacionCard(item = publicacionesDeEjemplo().first(), modifier = Modifier.padding(16.dp))
    }
}
