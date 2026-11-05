package com.retroconver.ui.screens.perfil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validación del formulario de perfil (E3-06.2, #54). */
class ValidacionPerfilTest {

    @Test
    fun unPerfilCorrectoNoTieneErrores() {
        assertFalse(validarPerfil("Yeray", "Fan de los 2000", "Estudiando").hayErrores)
    }

    @Test
    fun laBiografiaYElEstadoPuedenQuedarVacios() {
        assertFalse(validarPerfil("Yeray", "", "").hayErrores)
    }

    @Test
    fun elNombreEsObligatorio() {
        assertEquals(ErrorCampo.OBLIGATORIO, validarPerfil("", "", "").nombre)
        assertEquals(ErrorCampo.OBLIGATORIO, validarPerfil("   ", "", "").nombre)
    }

    @Test
    fun cadaCampoTieneSuLimite() {
        val errores = validarPerfil(
            nombre = "a".repeat(LimitesPerfil.NOMBRE_MAX + 1),
            biografia = "b".repeat(LimitesPerfil.BIOGRAFIA_MAX + 1),
            estado = "c".repeat(LimitesPerfil.ESTADO_MAX + 1),
        )
        assertEquals(ErrorCampo.DEMASIADO_LARGO, errores.nombre)
        assertEquals(ErrorCampo.DEMASIADO_LARGO, errores.biografia)
        assertEquals(ErrorCampo.DEMASIADO_LARGO, errores.estado)
        assertTrue(errores.hayErrores)
    }

    @Test
    fun justoEnElLimiteEsValido() {
        val errores = validarPerfil(
            nombre = "a".repeat(LimitesPerfil.NOMBRE_MAX),
            biografia = "b".repeat(LimitesPerfil.BIOGRAFIA_MAX),
            estado = "c".repeat(LimitesPerfil.ESTADO_MAX),
        )
        assertFalse(errores.hayErrores)
    }

    @Test
    fun losEspaciosDeLosBordesNoCuentan() {
        assertNull(validarPerfil("  " + "a".repeat(LimitesPerfil.NOMBRE_MAX) + "  ", "", "").nombre)
    }
}
