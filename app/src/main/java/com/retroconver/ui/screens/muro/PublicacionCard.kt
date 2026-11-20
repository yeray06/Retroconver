package com.retroconver.ui.screens.muro

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
 * Al pie, el número de comentarios (E4-07.2, #67) y el botón ♥ con los «Me gusta» (EX-05, #130):
 * con [alCambiarMeGusta] se puede pulsar para darlo o quitarlo.
 * Con [alAbrirAutor], el avatar y el nombre abren el perfil de quien la publica (E4-08.1, #127).
 */
@Composable
fun PublicacionCard(
    item: PublicacionConAutor,
    modifier: Modifier = Modifier,
    alAbrirAutor: ((String) -> Unit)? = null,
    alCambiarMeGusta: (() -> Unit)? = null,
) {
    val publicacion = item.publicacion
    val autor = item.autor
    val etiquetaPerfil = stringResource(R.string.personas_ver_perfil_de, autor.nombre)
    // La fila del autor se pulsa aparte de la tarjeta: TalkBack la anuncia como un botón propio
    val filaAutor = if (alAbrirAutor != null) {
        Modifier
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = etiquetaPerfil) { alAbrirAutor(autor.id) }
    } else {
        Modifier
    }
    RetroCard(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = filaAutor) {
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
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pluralStringResource(R.plurals.muro_num_comentarios, item.numComentarios, item.numComentarios),
                style = MaterialTheme.typography.bodySmall,
                color = RetroGrayText,
                modifier = Modifier.weight(1f),
            )
            BotonMeGusta(
                numero = item.numMeGusta,
                mio = item.meGustaMio,
                alPulsar = alCambiarMeGusta,
            )
        }
    }
}

/**
 * ♥ con el número de «Me gusta» (EX-05, #130). Relleno y en rojo si me gusta a mí. Si se puede
 * pulsar, TalkBack lo anuncia como un botón que da o quita el «Me gusta», con el número.
 */
@Composable
private fun BotonMeGusta(numero: Int, mio: Boolean, alPulsar: (() -> Unit)?) {
    val descripcion = stringResource(R.string.me_gusta_descripcion, numero)
    val accion = stringResource(if (mio) R.string.me_gusta_quitar else R.string.me_gusta_dar)
    val pulsable = if (alPulsar != null) {
        Modifier
            .clip(MaterialTheme.shapes.small)
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = accion) { alPulsar() }
            .semantics(mergeDescendants = true) {
                contentDescription = descripcion
                stateDescription = accion
            }
    } else {
        Modifier.semantics { contentDescription = descripcion }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = pulsable.padding(horizontal = 8.dp),
    ) {
        Text(
            text = (if (mio) "♥ " else "♡ ") + numero,
            style = MaterialTheme.typography.titleSmall,
            color = if (mio) RetroCorazon else RetroGrayText,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

/** Rojo del ♥ cuando me gusta: el de los bocetos, con contraste suficiente sobre la tarjeta blanca. */
private val RetroCorazon = Color(0xFFC62828)

/** Datos de ejemplo de los bocetos, solo para las vistas previas. */
internal fun publicacionesDeEjemplo(): List<PublicacionConAutor> {
    val ahora = Instant.now()
    val cris = UsuarioEntity(id = "cris", nombre = "Cris Reed", fechaAlta = ahora)
    val simon = UsuarioEntity(id = "simon", nombre = "Simon Garcia", fechaAlta = ahora)
    return listOf(
        PublicacionConAutor(
            publicacion = PublicacionEntity(
                id = "2",
                usuarioId = "simon",
                texto = "Desayunando en la terraza mientras escucho música en MSN. ¡Hoy va a ser un gran día de 2008!",
                timestamp = ahora.minusSeconds(3 * 60),
            ),
            autor = simon,
            numComentarios = 0,
        ),
        PublicacionConAutor(
            publicacion = PublicacionEntity(
                id = "1",
                usuarioId = "cris",
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
