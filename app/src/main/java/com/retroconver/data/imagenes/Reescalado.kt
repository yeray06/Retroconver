package com.retroconver.data.imagenes

import kotlin.math.min
import kotlin.math.roundToInt

/** Ancho y alto de una imagen en píxeles. */
data class Tamano(val ancho: Int, val alto: Int)

/**
 * Cálculos para el límite de 720p (RNF-02): ninguna imagen supera 1280×720 en horizontal
 * ni 720×1280 en vertical. No usa nada de Android, así que se prueba con pruebas unitarias normales.
 */
object Reescalado {

    /** Máximo del lado más largo. */
    const val LADO_LARGO_MAX = 1280

    /** Máximo del lado más corto. */
    const val LADO_CORTO_MAX = 720

    /**
     * Tamaño final de una imagen de [ancho]×[alto]: la reduce lo justo para que quepa en
     * 1280×720 (o 720×1280 si es vertical) sin deformarla. Si ya cabe, la deja igual:
     * las imágenes pequeñas nunca se agrandan.
     */
    fun tamanoDestino(ancho: Int, alto: Int): Tamano {
        require(ancho > 0 && alto > 0) { "Tamaño de imagen no válido: ${ancho}x$alto" }
        val largo = maxOf(ancho, alto)
        val corto = minOf(ancho, alto)
        val factor = min(LADO_LARGO_MAX.toDouble() / largo, LADO_CORTO_MAX.toDouble() / corto)
        if (factor >= 1.0) return Tamano(ancho, alto)
        val maxAncho = if (ancho >= alto) LADO_LARGO_MAX else LADO_CORTO_MAX
        val maxAlto = if (ancho >= alto) LADO_CORTO_MAX else LADO_LARGO_MAX
        return Tamano(
            ancho = (ancho * factor).roundToInt().coerceIn(1, maxAncho),
            alto = (alto * factor).roundToInt().coerceIn(1, maxAlto),
        )
    }

    /**
     * Valor de `inSampleSize` para `BitmapFactory`: la mayor potencia de 2 que reduce la imagen
     * al leerla sin que quede más pequeña que [destino]. Así no se carga en memoria la foto
     * entera de 12 MP para luego reducirla.
     */
    fun factorDeMuestreo(ancho: Int, alto: Int, destino: Tamano): Int {
        var muestra = 1
        while (ancho / (muestra * 2) >= destino.ancho && alto / (muestra * 2) >= destino.alto) {
            muestra *= 2
        }
        return muestra
    }

    /**
     * Si la orientación EXIF [orientacion] (valores 1 a 8 de `ExifInterface.TAG_ORIENTATION`)
     * gira la foto 90 o 270 grados. En ese caso el ancho y el alto que se ven son los
     * guardados en el fichero, intercambiados.
     */
    fun intercambiaLados(orientacion: Int): Boolean = orientacion in 5..8
}
