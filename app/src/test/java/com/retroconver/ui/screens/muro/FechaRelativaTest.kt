package com.retroconver.ui.screens.muro

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class FechaRelativaTest {

    private val ahora = Instant.parse("2026-11-04T12:00:00Z")

    private fun hace(duracion: Duration) = transcurrido(ahora.minus(duracion), ahora)

    @Test
    fun menosDeUnMinutoCuentaComoUnMinuto() {
        assertEquals(Transcurrido(Unidad.MINUTOS, 1), hace(Duration.ofSeconds(20)))
    }

    @Test
    fun unaFechaFuturaPorElRelojDelMovilCuentaComoUnMinuto() {
        assertEquals(Transcurrido(Unidad.MINUTOS, 1), transcurrido(ahora.plusSeconds(90), ahora))
    }

    @Test
    fun hastaUnaHoraSeMuestranMinutos() {
        assertEquals(Transcurrido(Unidad.MINUTOS, 3), hace(Duration.ofMinutes(3)))
        assertEquals(Transcurrido(Unidad.MINUTOS, 59), hace(Duration.ofMinutes(59)))
    }

    @Test
    fun hastaUnDiaSeMuestranHoras() {
        assertEquals(Transcurrido(Unidad.HORAS, 1), hace(Duration.ofMinutes(60)))
        assertEquals(Transcurrido(Unidad.HORAS, 23), hace(Duration.ofHours(23).plusMinutes(59)))
    }

    @Test
    fun hastaUnaSemanaSeMuestranDias() {
        assertEquals(Transcurrido(Unidad.DIAS, 1), hace(Duration.ofHours(24)))
        assertEquals(Transcurrido(Unidad.DIAS, 6), hace(Duration.ofDays(6).plusHours(23)))
    }

    @Test
    fun apartirDeUnaSemanaSeMuestraLaFecha() {
        assertEquals(Unidad.FECHA, hace(Duration.ofDays(7)).unidad)
        assertEquals(Unidad.FECHA, hace(Duration.ofDays(400)).unidad)
    }
}
