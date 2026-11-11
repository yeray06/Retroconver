package com.retroconver.ui.screens.acceso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validación de los formularios de inicio de sesión y registro (E4-04.2, #118). */
class ValidacionAccesoTest {

    @Test
    fun emailsValidos() {
        listOf("yeray@retroconver.es", "dani.nc@gmail.com", "a+b@sub.dominio.org", "  con@espacios.es  ")
            .forEach { assertTrue(it, esEmailValido(it)) }
    }

    @Test
    fun emailsNoValidos() {
        listOf("", "yeray", "yeray@", "@retroconver.es", "yeray@retroconver", "yeray@retroconver.e", "ye ray@correo.es", "a@b@c.es")
            .forEach { assertFalse(it, esEmailValido(it)) }
    }

    @Test
    fun inicioDeSesionCorrecto() {
        assertFalse(validarInicioSesion("yeray@retroconver.es", "x").hayErrores)
    }

    @Test
    fun alEntrarElEmailYLaContrasenaSonObligatorios() {
        val errores = validarInicioSesion("   ", "")
        assertEquals(ErrorAcceso.OBLIGATORIO, errores.email)
        assertEquals(ErrorAcceso.OBLIGATORIO, errores.contrasena)
        assertNull(errores.nombre)
    }

    @Test
    fun alEntrarSeCompruebaElFormatoDelEmail() {
        assertEquals(ErrorAcceso.EMAIL_NO_VALIDO, validarInicioSesion("yeray@correo", "secreta").email)
    }

    @Test
    fun registroCorrecto() {
        assertFalse(validarRegistro("Yeray", "yeray@retroconver.es", "123456").hayErrores)
    }

    @Test
    fun alRegistrarseElNombreEsObligatorioYTieneLimite() {
        assertEquals(ErrorAcceso.OBLIGATORIO, validarRegistro("  ", "yeray@retroconver.es", "123456").nombre)
        assertEquals(
            ErrorAcceso.DEMASIADO_LARGO,
            validarRegistro("a".repeat(LimitesAcceso.NOMBRE_MAX + 1), "yeray@retroconver.es", "123456").nombre,
        )
        assertNull(validarRegistro("a".repeat(LimitesAcceso.NOMBRE_MAX), "yeray@retroconver.es", "123456").nombre)
    }

    @Test
    fun alRegistrarseLaContrasenaTieneAlMenosSeisCaracteres() {
        assertEquals(ErrorAcceso.OBLIGATORIO, validarRegistro("Yeray", "yeray@retroconver.es", "").contrasena)
        assertEquals(ErrorAcceso.CONTRASENA_CORTA, validarRegistro("Yeray", "yeray@retroconver.es", "12345").contrasena)
        assertNull(validarRegistro("Yeray", "yeray@retroconver.es", "123456").contrasena)
    }

    @Test
    fun losEspaciosDeLaContrasenaCuentan() {
        // No se recorta: «  abc » tiene 6 caracteres, igual que para el servidor
        assertNull(validarRegistro("Yeray", "yeray@retroconver.es", "  abc ").contrasena)
    }

    @Test
    fun alRegistrarseSeMarcanTodosLosErroresALaVez() {
        val errores = validarRegistro("", "no-es-un-email", "123")
        assertEquals(ErrorAcceso.OBLIGATORIO, errores.nombre)
        assertEquals(ErrorAcceso.EMAIL_NO_VALIDO, errores.email)
        assertEquals(ErrorAcceso.CONTRASENA_CORTA, errores.contrasena)
        assertTrue(errores.hayErrores)
    }
}
