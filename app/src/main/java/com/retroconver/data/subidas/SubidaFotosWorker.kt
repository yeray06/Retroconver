package com.retroconver.data.subidas

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.retroconver.RetroconverApp
import com.retroconver.data.imagenes.AlmacenImagenes
import com.retroconver.data.repository.ImagenParaSubir

/**
 * Sube a un álbum una tanda de fotos elegidas en el selector (#147).
 *
 * Es un trabajo de WorkManager: sigue aunque se salga del álbum o se cierre la app, y solo
 * empieza cuando hay red. Cada foto se reduce a 720p, se sube con
 * [com.retroconver.data.repository.GaleriaRepository.subirFoto] y se borra la copia del móvil.
 * La foto aparece en el álbum en cuanto se sube, porque la pantalla lee de Room.
 *
 * Lo lanza [GestorSubidasFotos.subir]; la pantalla sigue el progreso con [GestorSubidasFotos.estado].
 */
class SubidaFotosWorker(
    contexto: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val albumId = inputData.getString(CLAVE_ALBUM) ?: return Result.failure()
        val origenes = inputData.getStringArray(CLAVE_ORIGENES)?.toList().orEmpty()
        val contenedor = (applicationContext as RetroconverApp).container
        val imagenes = contenedor.almacenImagenes

        val resumen = subirTanda(
            origenes = origenes,
            preparar = { origen -> prepararImagen(imagenes, origen) },
            subir = { imagen -> contenedor.galeriaRepository.subirFoto(albumId, imagen) },
            limpiar = { imagen -> imagen.fichero.delete() },
            alAvanzar = { actual, total -> setProgress(workDataOf(CLAVE_ACTUAL to actual, CLAVE_TOTAL to total)) },
        )
        origenes.forEach(::soltarPermiso)

        return Result.success(
            workDataOf(
                CLAVE_TOTAL to resumen.total,
                CLAVE_FALLIDAS to resumen.fallidas,
                CLAVE_MOTIVO to (resumen.motivo ?: 0),
                CLAVE_TERMINADA_EN to System.currentTimeMillis(),
            ),
        )
    }

    /** Copia a 720p en el móvil; `null` si la foto ya no se puede leer. */
    private suspend fun prepararImagen(imagenes: AlmacenImagenes, origen: String): ImagenParaSubir? {
        val copia = runCatching { imagenes.guardar(Uri.parse(origen)) }.getOrNull() ?: return null
        val medidas = imagenes.medidas(copia)
        return ImagenParaSubir(Uri.parse(copia).toFile(), ancho = medidas?.first, alto = medidas?.second)
    }

    /** El permiso para leer la foto se pidió al encolarla; ya no hace falta. */
    private fun soltarPermiso(origen: String) {
        runCatching {
            applicationContext.contentResolver.releasePersistableUriPermission(
                Uri.parse(origen),
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    companion object {
        const val CLAVE_ALBUM = "albumId"
        const val CLAVE_ORIGENES = "origenes"
        const val CLAVE_ACTUAL = "actual"
        const val CLAVE_TOTAL = "total"
        const val CLAVE_FALLIDAS = "fallidas"
        const val CLAVE_MOTIVO = "motivo"
        const val CLAVE_TERMINADA_EN = "terminadaEn"
    }
}
