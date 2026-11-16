package com.retroconver.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Amistad entre dos personas (EX-04, #129): copia local de su fila de `amistades` en el servidor.
 * [solicitanteId] la pidió y [destinatarioId] la acepta o la rechaza. Mientras no la acepta,
 * es una solicitud pendiente. Solo hay una por pareja, la pida quien la pida.
 */
@Entity(
    tableName = "amistades",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["solicitanteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinatarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("solicitanteId"), Index("destinatarioId")],
)
data class AmistadEntity(
    @PrimaryKey val id: String,
    val solicitanteId: String,
    val destinatarioId: String,
    val aceptada: Boolean,
    val fechaCreacion: Instant,
) {
    /** La otra persona de la amistad, vista desde [id]. */
    fun otraPersona(id: String): String = if (solicitanteId == id) destinatarioId else solicitanteId
}

/** Una solicitud pendiente con la persona que la envía o la recibe, para la lista del perfil. */
data class SolicitudConPersona(
    val idAmistad: String,
    @Embedded val persona: UsuarioEntity,
)
