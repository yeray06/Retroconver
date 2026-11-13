package com.retroconver.data.imagenes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

/**
 * Guarda en la app una copia reducida a 720p de las imágenes que se eligen (E3-05, #20).
 *
 * Las copias van a `filesDir/images`, la carpeta privada de la app, así la foto sigue
 * aunque se borre de la galería del móvil. En Room solo se guarda la URI que devuelve
 * [guardar] (`file:///…/files/images/….jpg`), que Coil carga tal cual con `AsyncImage(model = uri)`.
 *
 * Uso típico en un ViewModel, junto con [com.retroconver.ui.components.rememberSelectorDeImagen]:
 * ```
 * fun imagenElegida(origen: Uri) = viewModelScope.launch {
 *     val uri = runCatching { almacenImagenes.guardar(origen) }.getOrNull()
 *     if (uri == null) { /* mostrar error: no se ha podido leer la imagen */ }
 * }
 * ```
 * Al borrar una publicación, una foto o cambiar el avatar, hay que llamar a [borrar] con la URI vieja.
 */
class AlmacenImagenes(
    context: Context,
    nombreCarpeta: String = CARPETA,
    private val despachador: CoroutineDispatcher = Dispatchers.IO,
) {

    private val contextoApp = context.applicationContext

    /** Carpeta donde se guardan las copias. Las pruebas usan otra para no tocar las de verdad. */
    val carpeta: File = File(contextoApp.filesDir, nombreCarpeta)

    /**
     * Lee la imagen de [origen] (la URI que devuelve el Photo Picker), la gira según su
     * orientación EXIF, la reduce a 720p si es más grande y guarda la copia con un nombre único.
     * Devuelve la URI de la copia. Se ejecuta fuera del hilo principal.
     *
     * @throws IOException si la imagen no se puede leer o guardar.
     */
    suspend fun guardar(origen: Uri): String = withContext(despachador) {
        // 1. Tamaño guardado en el fichero, sin cargar la imagen en memoria
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        abrir(origen).use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) {
            throw IOException("No se puede leer la imagen $origen")
        }

        // 2. Tamaño final, teniendo en cuenta si la foto está girada
        val orientacion = abrir(origen).use { leerOrientacion(it) }
        val girada = Reescalado.intercambiaLados(orientacion)
        val destino = if (girada) {
            Reescalado.tamanoDestino(limites.outHeight, limites.outWidth)
        } else {
            Reescalado.tamanoDestino(limites.outWidth, limites.outHeight)
        }
        val destinoEnFichero = if (girada) Tamano(destino.alto, destino.ancho) else destino

        // 3. Lectura ya reducida con inSampleSize, y ajuste exacto con una matriz
        val opciones = BitmapFactory.Options().apply {
            inSampleSize = Reescalado.factorDeMuestreo(limites.outWidth, limites.outHeight, destinoEnFichero)
        }
        val leida = abrir(origen).use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: throw IOException("No se puede decodificar la imagen $origen")
        val resultado = transformar(leida, orientacion, destinoEnFichero)

        // 4. Copia en la carpeta de la app. PNG solo si tiene transparencia, para no perderla.
        val conTransparencia = resultado.hasAlpha()
        carpeta.mkdirs()
        val fichero = File(carpeta, "${UUID.randomUUID()}.${if (conTransparencia) "png" else "jpg"}")
        try {
            FileOutputStream(fichero).use { salida ->
                val formato = if (conTransparencia) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                if (!resultado.compress(formato, CALIDAD_JPEG, salida)) {
                    throw IOException("No se puede guardar la imagen")
                }
            }
        } catch (e: IOException) {
            fichero.delete()
            throw e
        } finally {
            if (resultado !== leida) leida.recycle()
            resultado.recycle()
        }
        Uri.fromFile(fichero).toString()
    }

    /**
     * Borra la copia guardada con [guardar]. Devuelve `true` si se ha borrado.
     * No hace nada con `null` ni con ficheros de fuera de [carpeta], por seguridad.
     */
    suspend fun borrar(uri: String?): Boolean = withContext(despachador) {
        val ruta = uri?.let { Uri.parse(it).path } ?: return@withContext false
        val fichero = File(ruta)
        val esNuestro = fichero.parentFile?.canonicalPath == carpeta.canonicalPath
        esNuestro && fichero.delete()
    }

    /** Borra todas las copias. Se usa al cerrar sesión, para no dejar en el móvil fotos de esa persona (RF-25). */
    suspend fun borrarTodo() = withContext(despachador) {
        carpeta.listFiles()?.forEach { it.delete() }
        Unit
    }

    private fun abrir(uri: Uri): InputStream =
        contextoApp.contentResolver.openInputStream(uri) ?: throw IOException("No se puede abrir $uri")

    private fun leerOrientacion(entrada: InputStream): Int = try {
        ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (e: IOException) {
        ExifInterface.ORIENTATION_NORMAL // PNG, GIF… sin datos EXIF: se deja como está
    }

    /**
     * Gira o voltea la imagen según su orientación EXIF y la escala a [destinoEnFichero]
     * (medido antes de girar). Si no hace falta ninguna de las dos cosas devuelve la misma imagen.
     */
    private fun transformar(imagen: Bitmap, orientacion: Int, destinoEnFichero: Tamano): Bitmap {
        val matriz = Matrix()
        when (orientacion) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matriz.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matriz.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> { matriz.setRotate(180f); matriz.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSPOSE -> { matriz.setRotate(90f); matriz.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> matriz.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matriz.setRotate(-90f); matriz.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matriz.setRotate(-90f)
        }
        // preScale: la escala se aplica antes del giro, sobre los ejes del fichero
        val escalaX = destinoEnFichero.ancho.toFloat() / imagen.width
        val escalaY = destinoEnFichero.alto.toFloat() / imagen.height
        if (escalaX != 1f || escalaY != 1f) matriz.preScale(escalaX, escalaY)
        if (matriz.isIdentity) return imagen
        return Bitmap.createBitmap(imagen, 0, 0, imagen.width, imagen.height, matriz, true)
    }

    companion object {
        /** Subcarpeta de `filesDir` con las imágenes de la app. */
        const val CARPETA = "images"

        /** Calidad JPEG: suficiente para fotos a 720p y ficheros de unos 100-200 KB. */
        private const val CALIDAD_JPEG = 85
    }
}
