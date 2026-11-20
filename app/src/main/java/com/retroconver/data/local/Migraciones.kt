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

/** 6 → 7 (EX-04, #129): amistades y solicitudes. */
val MIGRACION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `amistades` (`id` TEXT NOT NULL, `solicitanteId` TEXT NOT NULL, " +
                "`destinatarioId` TEXT NOT NULL, `aceptada` INTEGER NOT NULL, `fechaCreacion` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`solicitanteId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`destinatarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_amistades_solicitanteId` ON `amistades` (`solicitanteId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_amistades_destinatarioId` ON `amistades` (`destinatarioId`)")
    }
}

/** 7 → 8 (E4-09, #72): mensajes del tablón. */
val MIGRACION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `mensajes` (`id` TEXT NOT NULL, `perfilId` TEXT NOT NULL, " +
                "`autorId` TEXT NOT NULL, `texto` TEXT NOT NULL, `fechaCreacion` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`perfilId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`autorId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensajes_perfilId` ON `mensajes` (`perfilId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensajes_autorId` ON `mensajes` (`autorId`)")
    }
}

/**
 * 8 → 9: los extras de Muro y Mensajes. «Me gusta» (EX-05, #130), respuestas a comentarios
 * (EX-02, #29) y mensajes directos (EX-01, #28).
 */
val MIGRACION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `me_gusta` (`publicacionId` TEXT NOT NULL, `usuarioId` TEXT NOT NULL, " +
                "PRIMARY KEY(`publicacionId`, `usuarioId`), " +
                "FOREIGN KEY(`publicacionId`) REFERENCES `publicaciones`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_me_gusta_usuarioId` ON `me_gusta` (`usuarioId`)")
        db.execSQL("ALTER TABLE comentarios ADD COLUMN respuestaA TEXT")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `mensajes_directos` (`id` TEXT NOT NULL, `remitenteId` TEXT NOT NULL, " +
                "`destinatarioId` TEXT NOT NULL, `texto` TEXT NOT NULL, `zumbido` INTEGER NOT NULL, " +
                "`fechaCreacion` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`remitenteId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`destinatarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensajes_directos_remitenteId` ON `mensajes_directos` (`remitenteId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensajes_directos_destinatarioId` ON `mensajes_directos` (`destinatarioId`)")
    }
}

/** Todas las migraciones, en orden. Se registran en AppContainer. */
val MIGRACIONES: Array<Migration> = arrayOf(MIGRACION_5_6, MIGRACION_6_7, MIGRACION_7_8, MIGRACION_8_9)

/** Versiones sin servidor: desde ellas se borra la copia y se crea de nuevo. */
val VERSIONES_SIN_MIGRACION: IntArray = intArrayOf(1, 2, 3, 4)
