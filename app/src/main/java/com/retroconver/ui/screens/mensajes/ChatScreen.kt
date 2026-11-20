package com.retroconver.ui.screens.mensajes

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.local.MensajeDirectoConPersonas
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.MensajesDirectosRepository
import com.retroconver.data.repository.UsuarioRepository
import com.retroconver.navigation.RutasMensajes
import com.retroconver.ui.screens.galeria.CabeceraGaleria
import com.retroconver.ui.screens.muro.ErrorPublicacion
import com.retroconver.ui.screens.muro.textoFecha
import com.retroconver.ui.theme.RetroCardBackground
import com.retroconver.ui.theme.RetroCardBorder
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroProfileCardBg
import com.retroconver.ui.theme.RetroProfileCardBorder
import com.retroconver.ui.theme.RetroTextDark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.roundToInt

/** Lo que enseña el chat con otra persona (EX-01, #28). */
data class ChatUiState(
    /** La otra persona, o `null` mientras no está en Room. */
    val persona: UsuarioEntity? = null,
    val miId: String? = null,
    /** Mensajes del más antiguo al más reciente. */
    val mensajes: List<MensajeDirectoConPersonas> = emptyList(),
    val enviando: Boolean = false,
    /** No se ha podido enviar o descargar: id del mensaje de error, o `null`. */
    val error: Int? = null,
)

/**
 * Chat privado con otra persona (EX-01, #28), como en MSN: los mensajes propios a la derecha y
 * los suyos a la izquierda. Mientras está abierto se descarga cada [INTERVALO_MS] ms. Con «Zumbido»
 * la conversación tiembla y el móvil vibra, en el de quien lo envía y en el de quien lo recibe.
 */
@Composable
fun ChatScreen(
    alVolver: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val temblor = remember { Animatable(0f) }
    var avisoZumbido by remember { mutableStateOf<String?>(null) }

    // Sin tiempo real: mientras la pantalla está delante se pregunta al servidor cada pocos segundos
    val ciclo = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(ciclo) {
        ciclo.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.descargar()
                delay(INTERVALO_MS)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.zumbidos.collect { nombre ->
            avisoZumbido = nombre
            vibrar(contexto)
            repeat(VECES_TEMBLOR) { i -> temblor.animateTo(if (i % 2 == 0) AMPLITUD else -AMPLITUD, tween(DURACION_SACUDIDA)) }
            temblor.animateTo(0f, tween(DURACION_SACUDIDA))
        }
    }

    var texto by rememberSaveable { mutableStateOf("") }
    ChatContenido(
        estado = estado,
        texto = texto,
        alCambiarTexto = { texto = it },
        alEnviar = { viewModel.enviar(texto) { texto = "" } },
        alZumbido = viewModel::zumbido,
        alVolver = alVolver,
        avisoZumbido = avisoZumbido,
        modifier = Modifier.offset { IntOffset(temblor.value.roundToInt(), 0) },
    )
}

