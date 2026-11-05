package com.retroconver.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Selector de imágenes común para Perfil, Muro y Galería (E3-04, #19).
 *
 * Abre el selector de fotos de Android (Photo Picker) y, si se elige una foto, llama a
 * [alElegir] con su URI. Si se cierra sin elegir, no pasa nada.
 *
 * **Permisos:** no hace falta pedir ninguno en ninguna versión de Android. El Photo Picker
 * solo da acceso a la foto que se elige: en Android 11 o superior es parte del sistema,
 * en versiones anteriores lo instala Google Play (ver el servicio declarado en
 * AndroidManifest.xml) y, si tampoco está, Android abre su selector de archivos. Por eso
 * la app no declara `READ_MEDIA_IMAGES` ni `READ_EXTERNAL_STORAGE` (RF-16).
 *
 * La URI que llega solo sirve mientras la app está abierta: hay que pasarla enseguida a
 * [com.retroconver.data.imagenes.AlmacenImagenes.guardar], que guarda una copia a 720p.
 *
 * Uso:
 * ```
 * val elegirFoto = rememberSelectorDeImagen { uri -> viewModel.imagenElegida(uri) }
 * RetroButton(texto = "Elegir foto", onClick = elegirFoto)
 * ```
 *
 * @return función que abre el selector; se llama desde el `onClick` de un botón.
 */
@Composable
fun rememberSelectorDeImagen(alElegir: (Uri) -> Unit): () -> Unit {
    // Si quien llama cambia la lambda entre recomposiciones, se usa siempre la última
    val alElegirActual by rememberUpdatedState(alElegir)
    val lanzador = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) alElegirActual(uri)
    }
    return remember(lanzador) {
        { lanzador.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
    }
}
