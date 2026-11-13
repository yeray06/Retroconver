package com.retroconver.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Publicación del muro. Pertenece a un usuario: si se borra el usuario, se borran sus publicaciones.
 * Desde E4-05.1 (#121) los ids son los UUID del servidor.
 *
 * Solo se guarda la ruta de la imagen (ya reescalada a 720p), nunca la imagen en sí.
 * Siguiendo docs/datos/esquemadatos.md: el contador de «Me gusta» no se crea hasta que se decida
 * si se añade esa función, y el número de comentarios se calcula con una consulta.
 */
@Entity(
    tableName = "publicaciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("usuarioId"), Index("timestamp")],
)
data class PublicacionEntity(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val texto: String,
    /** Ruta de la imagen dentro del bucket `imagenes`, o `null`. Sirve para borrarla del servidor. */
    val imagenRuta: String? = null,
    /** Dirección de la imagen lista para Coil (URL pública o fichero local), o `null` si no tiene. */
    val imagenUri: String? = null,
    val timestamp: Instant,
)
