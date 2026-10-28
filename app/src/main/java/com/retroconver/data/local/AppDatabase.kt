package com.retroconver.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos local de Retroconver.
 *
 * Para añadir una tabla:
 * 1. Crea la entidad (@Entity) y su DAO (@Dao) en este paquete.
 * 2. Añade la entidad a la lista `entities` de abajo.
 * 3. Añade una función abstracta que devuelva el DAO, por ejemplo `abstract fun publicacionDao(): PublicacionDao`.
 * 4. Sube `version` en 1. El esquema nuevo se guarda solo en app/schemas: súbelo con tu PR.
 */
@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Convertidores::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        /** Nombre del fichero de la base de datos en el móvil. */
        const val NOMBRE = "retroconver.db"
    }
}
