package com.retroconver.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Usuario de la app, con los datos de su perfil (RF-05).
 *
 * La app trabaja con un único usuario local, que se crea solo la primera vez que se abre
 * (RF-04, ver [com.retroconver.data.repository.UsuarioRepository]). Es el autor de las
 * publicaciones, los comentarios y los mensajes.
 *
 * El avatar se guarda como ruta a una copia reescalada a 720p en el almacenamiento interno,
 * nunca la imagen en sí (docs/datos/esquemadatos.md).
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Nombre público. Obligatorio. */
    val nombre: String,
    /** Texto libre del perfil. Vacío si no se ha escrito. */
    @ColumnInfo(defaultValue = "") val biografia: String = "",
    /** Frase del día, como el estado de MSN Messenger. Vacío si no se ha escrito. */
    @ColumnInfo(defaultValue = "") val estado: String = "",
    /** URI del fichero del avatar (`file:///…/files/images/….jpg`), o `null` si no tiene foto. */
    val avatarUri: String? = null,
    val fechaAlta: Instant,
)
