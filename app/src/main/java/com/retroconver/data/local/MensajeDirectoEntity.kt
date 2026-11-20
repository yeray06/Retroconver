package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Mensaje directo entre dos personas (EX-01, #28): copia de su fila de `mensajes_directos`.
 * Solo lo ven [remitenteId] y [destinatarioId] (lo garantiza el servidor). Un [zumbido] es un
 * mensaje especial que hace temblar la conversación, como en MSN.
 */
@Entity(
    tableName = "mensajes_directos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["remitenteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinatarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("remitenteId"), Index("destinatarioId")],
)
data class MensajeDirectoEntity(
    @PrimaryKey val id: String,
    val remitenteId: String,
    val destinatarioId: String,
    val texto: String,
    val zumbido: Boolean,
    val fechaCreacion: Instant,
)

/** Mensaje directo con las dos personas de la conversación. */
data class MensajeDirectoConPersonas(
    @Embedded val mensaje: MensajeDirectoEntity,
    @Relation(parentColumn = "remitenteId", entityColumn = "id")
    val remitente: UsuarioEntity,
    @Relation(parentColumn = "destinatarioId", entityColumn = "id")
    val destinatario: UsuarioEntity,
) {
    /** La otra persona de la conversación, vista desde [miId]. */
    fun otraPersona(miId: String): UsuarioEntity = if (remitente.id == miId) destinatario else remitente
}

/** Mensajes directos (EX-01, #28). No se borran: solo se guardan los que llegan del servidor. */
@Dao
abstract class MensajeDirectoDao {

    @Upsert
    abstract suspend fun guardar(mensajes: List<MensajeDirectoEntity>)

    /** Todos los mensajes en los que está [miId], el más reciente primero (para la lista de conversaciones). */
    @Transaction
    @Query(
        """
        SELECT * FROM mensajes_directos
        WHERE remitenteId = :miId OR destinatarioId = :miId
        ORDER BY fechaCreacion DESC, id DESC
        """,
    )
    abstract fun observarMios(miId: String): Flow<List<MensajeDirectoConPersonas>>

    /** La conversación entre [miId] y [otroId], del más antiguo al más reciente, como un chat. */
    @Transaction
    @Query(
        """
        SELECT * FROM mensajes_directos
        WHERE (remitenteId = :miId AND destinatarioId = :otroId)
           OR (remitenteId = :otroId AND destinatarioId = :miId)
        ORDER BY fechaCreacion ASC, id ASC
        """,
    )
    abstract fun observarConversacion(miId: String, otroId: String): Flow<List<MensajeDirectoConPersonas>>
}
