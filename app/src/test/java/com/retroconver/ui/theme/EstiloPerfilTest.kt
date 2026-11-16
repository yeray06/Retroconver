package com.retroconver.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/** Temas clásicos y tipografías del perfil (EX-03, #30). */
class EstiloPerfilTest {

    @Test
    fun hayAlMenosTresTemasClasicosYVariasTipografias() {
        assertTrue(TemaPerfil.entries.size >= 3)
        assertTrue(TipografiaPerfil.entries.size >= 2)
    }

    @Test
    fun cadaClaveEsUnicaYSeRecupera() {
        assertEquals(TemaPerfil.entries.size, TemaPerfil.entries.map { it.clave }.toSet().size)
        assertEquals(TipografiaPerfil.entries.size, TipografiaPerfil.entries.map { it.clave }.toSet().size)
        TemaPerfil.entries.forEach { assertEquals(it, TemaPerfil.deClave(it.clave)) }
        TipografiaPerfil.entries.forEach { assertEquals(it, TipografiaPerfil.deClave(it.clave)) }
    }

    @Test
    fun sinElegirNadaOConUnaClaveDesconocidaSeUsaLoDePorDefecto() {
        assertEquals(TemaPerfil.CLASICO, TemaPerfil.deClave(null))
        assertEquals(TemaPerfil.CLASICO, TemaPerfil.deClave("tema-que-ya-no-existe"))
        assertEquals(TipografiaPerfil.NORMAL, TipografiaPerfil.deClave(null))
        assertEquals(TipografiaPerfil.NORMAL, TipografiaPerfil.deClave("comic-sans"))
    }

    @Test
    fun elTemaClasicoEsElDeLaGuiaDeEstilo() {
        val clasico = TemaPerfil.CLASICO.colores
        assertEquals(RetroBackground, clasico.fondo)
        assertEquals(RetroCardBackground, clasico.tarjeta)
        assertEquals(RetroLinkBlue, clasico.nombre)
    }

    /** WCAG 2.1: el texto normal necesita un contraste de 4,5:1 sobre su fondo (RNF-03). */
    @Test
    fun todosLosTextosTienenContrasteSuficienteEnCadaTema() {
        TemaPerfil.entries.forEach { tema ->
            val c = tema.colores
            listOf(c.titulo, c.nombre, c.texto, c.textoSecundario, c.error).forEach { texto ->
                listOf(c.tarjeta, c.destacado).forEach { fondo ->
                    val ratio = contraste(texto, fondo)
                    assertTrue("${tema.name}: contraste ${"%.2f".format(ratio)} por debajo de 4,5", ratio >= 4.5)
                }
            }
        }
    }

    private fun contraste(a: Color, b: Color): Double {
        val la = luminancia(a)
        val lb = luminancia(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun luminancia(color: Color): Double {
        fun canal(valor: Float): Double {
            val c = valor.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * canal(color.red) + 0.7152 * canal(color.green) + 0.0722 * canal(color.blue)
    }
}
