package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {

    /** Guarda una publicación nueva. */
    @Insert
    suspend fun insertar(publicacion: PublicacionEntity)

    /**
     * Guarda las publicaciones que llegan del servidor: crea las nuevas y actualiza las que ya
     * estaban, sin borrar sus comentarios.
     */
    @Upsert
    suspend fun guardar(publicaciones: List<PublicacionEntity>)

    /** Borra la publicación y, en cascada, sus comentarios. */
    @Query("DELETE FROM publicaciones WHERE id = :id")
    suspend fun eliminar(id: String)

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
    fun observarPorId(id: String): Flow<PublicacionConAutor?>
}
