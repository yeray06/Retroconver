package com.retroconver.data.repository

import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlbumDto
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.FotoDto
import com.retroconver.data.remote.PerfilDto
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
