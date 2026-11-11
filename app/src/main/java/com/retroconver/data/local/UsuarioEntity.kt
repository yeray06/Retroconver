package com.retroconver.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Perfil de una persona de la red (RF-05): copia local de su fila de `perfiles` en el servidor
 * (E4-05.1, #121). El servidor es quien manda; Room guarda lo último descargado para que las
 * pantallas se actualicen solas y se vean sin conexión.
 *
 * El id es el UUID de su cuenta de Supabase Auth. Es el autor de las publicaciones,
 * los comentarios y los mensajes.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    /** Nombre público. Obligatorio. */
    val nombre: String,
    /** Texto libre del perfil. Vacío si no se ha escrito. */
    @ColumnInfo(defaultValue = "") val biografia: String = "",
    /** Frase del día, como el estado de MSN Messenger. Vacío si no se ha escrito. */
    @ColumnInfo(defaultValue = "") val estado: String = "",
    /** Ruta del avatar dentro del bucket `imagenes` (`{id}/{uuid}.jpg`), o `null`. Sirve para borrarlo del servidor. */
    val avatarRuta: String? = null,
    /** Dirección del avatar lista para Coil (la URL pública de [avatarRuta]), o `null` si no tiene foto. */
    val avatarUri: String? = null,
    /** Color de fondo del perfil (RF-08, E4-11). `null` usa el de la guía de estilo. */
    val colorFondo: String? = null,
    /** Tema del perfil (RF-08, E4-11). `null` usa el de por defecto. */
    val tema: String? = null,
    val fechaAlta: Instant,
)
