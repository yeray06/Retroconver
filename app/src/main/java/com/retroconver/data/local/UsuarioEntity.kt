package com.retroconver.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Usuario local de la app.
 *
 * Versión mínima para que la base de datos tenga al menos una tabla, como exige Room.
 * E3-01 (#13) añade la biografía, el estado, el avatar, el DAO y el repositorio,
 * siguiendo el diagrama entidad-relación de E2-05.1 (#44).
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val fechaAlta: Instant,
)
