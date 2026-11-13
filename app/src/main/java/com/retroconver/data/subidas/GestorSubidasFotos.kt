package com.retroconver.data.subidas

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_ACTUAL
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_ALBUM
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_FALLIDAS
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_MOTIVO
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_ORIGENES
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_TERMINADA_EN
import com.retroconver.data.subidas.SubidaFotosWorker.Companion.CLAVE_TOTAL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Lo que la pantalla de un álbum necesita saber de sus subidas. */
data class EstadoSubidas(
    /** Hay una tanda subiéndose ahora mismo. */
    val subiendo: Boolean = false,
    /** Foto que se está subiendo (desde 1) y cuántas tiene la tanda. 0 mientras empieza. */
    val actual: Int = 0,
    val total: Int = 0,
    /** Hay una tanda esperando su turno o a que vuelva la red. */
    val enCola: Boolean = false,
    /** La última tanda terminada hace poco con alguna foto sin subir, o `null`. */
    val ultimoFallo: ResumenTanda? = null,
    /** Identifica [ultimoFallo], para no volver a enseñarlo cuando se oculta. */
    val idUltimoFallo: String? = null,
)

/** Lo que interesa de cada trabajo de WorkManager, sin depender de sus clases (para poder probarlo). */
internal data class InfoTanda(
    val id: String,
    val enMarcha: Boolean,
    val enCola: Boolean,
    val terminada: Boolean,
    val actual: Int = 0,
    val total: Int = 0,
    val fallidas: Int = 0,
    val motivo: Int = 0,
    val terminadaEn: Long = 0,
)

/**
 * Lanza y sigue las subidas de fotos en segundo plano (#147). Cada álbum tiene su cola: si se
 * eligen más fotos mientras se sube una tanda, la nueva espera su turno.
 */
class GestorSubidasFotos(private val contexto: Context) {

    private val workManager: WorkManager get() = WorkManager.getInstance(contexto)

    /**
     * Encola una tanda de fotos para [albumId]. Se pide permiso permanente para leer cada foto,
     * así se puede subir aunque se cierre la app antes de terminar.
     */
    fun subir(albumId: String, origenes: List<Uri>) {
        if (origenes.isEmpty()) return
        origenes.forEach { origen ->
            runCatching {
                contexto.contentResolver.takePersistableUriPermission(origen, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        val peticion = OneTimeWorkRequestBuilder<SubidaFotosWorker>()
            .setInputData(
                workDataOf(
                    CLAVE_ALBUM to albumId,
                    CLAVE_ORIGENES to origenes.map(Uri::toString).toTypedArray(),
                ),
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(ETIQUETA)
            .build()
        workManager.enqueueUniqueWork(nombreCola(albumId), ExistingWorkPolicy.APPEND_OR_REPLACE, peticion)
    }

    /** Estado de las subidas de [albumId]. Emite cada vez que avanza una foto o termina una tanda. */
    fun estado(albumId: String): Flow<EstadoSubidas> =
        workManager.getWorkInfosForUniqueWorkFlow(nombreCola(albumId)).map { trabajos ->
            resumirSubidas(trabajos.map { it.aInfoTanda() }, ahora = System.currentTimeMillis())
        }

    /** Al cerrar sesión no tiene sentido seguir subiendo fotos de esa cuenta. */
    fun cancelarTodo() {
        workManager.cancelAllWorkByTag(ETIQUETA)
    }

    private fun WorkInfo.aInfoTanda() = InfoTanda(
        id = id.toString(),
        enMarcha = state == WorkInfo.State.RUNNING,
        enCola = state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.BLOCKED,
        terminada = state == WorkInfo.State.SUCCEEDED,
        actual = progress.getInt(CLAVE_ACTUAL, 0),
        total = progress.getInt(CLAVE_TOTAL, 0).takeIf { it > 0 } ?: outputData.getInt(CLAVE_TOTAL, 0),
        fallidas = outputData.getInt(CLAVE_FALLIDAS, 0),
        motivo = outputData.getInt(CLAVE_MOTIVO, 0),
        terminadaEn = outputData.getLong(CLAVE_TERMINADA_EN, 0),
    )

    companion object {
        private const val ETIQUETA = "subida-fotos"

        private fun nombreCola(albumId: String) = "subida-fotos-$albumId"
    }
}

/** Un fallo se enseña durante este tiempo; después ya no viene a cuento (WorkManager guarda los trabajos un día). */
internal const val VIGENCIA_FALLO_MS = 10 * 60 * 1000L

/** Junta los trabajos de la cola de un álbum en lo que enseña la pantalla. */
internal fun resumirSubidas(tandas: List<InfoTanda>, ahora: Long): EstadoSubidas {
    val enMarcha = tandas.firstOrNull { it.enMarcha }
    val fallo = tandas
        .filter { it.terminada && it.fallidas > 0 && ahora - it.terminadaEn < VIGENCIA_FALLO_MS }
        .maxByOrNull { it.terminadaEn }
    return EstadoSubidas(
        subiendo = enMarcha != null,
        actual = enMarcha?.actual ?: 0,
        total = enMarcha?.total ?: 0,
        enCola = tandas.any { it.enCola },
        ultimoFallo = fallo?.let { ResumenTanda(it.total, it.fallidas, it.motivo.takeIf { motivo -> motivo != 0 }) },
        idUltimoFallo = fallo?.id,
    )
}
