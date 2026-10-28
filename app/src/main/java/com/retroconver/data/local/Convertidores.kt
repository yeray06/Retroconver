package com.retroconver.data.local

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Convierte tipos que Room no sabe guardar a tipos que sí sabe.
 *
 * Las fechas se guardan como milisegundos desde 1970 (UTC). Al mostrarlas,
 * cada pantalla las pasa a la zona horaria del móvil.
 */
class Convertidores {

    @TypeConverter
    fun instanteAMilisegundos(instante: Instant?): Long? = instante?.toEpochMilli()

    @TypeConverter
    fun milisegundosAInstante(milisegundos: Long?): Instant? = milisegundos?.let(Instant::ofEpochMilli)
}
