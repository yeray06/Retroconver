package com.retroconver.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/*
 * Migraciones de la base de datos Room. Cada una pasa de una versión a la siguiente sin
 * borrar la copia local, así al actualizar la app no hay que volver a descargarlo todo.
 *
 * Las versiones 1 a 4 eran de la app sin servidor (ids numéricos y un usuario local): esos datos
 * no tienen equivalente en el servidor, así que desde ellas la base de datos se crea de nuevo
 * (AppContainer). Desde la 5, la primera con servidor, siempre hay migración.
 *
 * Para cambiar el esquema:
 * 1. Sube `version` en AppDatabase y cambia la entidad.
 * 2. Escribe aquí la migración con el SQL que cambia la tabla y añádela a [MIGRACIONES].
 * 3. Compila: Room genera app/schemas/…/{versión}.json. Súbelo con la PR.
 * 4. Añade su prueba en MigracionesTest (androidTest).
 */

/** 5 → 6 (EX-03, #30): tipografía del perfil. */
val MIGRACION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE usuarios ADD COLUMN tipografia TEXT")
    }
}

/** Todas las migraciones, en orden. Se registran en AppContainer. */
val MIGRACIONES: Array<Migration> = arrayOf(MIGRACION_5_6)

/** Versiones sin servidor: desde ellas se borra la copia y se crea de nuevo. */
val VERSIONES_SIN_MIGRACION: IntArray = intArrayOf(1, 2, 3, 4)
