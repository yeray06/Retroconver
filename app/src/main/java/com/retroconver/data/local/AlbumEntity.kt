package com.retroconver.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Álbum de fotos (RF-18): copia local de su fila de `albumes` en el servidor (E4-10.1, #68).
 * Pertenece a una persona: si se borra su perfil, se borran sus álbumes y, en cascada, sus fotos.
 */
@Entity(
    tableName = "albumes",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["propietarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("propietarioId")],
)
data class AlbumEntity(
    @PrimaryKey val id: String,
    val propietarioId: String,
    /** Nombre del álbum. Obligatorio, de 1 a 40 caracteres. */
    val titulo: String,
    @ColumnInfo(defaultValue = "") val descripcion: String = "",
    val fechaCreacion: Instant,
)

/**
 * Foto de un álbum (RF-19 y RF-20): copia local de su fila de `fotos` (E4-10.1, #68).
 * Si se borra el álbum, se borran sus fotos.
 */
@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("albumId")],
)
data class FotoEntity(
    @PrimaryKey val id: String,
    val albumId: String,
    val propietarioId: String,
    /** Ruta dentro del bucket `imagenes` (`{id de la cuenta}/{uuid}.jpg`). Sirve para borrarla del servidor. */
    val ruta: String,
    /** URL pública de [ruta], lista para Coil. */
    val uri: String,
    /** Tamaño en píxeles de la copia a 720p, si se conoce. */
    val ancho: Int? = null,
    val alto: Int? = null,
    val fechaAnadida: Instant,
)

/**
 * Álbum con lo que enseña la lista de la galería (RF-18): cuántas fotos tiene y la más
 * reciente como portada. Las dos cosas se calculan en la consulta, no se guardan.
 */
data class AlbumConPortada(
    @Embedded val album: AlbumEntity,
    val numFotos: Int,
    /** URL de la foto más reciente, o `null` si el álbum está vacío. */
    val portadaUri: String?,
)
