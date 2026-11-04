package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {

    /** Guarda la publicación y devuelve el id que le ha puesto Room. */
    @Insert
    suspend fun insertar(publicacion: PublicacionEntity): Long

    /** Borra la publicación y, en cascada, sus comentarios. */
    @Query("DELETE FROM publicaciones WHERE id = :id")
    suspend fun eliminar(id: Long)

    /** Todas las publicaciones, la más reciente primero. El `id` desempata si coincide la fecha. */
    @Transaction
    @Query(
        """
        SELECT *,
               (SELECT COUNT(*) FROM comentarios WHERE comentarios.publicacionId = publicaciones.id) AS numComentarios
        FROM publicaciones
        ORDER BY timestamp DESC, id DESC
        """,
    )
    fun observarTodas(): Flow<List<PublicacionConAutor>>

    /** Una publicación (para la pantalla de detalle). Emite `null` si no existe o se ha borrado. */
    @Transaction
    @Query(
        """
        SELECT *,
               (SELECT COUNT(*) FROM comentarios WHERE comentarios.publicacionId = publicaciones.id) AS numComentarios
        FROM publicaciones
        WHERE id = :id
        """,
    )
    fun observarPorId(id: Long): Flow<PublicacionConAutor?>
}
