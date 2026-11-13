package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Álbumes y fotos (E4-10.1, #68). Es una clase abstracta, y no una interfaz, para poder tener
 * las funciones `@Transaction` que sincronizan con el servidor en un solo paso.
 */
@Dao
abstract class GaleriaDao {

    /** Álbumes de una persona, el más reciente primero, con su número de fotos y su portada. */
    @Query(
        """
        SELECT albumes.*,
               (SELECT COUNT(*) FROM fotos WHERE fotos.albumId = albumes.id) AS numFotos,
               (SELECT fotos.uri FROM fotos WHERE fotos.albumId = albumes.id
                ORDER BY fotos.fechaAnadida DESC, fotos.id DESC LIMIT 1) AS portadaUri
        FROM albumes
        WHERE propietarioId = :propietarioId
        ORDER BY fechaCreacion DESC, id DESC
        """,
    )
    abstract fun observarAlbumesDe(propietarioId: String): Flow<List<AlbumConPortada>>

    /** Un álbum. Emite `null` si no existe o se ha borrado. */
    @Query("SELECT * FROM albumes WHERE id = :id")
    abstract fun observarAlbum(id: String): Flow<AlbumEntity?>

    /** Fotos de un álbum, la más reciente primero. */
    @Query("SELECT * FROM fotos WHERE albumId = :albumId ORDER BY fechaAnadida DESC, id DESC")
    abstract fun observarFotos(albumId: String): Flow<List<FotoEntity>>

    /** Una foto (para verla a pantalla completa). Emite `null` si se ha borrado. */
    @Query("SELECT * FROM fotos WHERE id = :id")
    abstract fun observarFoto(id: String): Flow<FotoEntity?>

    @Upsert
    abstract suspend fun guardarAlbum(album: AlbumEntity)

    @Upsert
    abstract suspend fun guardarAlbumes(albumes: List<AlbumEntity>)

    @Upsert
    abstract suspend fun guardarFoto(foto: FotoEntity)

    @Upsert
    abstract suspend fun guardarFotos(fotos: List<FotoEntity>)

    @Query("DELETE FROM fotos WHERE id = :id")
    abstract suspend fun borrarFoto(id: String)

    /** Borra el álbum y, en cascada, sus fotos. */
    @Query("DELETE FROM albumes WHERE id = :id")
    abstract suspend fun borrarAlbum(id: String)

    @Query("DELETE FROM albumes WHERE propietarioId = :propietarioId AND id NOT IN (:conservar)")
    protected abstract suspend fun borrarAlbumesQueYaNoEstan(propietarioId: String, conservar: List<String>)

    @Query("DELETE FROM fotos WHERE albumId = :albumId AND id NOT IN (:conservar)")
    protected abstract suspend fun borrarFotosQueYaNoEstan(albumId: String, conservar: List<String>)

    /**
     * Deja en Room exactamente los álbumes y fotos que tiene [propietarioId] en el servidor:
     * guarda los nuevos, actualiza los que ya estaban y quita los que se han borrado desde otro
     * móvil. Todo en una transacción, así la pantalla se actualiza una sola vez.
     */
    @Transaction
    open suspend fun sincronizarAlbumesDe(propietarioId: String, albumes: List<AlbumEntity>, fotos: List<FotoEntity>) {
        borrarAlbumesQueYaNoEstan(propietarioId, albumes.map { it.id })
        guardarAlbumes(albumes)
        albumes.forEach { album ->
            borrarFotosQueYaNoEstan(album.id, fotos.filter { it.albumId == album.id }.map { it.id })
        }
        guardarFotos(fotos)
    }

    /** Lo mismo que [sincronizarAlbumesDe], para un solo álbum y sus fotos. */
    @Transaction
    open suspend fun sincronizarAlbum(album: AlbumEntity, fotos: List<FotoEntity>) {
        guardarAlbum(album)
        borrarFotosQueYaNoEstan(album.id, fotos.map { it.id })
        guardarFotos(fotos)
    }
}
