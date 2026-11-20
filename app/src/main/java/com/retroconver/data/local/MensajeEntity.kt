package com.retroconver.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant

/**
 * Mensaje del tablón de un perfil (E4-09.1, #72): copia local de su fila de `mensajes` en el
 * servidor. [perfilId] es el dueño del tablón y [autorId] quien lo escribe. Si se borra
 * cualquiera de los dos perfiles, el mensaje se borra también.
 */
@Entity(
    tableName = "mensajes",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["autorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("perfilId"), Index("autorId")],
)
data class MensajeEntity(
    @PrimaryKey val id: String,
    val perfilId: String,
    val autorId: String,
    val texto: String,
    val fechaCreacion: Instant,
)

/** Mensaje del tablón junto a la persona que lo escribió. */
data class MensajeConAutor(
    @Embedded val mensaje: MensajeEntity,
    @Relation(parentColumn = "autorId", entityColumn = "id")
    val autor: UsuarioEntity,
)
