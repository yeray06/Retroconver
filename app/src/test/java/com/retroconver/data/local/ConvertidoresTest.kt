package com.retroconver.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ConvertidoresTest {

    private val convertidores = Convertidores()

    @Test
    fun unaFechaSeRecuperaIgualDespuesDeGuardarla() {
        val fecha = Instant.parse("2026-10-28T10:15:30.123Z")
        val guardada = convertidores.instanteAMilisegundos(fecha)
        assertEquals(fecha, convertidores.milisegundosAInstante(guardada))
    }

    @Test
    fun unaFechaVaciaSigueVacia() {
        assertNull(convertidores.instanteAMilisegundos(null))
        assertNull(convertidores.milisegundosAInstante(null))
    }
}
