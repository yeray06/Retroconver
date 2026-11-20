package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * «Me gusta» de una persona en una publicación (EX-05, #130): copia de su fila de `me_gusta` en el
 * servidor. La clave es la pareja publicación y persona, así cada persona cuenta una sola vez.
 * Solo apunta a la publicación: quien da «Me gusta» puede no estar guardado en Room, y para el
 * contador basta con su id. Si se borra la publicación, se borran sus «Me gusta».
 */
@Entity(
    tableName = "me_gusta",
    primaryKeys = ["publicacionId", "usuarioId"],
    foreignKeys = [
        ForeignKey(
            entity = PublicacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["publicacionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("usuarioId")],
)
data class MeGustaEntity(
    val publicacionId: String,
    val usuarioId: String,
)

/** «Me gusta» de las publicaciones (EX-05, #130). El contador y el «mío» los calcula [PublicacionDao]. */
@Dao
abstract class MeGustaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun dar(meGusta: MeGustaEntity)

    @Query("DELETE FROM me_gusta WHERE publicacionId = :publicacionId AND usuarioId = :usuarioId")
    abstract suspend fun quitar(publicacionId: String, usuarioId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun darTodos(meGusta: List<MeGustaEntity>)

    @Query("DELETE FROM me_gusta WHERE publicacionId IN (:publicaciones)")
    protected abstract suspend fun quitarDe(publicaciones: List<String>)

    /**
     * Deja los «Me gusta» de [publicaciones] igual que en el servidor: los que se han quitado desde
     * otro móvil desaparecen. Las publicaciones tienen que estar ya guardadas (clave foránea).
     */
    @Transaction
    open suspend fun sincronizar(publicaciones: List<String>, meGusta: List<MeGustaEntity>) {
        if (publicaciones.isEmpty()) return
        quitarDe(publicaciones)
        darTodos(meGusta)
    }
}
