package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Mensajes del tablón (E4-09.1, #72). Clase abstracta, como AmistadDao, para sincronizar con el
 * servidor en una sola transacción.
 */
@Dao
abstract class MensajeDao {

    /** Tablón de [perfilId]: sus mensajes con quien los escribió, el más reciente primero. */
    @Transaction
    @Query("SELECT * FROM mensajes WHERE perfilId = :perfilId ORDER BY fechaCreacion DESC, id DESC")
    abstract fun observarTablon(perfilId: String): Flow<List<MensajeConAutor>>

    @Upsert
    abstract suspend fun guardar(mensaje: MensajeEntity)

    @Upsert
    abstract suspend fun guardarTodos(mensajes: List<MensajeEntity>)

    @Query("DELETE FROM mensajes WHERE id = :id")
    abstract suspend fun borrar(id: String)

    @Query("DELETE FROM mensajes WHERE perfilId = :perfilId AND id NOT IN (:conservar)")
    protected abstract suspend fun borrarLosQueYaNoEstan(perfilId: String, conservar: List<String>)

    /**
     * Deja el tablón de [perfilId] igual que el del servidor: los mensajes borrados desde otro
     * móvil desaparecen y los de otros tablones no se tocan.
     */
    @Transaction
    open suspend fun sincronizarTablon(perfilId: String, mensajes: List<MensajeEntity>) {
        borrarLosQueYaNoEstan(perfilId, mensajes.map { it.id })
        guardarTodos(mensajes)
    }
}
