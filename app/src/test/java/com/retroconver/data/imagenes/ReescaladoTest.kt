package com.retroconver.data.imagenes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Límite de 720p (E3-05.1, #62). */
class ReescaladoTest {

    @Test
    fun unaFotoHorizontalDe12MpQuedaEn960x720() {
        assertEquals(Tamano(960, 720), Reescalado.tamanoDestino(4000, 3000))
    }

    @Test
    fun unaFotoVerticalQuedaDentroDe720x1280() {
        assertEquals(Tamano(720, 960), Reescalado.tamanoDestino(3000, 4000))
    }

    @Test
    fun unaFoto16a9QuedaEnExactamente1280x720() {
        assertEquals(Tamano(1280, 720), Reescalado.tamanoDestino(1920, 1080))
        assertEquals(Tamano(720, 1280), Reescalado.tamanoDestino(1080, 1920))
    }

    @Test
    fun unaPanoramicaSeLimitaPorElLadoLargo() {
        assertEquals(Tamano(1280, 320), Reescalado.tamanoDestino(4000, 1000))
    }

    @Test
    fun unaFotoCuadradaSeLimitaA720() {
        assertEquals(Tamano(720, 720), Reescalado.tamanoDestino(1000, 1000))
    }

    @Test
    fun lasImagenesPequenasNoSeAgrandan() {
        assertEquals(Tamano(800, 600), Reescalado.tamanoDestino(800, 600))
        assertEquals(Tamano(1280, 720), Reescalado.tamanoDestino(1280, 720))
        assertEquals(Tamano(1, 1), Reescalado.tamanoDestino(1, 1))
    }

    @Test
    fun ningunTamanoSePasaDelLimiteNiSeDeforma() {
        for (ancho in listOf(721, 1000, 1281, 2448, 3264, 4032, 6000, 9999)) {
            for (alto in listOf(721, 999, 1281, 1836, 2448, 3024, 4000, 7777)) {
                val destino = Reescalado.tamanoDestino(ancho, alto)
                assertTrue(maxOf(destino.ancho, destino.alto) <= Reescalado.LADO_LARGO_MAX)
                assertTrue(minOf(destino.ancho, destino.alto) <= Reescalado.LADO_CORTO_MAX)
                val proporcionOriginal = ancho.toDouble() / alto
                val proporcionFinal = destino.ancho.toDouble() / destino.alto
                assertEquals(proporcionOriginal, proporcionFinal, proporcionOriginal * 0.01)
            }
        }
    }

    @Test
    fun elMuestreoReduceSinQuedarseCortoDelDestino() {
        assertEquals(4, Reescalado.factorDeMuestreo(4000, 3000, Tamano(960, 720)))
        assertEquals(1, Reescalado.factorDeMuestreo(800, 600, Tamano(800, 600)))
        assertEquals(1, Reescalado.factorDeMuestreo(1920, 1080, Tamano(1280, 720)))
    }

    @Test
    fun soloLasOrientacionesDe90Y270IntercambianLados() {
        // 1 normal, 2 volteada, 3 girada 180, 4 volteada vertical
        for (orientacion in 0..4) assertFalse(Reescalado.intercambiaLados(orientacion))
        // 5 transpuesta, 6 girada 90, 7 transversa, 8 girada 270
        for (orientacion in 5..8) assertTrue(Reescalado.intercambiaLados(orientacion))
    }
}
