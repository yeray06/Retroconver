package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    /**
     * Guarda el usuario solo si no existe ya otro con el mismo `id`.
     * Devuelve el id de la fila nueva, o -1 si ya existía y no se ha tocado.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(usuario: UsuarioEntity): Long

    /** Guarda un usuario nuevo y devuelve el id que le ha puesto Room. */
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    /** El usuario con ese id. Emite otra vez cada vez que cambia, y `null` si no existe. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observar(id: Long): Flow<UsuarioEntity?>

    /** Lectura puntual del usuario, sin quedarse observando. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtener(id: Long): UsuarioEntity?

    /** Cambia los datos de texto del perfil. No toca el avatar ni la fecha de alta. */
    @Query("UPDATE usuarios SET nombre = :nombre, biografia = :biografia, estado = :estado WHERE id = :id")
    suspend fun actualizarDatos(id: Long, nombre: String, biografia: String, estado: String)

    /** Cambia la ruta del avatar (`null` lo quita). */
    @Query("UPDATE usuarios SET avatarUri = :avatarUri WHERE id = :id")
    suspend fun actualizarAvatar(id: Long, avatarUri: String?)
}
