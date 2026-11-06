package com.retroconver.ui.screens.muro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionPublicacionTest {

    @Test
    fun unTextoVacioNoSePuedePublicar() {
        assertEquals(ErrorPublicacion.VACIO, validarPublicacion(""))
    }

    @Test
    fun unTextoDeSoloEspaciosNoSePuedePublicar() {
        assertEquals(ErrorPublicacion.VACIO, validarPublicacion("   \n  "))
    }

    @Test
    fun unTextoNormalSePuedePublicar() {
        assertNull(validarPublicacion("Hola, muro"))
    }

    @Test
    fun eltextoEnElLimiteSePuedePublicar() {
        assertNull(validarPublicacion("a".repeat(LimitesPublicacion.TEXTO_MAX)))
    }

    @Test
    fun unTextoQuePasaDelLimiteNoSePuedePublicar() {
        assertEquals(ErrorPublicacion.DEMASIADO_LARGO, validarPublicacion("a".repeat(LimitesPublicacion.TEXTO_MAX + 1)))
    }

    @Test
    fun losEspaciosDeLosBordesNoCuentanParaElLimite() {
        assertNull(validarPublicacion("  " + "a".repeat(LimitesPublicacion.TEXTO_MAX) + "  "))
    }
}
