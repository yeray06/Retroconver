package com.retroconver.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Publicación del muro. Pertenece a un usuario: si se borra el usuario, se borran sus publicaciones.
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val texto: String,
    val imagenUri: String? = null,
    val timestamp: Instant,
)
