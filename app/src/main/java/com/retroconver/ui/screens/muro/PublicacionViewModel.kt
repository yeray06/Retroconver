package com.retroconver.ui.screens.muro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.RetroconverApp
import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.PublicacionRepository
import com.retroconver.navigation.RutasMuro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campo para escribir un comentario (E4-07.2, #67). */
data class FormularioComentario(
    val texto: String = "",
    /** El texto no vale: vacío o demasiado largo. */
    val error: ErrorPublicacion? = null,
    val enviando: Boolean = false,
    /** No se ha podido enviar: id del mensaje (sin conexión, sin permiso…), o `null`. */
    val errorEnvio: Int? = null,
)

/** Borrado de lo propio en el detalle (RF-29 y #67). */
data class BorradoUi(
    /** Id de la cuenta con sesión, para saber qué es mío; `null` mientras se lee. */
    val miId: String? = null,
    /** Se está borrando algo: no se puede lanzar otro borrado. */
    val borrando: Boolean = false,
    /** No se ha podido borrar: id del mensaje (sin conexión, sin permiso…), o `null`. */
    val error: Int? = null,
    /** La publicación se ha borrado: la pantalla vuelve atrás. */
    val publicacionBorrada: Boolean = false,
)

/** Lo que muestra el detalle de una publicación. */
data class PublicacionUiState(
    /** `null` mientras se descarga por primera vez o si la publicación ya no existe. */
    val publicacion: PublicacionConAutor? = null,
    /** Comentarios, el más reciente primero. */
    val comentarios: List<ComentarioConAutor> = emptyList(),
    /** Se está descargando del servidor (al abrir o al deslizar para actualizar). */
    val actualizando: Boolean = true,
    /** No se ha podido actualizar: id del mensaje, o `null`. Se sigue viendo la copia de Room. */
    val error: Int? = null,
    val formulario: FormularioComentario = FormularioComentario(),
    val borrado: BorradoUi = BorradoUi(),
)

/**
 * Detalle de una publicación con sus comentarios (E4-07.1, #66). Lee de Room y al abrirse
 * descarga del servidor, como el muro. Si la publicación se ha borrado, [PublicacionUiState.publicacion]
 * pasa a `null` y la pantalla lo dice. También comenta (E4-07.2, #67): el comentario nuevo aparece
 * solo, y sube el contador del muro, porque el repositorio lo guarda en Room.
 */
class PublicacionViewModel(
    private val repositorio: PublicacionRepository,
    private val publicacionId: String,
) : ViewModel() {

    private val actualizando = MutableStateFlow(true)
    private val error = MutableStateFlow<Int?>(null)
    private val formulario = MutableStateFlow(FormularioComentario())
    private val borrado = MutableStateFlow(BorradoUi())
    private var enCurso = false

    val estado: StateFlow<PublicacionUiState> = combine(
        combine(repositorio.publicacion(publicacionId), repositorio.comentarios(publicacionId)) { p, c -> p to c },
        combine(actualizando, error) { a, e -> a to e },
        formulario,
        borrado,
    ) { (publicacion, comentarios), (cargando, fallo), campo, borrar ->
        PublicacionUiState(publicacion, comentarios, cargando, fallo, campo, borrar)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PublicacionUiState())

    init {
        viewModelScope.launch {
            val miId = repositorio.idActual()
            borrado.update { it.copy(miId = miId) }
        }
        actualizar()
    }

    /** Descarga la publicación y sus comentarios. La pantalla se actualiza sola porque lee de Room. */
    fun actualizar() {
        if (enCurso) return
        enCurso = true
        viewModelScope.launch {
            actualizando.value = true
            error.value = null
            val resultado = repositorio.refrescarPublicacion(publicacionId)
            // Un 404 no es un fallo que avisar: la publicación se ha borrado y la pantalla lo dice
            val borrada = resultado is ResultadoRed.ErrorServidor && resultado.estadoHttp == 404
            if (resultado is ResultadoRed.Fallo && !borrada) error.value = resultado.mensaje()
            actualizando.value = false
            enCurso = false
        }
    }

    /** Borra esta publicación, que tiene que ser mía (RF-29): su fila, su imagen y sus comentarios. */
    fun borrarPublicacion() {
        val publicacion = estado.value.publicacion?.publicacion ?: return
        borrar(esPublicacion = true) { repositorio.eliminar(publicacion) }
    }

    /** Borra un comentario mío (#67). El contador del muro baja solo. */
    fun borrarComentario(comentarioId: String) = borrar(esPublicacion = false) { repositorio.eliminarComentario(comentarioId) }

    private fun borrar(esPublicacion: Boolean, accion: suspend () -> ResultadoRed<Unit>) {
        if (borrado.value.borrando) return
        borrado.update { it.copy(borrando = true, error = null) }
        viewModelScope.launch {
            val resultado = accion()
            borrado.update {
                it.copy(
                    borrando = false,
                    error = (resultado as? ResultadoRed.Fallo)?.mensaje(),
                    publicacionBorrada = esPublicacion && resultado is ResultadoRed.Correcto,
                )
            }
        }
    }

    /** Cambia el texto del comentario y quita los errores de antes. */
    fun cambiarTexto(valor: String) {
        formulario.value = FormularioComentario(texto = valor)
    }

    /** Valida el texto y, si está bien, lo envía al servidor (sin espacios en los bordes). Sin conexión no se envía nada y se conserva lo escrito. */
    fun comentar() {
        val actual = formulario.value
        if (actual.enviando) return
        val fallo = validarComentario(actual.texto)
        if (fallo != null) {
            formulario.value = actual.copy(error = fallo)
            return
        }
        formulario.value = actual.copy(error = null, errorEnvio = null, enviando = true)
        viewModelScope.launch {
            formulario.value = when (val resultado = repositorio.comentar(publicacionId, actual.texto.trim())) {
                is ResultadoRed.Correcto -> FormularioComentario()
                is ResultadoRed.Fallo -> actual.copy(errorEnvio = resultado.mensaje())
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                PublicacionViewModel(
                    repositorio = app.container.publicacionRepository,
                    publicacionId = checkNotNull(createSavedStateHandle().get<String>(RutasMuro.ARG_PUBLICACION)),
                )
            }
        }
    }
}
