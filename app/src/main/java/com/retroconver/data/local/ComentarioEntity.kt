package com.retroconver.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Comentario de una publicación. Si se borra la publicación o el usuario que lo escribió,
 * el comentario se borra también.
 */
@Entity(
    tableName = "comentarios",
    foreignKeys = [
        ForeignKey(
            entity = PublicacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["publicacionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("publicacionId"), Index("usuarioId")],
)
data class ComentarioEntity(
    @PrimaryKey val id: String,
    val publicacionId: String,
    val usuarioId: String,
    val texto: String,
    val timestamp: Instant,
)
