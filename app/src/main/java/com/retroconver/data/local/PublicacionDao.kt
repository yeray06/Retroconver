package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Publicaciones del muro. Es una clase abstracta, y no una interfaz, para poder tener la función
 * `@Transaction` que sincroniza con el servidor en un solo paso (igual que [GaleriaDao]).
 */
@Dao
abstract class PublicacionDao {

    /** Guarda una publicación nueva. */
    @Insert
    abstract suspend fun insertar(publicacion: PublicacionEntity)

    /**
     * Guarda las publicaciones que llegan del servidor: crea las nuevas y actualiza las que ya
     * estaban, sin borrar sus comentarios.
     */
    @Upsert
    abstract suspend fun guardar(publicaciones: List<PublicacionEntity>)

    /** Guarda una sola publicación que llega del servidor (la que se acaba de crear). */
    @Upsert
    abstract suspend fun guardarUna(publicacion: PublicacionEntity)

    /** Borra la publicación y, en cascada, sus comentarios. */
    @Query("DELETE FROM publicaciones WHERE id = :id")
    abstract suspend fun eliminar(id: String)

    /** Quita las publicaciones desde [desde] (incluida) que ya no están entre las descargadas. */
    @Query("DELETE FROM publicaciones WHERE timestamp >= :desde AND id NOT IN (:conservar)")
    protected abstract suspend fun borrarDesdeQueYaNoEstan(desde: Instant, conservar: List<String>)

    /** Quita todas las publicaciones que no están entre las descargadas. */
    @Query("DELETE FROM publicaciones WHERE id NOT IN (:conservar)")
    protected abstract suspend fun borrarQueYaNoEstan(conservar: List<String>)

    /**
     * Deja el muro de Room igual que el del servidor: guarda las publicaciones descargadas y
     * quita las que se han borrado desde otro móvil. Todo en una transacción, así el muro se
     * actualiza una sola vez.
     *
     * El muro se descarga por tramos (las más recientes). Si [hayMasAntiguas] es `true`, solo se
     * quitan las que son tan recientes como la más antigua descargada: las anteriores no se han
     * pedido y no se pueden dar por borradas.
     */
    @Transaction
    open suspend fun sincronizar(publicaciones: List<PublicacionEntity>, hayMasAntiguas: Boolean) {
        val conservar = publicaciones.map { it.id }
        val masAntigua = publicaciones.minOfOrNull { it.timestamp }
        if (hayMasAntiguas && masAntigua != null) {
            borrarDesdeQueYaNoEstan(masAntigua, conservar)
        } else {
            borrarQueYaNoEstan(conservar)
        }
        guardar(publicaciones)
    }

    /**
     * Todas las publicaciones, la más reciente primero. El `id` desempata si coincide la fecha.
     * [miId] es la cuenta de la sesión, para saber a cuáles les ha dado «Me gusta» (EX-05).
     */
    @Transaction
    @Query(
        """
        SELECT *, $CONTADORES
        FROM publicaciones
        ORDER BY timestamp DESC, id DESC
        """,
    )
    abstract fun observarTodas(miId: String): Flow<List<PublicacionConAutor>>

    /** Una publicación (para la pantalla de detalle). Emite `null` si no existe o se ha borrado. */
    @Transaction
    @Query(
        """
        SELECT *, $CONTADORES
        FROM publicaciones
        WHERE id = :id
        """,
    )
    abstract fun observarPorId(id: String, miId: String): Flow<PublicacionConAutor?>
}

/** Columnas calculadas de [PublicacionConAutor]: comentarios, «Me gusta» y si el mío está entre ellos. */
private const val CONTADORES = """
    (SELECT COUNT(*) FROM comentarios WHERE comentarios.publicacionId = publicaciones.id) AS numComentarios,
    (SELECT COUNT(*) FROM me_gusta WHERE me_gusta.publicacionId = publicaciones.id) AS numMeGusta,
    EXISTS (SELECT 1 FROM me_gusta WHERE me_gusta.publicacionId = publicaciones.id AND me_gusta.usuarioId = :miId) AS meGustaMio
"""
