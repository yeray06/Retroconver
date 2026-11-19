package com.retroconver.ui.screens.mensajes

import com.retroconver.ui.screens.muro.ErrorPublicacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Texto de un mensaje del tablón (E4-09.3, #74). */
class ValidacionMensajeTest {

    @Test
    fun unMensajeVacioOSoloEspaciosNoSePuedePublicar() {
        assertEquals(ErrorPublicacion.VACIO, validarMensaje(""))
        assertEquals(ErrorPublicacion.VACIO, validarMensaje("   \n "))
    }

    @Test
    fun unMensajeNormalSePuedePublicar() {
        assertNull(validarMensaje("¡Hola! ¿Quedamos el sábado?"))
    }

    @Test
    fun elLimiteEsElDelServidorYSeMideSinEspaciosDeLosBordes() {
        val justo = "a".repeat(LimitesMensaje.TEXTO_MAX)
        assertNull(validarMensaje("  $justo  "))
        assertEquals(ErrorPublicacion.DEMASIADO_LARGO, validarMensaje(justo + "a"))
        assertEquals(300, LimitesMensaje.TEXTO_MAX)
    }
}
