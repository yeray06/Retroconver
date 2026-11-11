package com.retroconver.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    /**
     * Guarda el perfil que llega del servidor: lo crea si no está y lo actualiza si ya estaba.
     * No borra nada, así sus publicaciones y comentarios se quedan.
     */
    @Upsert
    suspend fun guardar(usuario: UsuarioEntity)

    /** Igual que [guardar], para varios perfiles a la vez (por ejemplo, los autores del muro). */
    @Upsert
    suspend fun guardarTodos(usuarios: List<UsuarioEntity>)

    /** El usuario con ese id. Emite otra vez cada vez que cambia, y `null` si no existe. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observar(id: String): Flow<UsuarioEntity?>

    /** Lectura puntual del usuario, sin quedarse observando. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtener(id: String): UsuarioEntity?

    /** Cambia los datos de texto del perfil. No toca el avatar ni la fecha de alta. */
    @Query("UPDATE usuarios SET nombre = :nombre, biografia = :biografia, estado = :estado WHERE id = :id")
    suspend fun actualizarDatos(id: String, nombre: String, biografia: String, estado: String)

    /** Cambia el avatar (`null` lo quita): su ruta en el servidor y la dirección para mostrarlo. */
    @Query("UPDATE usuarios SET avatarRuta = :avatarRuta, avatarUri = :avatarUri WHERE id = :id")
    suspend fun actualizarAvatar(id: String, avatarRuta: String?, avatarUri: String?)
}
