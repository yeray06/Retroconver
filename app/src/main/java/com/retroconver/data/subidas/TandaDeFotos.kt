package com.retroconver.data.subidas

import androidx.annotation.StringRes
import com.retroconver.R
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje

/** Cómo ha ido una tanda de fotos. */
data class ResumenTanda(
    val total: Int,
    val fallidas: Int,
    /** Texto del motivo del último fallo (`strings_*.xml`), o `null` si no ha fallado ninguna. */
    @StringRes val motivo: Int?,
)

/**
 * Sube una tanda de fotos de una en una (#147). Es la lógica del [SubidaFotosWorker], aparte
 * para poder probarla sin Android.
 *
 * Por cada foto: [preparar] (reducirla a 720p; `null` si no se puede leer), [subir] y
 * [limpiar] la copia del móvil, haya ido bien o mal. Si una falla, se sigue con las demás.
 * Si la sesión ha caducado, ya no tiene sentido seguir: las que faltan cuentan como fallidas.
 *
 * @param alAvanzar Se llama antes de cada foto con su número (desde 1) y el total.
 */
suspend fun <T> subirTanda(
    origenes: List<String>,
    preparar: suspend (String) -> T?,
    subir: suspend (T) -> ResultadoRed<Unit>,
    limpiar: suspend (T) -> Unit,
    alAvanzar: suspend (actual: Int, total: Int) -> Unit = { _, _ -> },
): ResumenTanda {
    var fallidas = 0
    var motivo: Int? = null
    for ((indice, origen) in origenes.withIndex()) {
        alAvanzar(indice + 1, origenes.size)
        val imagen = preparar(origen)
        if (imagen == null) {
            fallidas++
            motivo = R.string.galeria_error_foto_no_valida
            continue
        }
        val resultado = try {
            subir(imagen)
        } finally {
            limpiar(imagen)
        }
        if (resultado is ResultadoRed.Fallo) {
            fallidas++
            motivo = resultado.mensaje()
            if (resultado == ResultadoRed.SesionCaducada) {
                fallidas += origenes.size - indice - 1
                break
            }
        }
    }
    return ResumenTanda(total = origenes.size, fallidas = fallidas, motivo = motivo)
}
