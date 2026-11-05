package com.retroconver.ui.screens.muro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.retroconver.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Cuánto tiempo ha pasado desde una publicación, en la unidad que toca mostrar. */
enum class Unidad { MINUTOS, HORAS, DIAS, FECHA }

data class Transcurrido(val unidad: Unidad, val valor: Long)

/**
 * Calcula el tiempo transcurrido entre [fecha] y [ahora] para mostrarlo como en los bocetos
 * («hace 1m»): minutos hasta la hora, horas hasta el día, días hasta la semana y, a partir de
 * ahí, la fecha. Menos de un minuto (o una fecha futura por el reloj del móvil) cuenta como 1 minuto.
 */
fun transcurrido(fecha: Instant, ahora: Instant): Transcurrido {
    val minutos = Duration.between(fecha, ahora).toMinutes()
    return when {
        minutos < 60 -> Transcurrido(Unidad.MINUTOS, minutos.coerceAtLeast(1))
        minutos < 24 * 60 -> Transcurrido(Unidad.HORAS, minutos / 60)
        minutos < 7 * 24 * 60 -> Transcurrido(Unidad.DIAS, minutos / (24 * 60))
        else -> Transcurrido(Unidad.FECHA, 0)
    }
}

/** Texto de la fecha de una publicación: «hace 3m», «hace 2h», «hace 4d» o la fecha completa. */
@Composable
fun textoFecha(fecha: Instant): String {
    val tiempo = transcurrido(fecha, Instant.now())
    return when (tiempo.unidad) {
        Unidad.MINUTOS -> stringResource(R.string.fecha_hace_minutos, tiempo.valor)
        Unidad.HORAS -> stringResource(R.string.fecha_hace_horas, tiempo.valor)
        Unidad.DIAS -> stringResource(R.string.fecha_hace_dias, tiempo.valor)
        Unidad.FECHA -> remember(fecha) {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault())
                .format(fecha)
        }
    }
}
