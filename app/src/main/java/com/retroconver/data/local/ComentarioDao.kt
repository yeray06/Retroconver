package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    /** Guarda un comentario nuevo. */
    @Insert
    suspend fun insertar(comentario: ComentarioEntity)

    /** Guarda los comentarios que llegan del servidor: crea los nuevos y actualiza los que ya estaban. */
    @Upsert
    suspend fun guardar(comentarios: List<ComentarioEntity>)

    /** Borra un comentario. */
    @Query("DELETE FROM comentarios WHERE id = :id")
    suspend fun eliminar(id: String)

    /** Quita los comentarios de la publicación que ya no están entre los descargados (borrados desde otro móvil). */
    @Query("DELETE FROM comentarios WHERE publicacionId = :publicacionId AND id NOT IN (:conservar)")
    suspend fun borrarQueYaNoEstan(publicacionId: String, conservar: List<String>)

    /** Comentarios de una publicación, el más reciente primero. */
    @Transaction
    @Query(
        """
        SELECT * FROM comentarios
        WHERE publicacionId = :publicacionId
        ORDER BY timestamp DESC, id DESC
        """,
    )
    fun observarDePublicacion(publicacionId: String): Flow<List<ComentarioConAutor>>
}
