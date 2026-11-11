package com.retroconver.data.repository

import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.ConfiguracionSupabase
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