/** Lo que dibuja [ChatScreen]. */
@Composable
internal fun ChatContenido(
    estado: ChatUiState,
    texto: String,
    alCambiarTexto: (String) -> Unit,
    alEnviar: () -> Unit,
    alZumbido: () -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
    avisoZumbido: String? = null,
) {
    val lista = rememberLazyListState()
    // Al llegar o enviar un mensaje, se baja hasta él
    LaunchedEffect(estado.mensajes.size) {
        if (estado.mensajes.isNotEmpty()) lista.animateScrollToItem(estado.mensajes.lastIndex)
    }
    val errorTexto = validarMensaje(texto).takeIf { texto.isNotEmpty() }
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGaleria(
            titulo = estado.persona?.let { stringResource(R.string.directos_chat_con, it.nombre) }
                ?: stringResource(R.string.directos_chat),
            alVolver = alVolver,
        )
        avisoZumbido?.let { nombre ->
            Text(
                text = stringResource(R.string.directos_zumbido_recibido, nombre),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RetroGlossyDark,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        estado.error?.let { error ->
            Text(
                text = stringResource(error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        LazyColumn(
            state = lista,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (estado.mensajes.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.directos_conversacion_vacia),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RetroGrayText,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
            items(estado.mensajes, key = { it.mensaje.id }) { item ->
                Burbuja(item = item, esMio = item.mensaje.remitenteId == estado.miId)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = alCambiarTexto,
                label = { Text(stringResource(R.string.directos_escribir)) },
                isError = errorTexto == ErrorPublicacion.DEMASIADO_LARGO,
                supportingText = {
                    Text(
                        if (errorTexto == ErrorPublicacion.DEMASIADO_LARGO) {
                            stringResource(R.string.tablon_demasiado_largo, LimitesMensaje.TEXTO_MAX)
                        } else {
                            stringResource(R.string.tablon_contador, texto.trim().length, LimitesMensaje.TEXTO_MAX)
                        },
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = alZumbido, enabled = !estado.enviando) {
                    Text(stringResource(R.string.directos_zumbido_boton))
                }
                Button(onClick = alEnviar, enabled = texto.isNotBlank() && errorTexto == null && !estado.enviando) {
                    Text(stringResource(if (estado.enviando) R.string.directos_enviando else R.string.directos_enviar))
                }
            }
        }
    }
}

/** Un mensaje: a la derecha y en azul si es mío; a la izquierda y en blanco si es suyo. Los zumbidos, en negrita. */
@Composable
private fun Burbuja(item: MensajeDirectoConPersonas, esMio: Boolean) {
    val mensaje = item.mensaje
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (esMio) RetroProfileCardBg else RetroCardBackground,
            border = BorderStroke(1.dp, if (esMio) RetroProfileCardBorder else RetroCardBorder),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .semantics(mergeDescendants = true) {},
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = item.remitente.nombre,
                    style = MaterialTheme.typography.labelMedium,
                    color = RetroGlossyDark,
                )
                Text(
                    text = if (mensaje.zumbido) stringResource(R.string.directos_zumbido) else mensaje.texto,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (mensaje.zumbido) FontWeight.Bold else null,
                    color = RetroTextDark,
                )
                Text(
                    text = textoFecha(mensaje.fechaCreacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroGrayText,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Una vibración corta, si el móvil puede. No es grave si no. */
private fun vibrar(contexto: Context) {
    try {
        contexto.getSystemService(Vibrator::class.java)
            ?.vibrate(VibrationEffect.createOneShot(DURACION_VIBRACION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    } catch (_: SecurityException) {
        // Sin permiso para vibrar: basta con el temblor de la pantalla
    }
}

/** Cada cuánto se descarga la conversación abierta. */
private const val INTERVALO_MS = 4_000L
private const val VECES_TEMBLOR = 8
private const val AMPLITUD = 14f
private const val DURACION_SACUDIDA = 40
private const val DURACION_VIBRACION_MS = 400L

/** Estado del chat con una persona (EX-01, #28). Lee de Room, así que lo nuevo aparece solo. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val otroId: String,
    private val directos: MensajesDirectosRepository,
    usuarios: UsuarioRepository,
    /** Desde cuándo está abierto el chat: solo tiemblan los zumbidos que lleguen después. */
    private val abiertoDesde: Instant = Instant.now(),
) : ViewModel() {

    private val miId = MutableStateFlow<String?>(null)
    private val enviando = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)
    private val zumbidosVistos = mutableSetOf<String>()
    private val _zumbidos = MutableSharedFlow<String>(extraBufferCapacity = 1)

    /** Nombre de quien manda cada zumbido nuevo: la pantalla tiembla y vibra. */
    val zumbidos: SharedFlow<String> = _zumbidos

    private val mensajes = miId.filterNotNull().flatMapLatest { yo -> directos.conversacion(yo, otroId) }

    val estado: StateFlow<ChatUiState> =
        combine(usuarios.usuario(otroId), miId, mensajes, enviando, error) { persona, yo, lista, publicando, fallo ->
            ChatUiState(persona = persona, miId = yo, mensajes = lista, enviando = publicando, error = fallo)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    init {
        viewModelScope.launch { miId.value = directos.idActual() }
        viewModelScope.launch {
            mensajes.collect { lista ->
                val nuevos = lista.filter { it.mensaje.zumbido && it.mensaje.id !in zumbidosVistos }
                zumbidosVistos += nuevos.map { it.mensaje.id }
                // Los de antes de abrir el chat ya no tiemblan
                nuevos.lastOrNull { !it.mensaje.fechaCreacion.isBefore(abiertoDesde.minusSeconds(MARGEN_S)) }
                    ?.let { _zumbidos.tryEmit(it.remitente.nombre) }
            }
        }
    }

    /** Descarga los mensajes del servidor. Sin conexión se sigue viendo la copia (ya avisa la franja amarilla). */
    suspend fun descargar() {
        val resultado = directos.refrescar()
        if (resultado is ResultadoRed.Fallo && resultado != ResultadoRed.SinConexion && !enviando.value) {
            error.value = resultado.mensaje()
        }
    }

    /** Envía [texto] y, si va bien, llama a [alTerminar] (para vaciar el campo). */
    fun enviar(texto: String, alTerminar: () -> Unit) {
        if (validarMensaje(texto) != null) return
        mandar(texto, zumbido = false, alTerminar = alTerminar)
    }

    /** Envía un zumbido: la conversación tiembla en los dos móviles. */
    fun zumbido() = mandar(TEXTO_ZUMBIDO, zumbido = true, alTerminar = {})

    private fun mandar(texto: String, zumbido: Boolean, alTerminar: () -> Unit) {
        if (enviando.value) return
        viewModelScope.launch {
            enviando.value = true
            error.value = null
            when (val resultado = directos.enviar(otroId, texto, zumbido)) {
                is ResultadoRed.Correcto -> alTerminar()
                is ResultadoRed.Fallo -> error.value = mensajeDeError(resultado)
            }
            enviando.value = false
        }
    }

    companion object {
        /** Texto que se guarda en el servidor para un zumbido (la app enseña «¡Zumbido!»). */
        const val TEXTO_ZUMBIDO = "¡Zumbido!"

        /** Margen por si el reloj del móvil y el del servidor no coinciden. */
        private const val MARGEN_S = 30L

        /** Sin permiso es que no sois amigos: se explica en vez del error general. */
        fun mensajeDeError(fallo: ResultadoRed.Fallo): Int {
            val sinPermiso = fallo is ResultadoRed.ErrorServidor &&
                (fallo.codigo == MensajesDirectosRepository.CODIGO_SIN_PERMISO || fallo.estadoHttp == 403)
            return if (sinPermiso) R.string.directos_solo_amigos else fallo.mensaje()
        }

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                ChatViewModel(
                    otroId = checkNotNull(createSavedStateHandle().get<String>(RutasMensajes.ARG_PERSONA)),
                    directos = app.container.mensajesDirectosRepository,
                    usuarios = app.container.usuarioRepository,
                )
            }
        }
    }
}
