package com.retroconver.data.imagenes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Reescalado y guardado con fotos reales en el emulador (E3-05, #20, #62 y #63).
 * Las fotos de prueba se crean en la caché: una de 12 MP como la de una cámara de móvil.
 */
@RunWith(AndroidJUnit4::class)
class AlmacenImagenesTest {

    private lateinit var contexto: Context
    private lateinit var almacen: AlmacenImagenes
    private val creados = mutableListOf<File>()

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        // Carpeta aparte: así las pruebas no borran las imágenes reales de la app
        almacen = AlmacenImagenes(contexto, nombreCarpeta = "images-pruebas")
    }

    @After
    fun limpiar() {
        creados.forEach { it.delete() }
        almacen.carpeta.deleteRecursively()
    }

    @Test
    fun unaFotoDe12MpSeGuardaA720pEnLaCarpetaDeLaApp() = runBlocking {
        val uri = almacen.guardar(fotoDePrueba(4000, 3000))

        val fichero = File(Uri.parse(uri).path!!)
        assertTrue(uri.startsWith("file://"))
        assertEquals(almacen.carpeta.canonicalPath, fichero.parentFile!!.canonicalPath)
        assertEquals(960 to 720, tamanoDe(fichero))
    }

    @Test
    fun unaFotoPequenaNoSeAgranda() = runBlocking {
        val uri = almacen.guardar(fotoDePrueba(640, 480))

        assertEquals(640 to 480, tamanoDe(File(Uri.parse(uri).path!!)))
    }

    @Test
    fun unaFotoGiradaPorExifSeGuardaDerecha() = runBlocking {
        // El sensor guarda 4000×3000, pero la etiqueta EXIF dice que se hizo en vertical
        val uri = almacen.guardar(fotoDePrueba(4000, 3000, orientacion = ExifInterface.ORIENTATION_ROTATE_90))

        assertEquals(720 to 960, tamanoDe(File(Uri.parse(uri).path!!)))
    }

    @Test
    fun cadaImagenTieneUnNombreDistinto() = runBlocking {
        val origen = fotoDePrueba(100, 100)

        assertNotEquals(almacen.guardar(origen), almacen.guardar(origen))
    }

    @Test
    fun borrarQuitaElFicheroYNoTocaLoDeFuera() = runBlocking {
        val uri = almacen.guardar(fotoDePrueba(100, 100))
        val ajeno = fotoDePrueba(10, 10)

        assertTrue(almacen.borrar(uri))
        assertFalse(File(Uri.parse(uri).path!!).exists())
        assertFalse(almacen.borrar(ajeno.toString()))
        assertTrue(File(ajeno.path!!).exists())
        assertFalse(almacen.borrar(null))
    }

    /** Crea un JPEG de [ancho]×[alto] en la caché, con la orientación EXIF indicada. */
    private fun fotoDePrueba(ancho: Int, alto: Int, orientacion: Int = ExifInterface.ORIENTATION_NORMAL): Uri {
        val fichero = File.createTempFile("prueba", ".jpg", contexto.cacheDir)
        creados += fichero
        val imagen = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)
        imagen.eraseColor(Color.rgb(43, 130, 223))
        fichero.outputStream().use { imagen.compress(Bitmap.CompressFormat.JPEG, 80, it) }
        imagen.recycle()
        if (orientacion != ExifInterface.ORIENTATION_NORMAL) {
            ExifInterface(fichero.absolutePath).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientacion.toString())
                saveAttributes()
            }
        }
        return Uri.fromFile(fichero)
    }

    private fun tamanoDe(fichero: File): Pair<Int, Int> {
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(fichero.absolutePath, opciones)
        return opciones.outWidth to opciones.outHeight
    }
}
