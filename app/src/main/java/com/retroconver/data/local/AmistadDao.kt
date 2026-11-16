package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Amistades y solicitudes (EX-04, #129). Clase abstracta, como GaleriaDao, para sincronizar con
 * el servidor en una sola transacción.
 */
@Dao
abstract class AmistadDao {

    /** Personas con las que [id] tiene una amistad aceptada, por orden alfabético. */
    @Query(
        """
        SELECT usuarios.* FROM amistades
        JOIN usuarios ON usuarios.id = CASE WHEN amistades.solicitanteId = :id
                                            THEN amistades.destinatarioId ELSE amistades.solicitanteId END
        WHERE amistades.aceptada = 1 AND (amistades.solicitanteId = :id OR amistades.destinatarioId = :id)
        ORDER BY usuarios.nombre COLLATE NOCASE, usuarios.id
        """,
    )
    abstract fun observarAmigosDe(id: String): Flow<List<UsuarioEntity>>

    /** Ids de los amigos de [id] (para el muro de amigos). */
    @Query(
        """
        SELECT CASE WHEN solicitanteId = :id THEN destinatarioId ELSE solicitanteId END
        FROM amistades WHERE aceptada = 1 AND (solicitanteId = :id OR destinatarioId = :id)
        """,
    )
    abstract fun observarIdsDeAmigos(id: String): Flow<List<String>>

    /** Solicitudes que ha recibido [id] y no ha contestado, con quien las envía. La más reciente primero. */
    @Query(
        """
        SELECT amistades.id AS idAmistad, usuarios.* FROM amistades
        JOIN usuarios ON usuarios.id = amistades.solicitanteId
        WHERE amistades.destinatarioId = :id AND amistades.aceptada = 0
        ORDER BY amistades.fechaCreacion DESC, amistades.id
        """,
    )
    abstract fun observarSolicitudesRecibidas(id: String): Flow<List<SolicitudConPersona>>

    /** Solicitudes que ha enviado [id] y siguen sin contestar, con quien las recibe. */
    @Query(
        """
        SELECT amistades.id AS idAmistad, usuarios.* FROM amistades
        JOIN usuarios ON usuarios.id = amistades.destinatarioId
        WHERE amistades.solicitanteId = :id AND amistades.aceptada = 0
        ORDER BY amistades.fechaCreacion DESC, amistades.id
        """,
    )
    abstract fun observarSolicitudesEnviadas(id: String): Flow<List<SolicitudConPersona>>

    /** La amistad o solicitud entre [a] y [b], la pidiera quien la pidiera, o `null` si no hay. */
    @Query(
        """
        SELECT * FROM amistades
        WHERE (solicitanteId = :a AND destinatarioId = :b) OR (solicitanteId = :b AND destinatarioId = :a)
        LIMIT 1
        """,
    )
    abstract fun observarEntre(a: String, b: String): Flow<AmistadEntity?>

    @Upsert
    abstract suspend fun guardar(amistad: AmistadEntity)

    @Upsert
    abstract suspend fun guardarTodas(amistades: List<AmistadEntity>)

    @Query("DELETE FROM amistades WHERE id = :id")
    abstract suspend fun borrar(id: String)

    @Query("DELETE FROM amistades WHERE (solicitanteId = :id OR destinatarioId = :id) AND id NOT IN (:conservar)")
    protected abstract suspend fun borrarLasDeQueYaNoEstan(id: String, conservar: List<String>)

    @Query(
        """
        DELETE FROM amistades
        WHERE aceptada = 1 AND (solicitanteId = :id OR destinatarioId = :id) AND id NOT IN (:conservar)
        """,
    )
    protected abstract suspend fun borrarAmistadesDeQueYaNoEstan(id: String, conservar: List<String>)

    /**
     * Deja en Room exactamente las amistades y solicitudes de [id] que hay en el servidor
     * (las de la cuenta de la sesión). Las que se han borrado o rechazado desaparecen.
     */
    @Transaction
    open suspend fun sincronizarTodasDe(id: String, amistades: List<AmistadEntity>) {
        borrarLasDeQueYaNoEstan(id, amistades.map { it.id })
        guardarTodas(amistades)
    }

    /** Lo mismo, solo con las amistades aceptadas de otra persona (lo que se ve en su perfil). */
    @Transaction
    open suspend fun sincronizarAmistadesDe(id: String, amistades: List<AmistadEntity>) {
        borrarAmistadesDeQueYaNoEstan(id, amistades.map { it.id })
        guardarTodas(amistades)
    }
}
