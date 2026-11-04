package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    /** Guarda el comentario y devuelve el id que le ha puesto Room. */
    @Insert
    suspend fun insertar(comentario: ComentarioEntity): Long

    /** Comentarios de una publicación, el más reciente primero. */
    @Transaction
    @Query(
        """
        SELECT * FROM comentarios
        WHERE publicacionId = :publicacionId
        ORDER BY timestamp DESC, id DESC
        """,
    )
    fun observarDePublicacion(publicacionId: Long): Flow<List<ComentarioConAutor>>
}
