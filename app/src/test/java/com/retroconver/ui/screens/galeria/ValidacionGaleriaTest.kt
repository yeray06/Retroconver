package com.retroconver.ui.screens.galeria

import com.retroconver.ui.screens.perfil.ErrorCampo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Nombre de un álbum nuevo (E4-10.2, #69). */
class ValidacionGaleriaTest {

    @Test
    fun unNombreNormalEsValido() {
        assertNull(validarTituloAlbum("Viaje a Londres"))
    }

    @Test
    fun elNombreEsObligatorio() {
        assertEquals(ErrorCampo.OBLIGATORIO, validarTituloAlbum(""))
        assertEquals(ErrorCampo.OBLIGATORIO, validarTituloAlbum("   "))
    }

    @Test
    fun elNombreTieneComoMucho40CaracteresSinContarLosBordes() {
        assertNull(validarTituloAlbum("a".repeat(LimitesGaleria.TITULO_MAX)))
        assertNull(validarTituloAlbum("  " + "a".repeat(LimitesGaleria.TITULO_MAX) + "  "))
        assertEquals(ErrorCampo.DEMASIADO_LARGO, validarTituloAlbum("a".repeat(LimitesGaleria.TITULO_MAX + 1)))
    }
}
