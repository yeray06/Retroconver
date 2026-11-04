package com.retroconver.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Publicación junto a su autor y al número de comentarios que tiene (RF-14).
 * El número se calcula en la consulta del DAO, no se guarda en ninguna columna.
 */
data class PublicacionConAutor(
    @Embedded val publicacion: PublicacionEntity,
    @Relation(parentColumn = "usuarioId", entityColumn = "id")
    val autor: UsuarioEntity,
    val numComentarios: Int,
)

/** Comentario junto al usuario que lo escribió. */
data class ComentarioConAutor(
    @Embedded val comentario: ComentarioEntity,
    @Relation(parentColumn = "usuarioId", entityColumn = "id")
    val autor: UsuarioEntity,
)
