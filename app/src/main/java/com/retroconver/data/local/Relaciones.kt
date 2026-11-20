package com.retroconver.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Publicación junto a su autor, el número de comentarios que tiene (RF-14) y sus «Me gusta»
 * (EX-05, #130). Los números se calculan en la consulta del DAO, no se guardan en ninguna columna.
 * [meGustaMio] dice si la cuenta de la sesión le ha dado «Me gusta».
 */
data class PublicacionConAutor(
    @Embedded val publicacion: PublicacionEntity,
    @Relation(parentColumn = "usuarioId", entityColumn = "id")
    val autor: UsuarioEntity,
    val numComentarios: Int,
    val numMeGusta: Int = 0,
    val meGustaMio: Boolean = false,
)

/** Comentario junto al usuario que lo escribió. */
data class ComentarioConAutor(
    @Embedded val comentario: ComentarioEntity,
    @Relation(parentColumn = "usuarioId", entityColumn = "id")
    val autor: UsuarioEntity,
)
