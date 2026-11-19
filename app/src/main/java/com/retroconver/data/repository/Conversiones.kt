package com.retroconver.data.repository

import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.AmistadEntity
import com.retroconver.data.local.ComentarioEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.MensajeEntity
import com.retroconver.data.local.PublicacionEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlbumDto
import com.retroconver.data.remote.AmistadDto
import com.retroconver.data.remote.ComentarioDto
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.FotoDto
import com.retroconver.data.remote.MensajeDto
import com.retroconver.data.remote.PerfilDto
import com.retroconver.data.remote.PublicacionDto
import com.retroconver.data.remote.instanteDelServidor

/**
 * Pasa una fila de `perfiles` del servidor a la copia de Room (E4-05.1, #121).
 * La ruta del avatar se convierte en su URL pública para que Coil la cargue sin token.
 */
fun PerfilDto.aEntidad(configuracion: ConfiguracionSupabase): UsuarioEntity = UsuarioEntity(
    id = id,
    nombre = nombre,
    biografia = biografia,
    estado = estado,
    avatarRuta = avatarRuta,
    avatarUri = avatarRuta?.let { configuracion.urlPublica(it) },
    colorFondo = colorFondo,
    tema = tema,
    tipografia = tipografia,
    fechaAlta = instanteDelServidor(creadoEn),
)

/** Pasa una fila de `albumes` a Room (E4-10.1, #68). Su dueño y sus fotos se convierten aparte. */
fun AlbumDto.aEntidad(): AlbumEntity = AlbumEntity(
    id = id,
    propietarioId = propietarioId,
    titulo = titulo,
    descripcion = descripcion,
    fechaCreacion = instanteDelServidor(creadoEn),
)

/** Pasa una fila de `fotos` a Room, con la URL pública para Coil (E4-10.1, #68). */
fun FotoDto.aEntidad(configuracion: ConfiguracionSupabase): FotoEntity = FotoEntity(
    id = id,
    albumId = albumId,
    propietarioId = propietarioId,
    ruta = ruta,
    uri = configuracion.urlPublica(ruta),
    ancho = ancho,
    alto = alto,
    fechaAnadida = instanteDelServidor(creadaEn),
)

/** Pasa una fila de `amistades` a Room (EX-04, #129). Los perfiles que trae se guardan aparte. */
fun AmistadDto.aEntidad(): AmistadEntity = AmistadEntity(
    id = id,
    solicitanteId = solicitanteId,
    destinatarioId = destinatarioId,
    aceptada = aceptada,
    fechaCreacion = instanteDelServidor(creadaEn),
)

/**
 * Pasa una fila de `publicaciones` a Room (E4-06, #123). Su autor y sus comentarios se convierten
 * aparte. La ruta de la imagen se convierte en su URL pública para que Coil la cargue sin token.
 */
fun PublicacionDto.aEntidad(configuracion: ConfiguracionSupabase): PublicacionEntity = PublicacionEntity(
    id = id,
    usuarioId = autorId,
    texto = texto,
    imagenRuta = imagenRuta,
    imagenUri = imagenRuta?.let { configuracion.urlPublica(it) },
    timestamp = instanteDelServidor(creadaEn),
)

/** Pasa una fila de `comentarios` a Room (E4-07, #18). */
fun ComentarioDto.aEntidad(): ComentarioEntity = ComentarioEntity(
    id = id,
    publicacionId = publicacionId,
    usuarioId = autorId,
    texto = texto,
    timestamp = instanteDelServidor(creadoEn),
)

/** Pasa una fila de `mensajes` a Room (E4-09, #72). */
fun MensajeDto.aEntidad(): MensajeEntity = MensajeEntity(
    id = id,
    perfilId = perfilId,
    autorId = autorId,
    texto = texto,
    fechaCreacion = instanteDelServidor(creadoEn),
)
