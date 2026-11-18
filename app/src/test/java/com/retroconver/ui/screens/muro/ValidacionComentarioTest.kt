package com.retroconver.ui.screens.muro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionComentarioTest {

    @Test
    fun unComentarioVacioOSoloEspaciosNoSePuedeEnviar() {
        assertEquals(ErrorPublicacion.VACIO, validarComentario(""))
        assertEquals(ErrorPublicacion.VACIO, validarComentario("  \n "))
    }

    @Test
    fun unComentarioNormalSePuedeEnviar() {
        assertNull(validarComentario("¡Qué bueno!"))
    }

    @Test
    fun elLimiteEsElDelServidorYSeMideSinEspaciosDeLosBordes() {
        assertNull(validarComentario(" " + "a".repeat(LimitesComentario.TEXTO_MAX) + " "))
        assertEquals(ErrorPublicacion.DEMASIADO_LARGO, validarComentario("a".repeat(LimitesComentario.TEXTO_MAX + 1)))
    }
}
