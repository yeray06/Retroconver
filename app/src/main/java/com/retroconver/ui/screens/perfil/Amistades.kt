package com.retroconver.ui.screens.perfil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retroconver.R
import com.retroconver.RetroconverApp
import com.retroconver.data.local.SolicitudConPersona
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.mensaje
import com.retroconver.data.repository.AmistadesRepository
import com.retroconver.data.repository.RelacionAmistad
import com.retroconver.ui.components.Avatar
import com.retroconver.ui.components.RetroButton
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.theme.LocalColoresPerfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * Amistades en el perfil (EX-04, #129): la lista de amigos, las solicitudes y el botón para
 * pedir amistad. Usan los colores del tema del perfil en el que se dibujan (EX-03).
 */

/**
 * Lista de amigos de un perfil. Con [alAbrirPerfil], cada amigo se puede pulsar para ir a su
 * perfil (cuando exista la pantalla de perfiles ajenos, E4-08.1). [vacio] es el texto sin amigos.
 */
@Composable
fun TarjetaAmigos(
    amigos: List<UsuarioEntity>,
    vacio: String,
    modifier: Modifier = Modifier,
    alAbrirPerfil: ((String) -> Unit)? = null,
) {
    val colores = LocalColoresPerfil.current
    RetroCard(modifier = modifier.fillMaxWidth(), color = colores.tarjeta, borde = colores.bordeTarjeta) {
        Text(
            text = stringResource(R.string.amistades_amigos, amigos.size),
            style = MaterialTheme.typography.titleMedium,
            color = colores.titulo,
            modifier = Modifier.semantics { heading() },
        )
        if (amigos.isEmpty()) {
            Text(
                text = vacio,
                style = MaterialTheme.typography.bodyMedium,
                color = colores.textoSecundario,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        val verPerfil = stringResource(R.string.amistades_ver_perfil)
        amigos.forEach { amigo ->
            val pulsable = if (alAbrirPerfil != null) {
                Modifier.clickable(onClickLabel = verPerfil) { alAbrirPerfil(amigo.id) }
            } else {
                Modifier
            }
            FilaPersona(persona = amigo, modifier = pulsable)
        }
    }
}

/**
 * Solicitudes pendientes de la cuenta de la sesión: las recibidas, con «Aceptar» y «Rechazar»,
 * y las enviadas, con «Cancelar». No se dibuja si no hay ninguna.
 * [enCurso] es la solicitud que se está contestando: sus botones se desactivan.
 */
@Composable
fun TarjetaSolicitudes(
    recibidas: List<SolicitudConPersona>,
    enviadas: List<SolicitudConPersona>,
    enCurso: String?,
    error: Int?,
    alAceptar: (String) -> Unit,
    alBorrar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recibidas.isEmpty() && enviadas.isEmpty() && error == null) return
    val colores = LocalColoresPerfil.current
    RetroCard(modifier = modifier.fillMaxWidth(), color = colores.tarjeta, borde = colores.bordeTarjeta) {
        if (recibidas.isNotEmpty()) {
            TituloSolicitudes(stringResource(R.string.amistades_recibidas))
            recibidas.forEach { solicitud ->
                val nombre = solicitud.persona.nombre
                FilaPersona(persona = solicitud.persona, detalle = stringResource(R.string.amistades_te_ha_pedido))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 56.dp, bottom = 4.dp),
                ) {
                    val ocupada = enCurso == solicitud.idAmistad
                    val descripcionAceptar = stringResource(R.string.amistades_aceptar_de, nombre)
                    Button(
                        onClick = { alAceptar(solicitud.idAmistad) },
                        enabled = !ocupada,
                        modifier = Modifier.semantics { contentDescription = descripcionAceptar },
                    ) {
                        Text(stringResource(if (ocupada) R.string.amistades_un_momento else R.string.amistades_aceptar))
                    }
                    val descripcionRechazar = stringResource(R.string.amistades_rechazar_de, nombre)
                    RetroButton(
                        texto = stringResource(R.string.amistades_rechazar),
                        onClick = { if (!ocupada) alBorrar(solicitud.idAmistad) },
                        modifier = Modifier.semantics { contentDescription = descripcionRechazar },
                    )
                }
            }
        }
        if (enviadas.isNotEmpty()) {
            TituloSolicitudes(stringResource(R.string.amistades_enviadas))
            enviadas.forEach { solicitud ->
                val ocupada = enCurso == solicitud.idAmistad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilaPersona(
                        persona = solicitud.persona,
                        detalle = stringResource(R.string.amistades_esperando),
                        modifier = Modifier.weight(1f),
                    )
                    val descripcionCancelar = stringResource(R.string.amistades_cancelar_a, solicitud.persona.nombre)
                    RetroButton(
                        texto = stringResource(if (ocupada) R.string.amistades_un_momento else R.string.amistades_cancelar),
                        onClick = { if (!ocupada) alBorrar(solicitud.idAmistad) },
                        modifier = Modifier.semantics { contentDescription = descripcionCancelar },
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = stringResource(error),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.error,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun TituloSolicitudes(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = LocalColoresPerfil.current.titulo,
        modifier = Modifier
            .padding(bottom = 4.dp)
            .semantics { heading() },
    )
}

/** Avatar y nombre de una persona, con una línea de [detalle] opcional. */
@Composable
private fun FilaPersona(persona: UsuarioEntity, modifier: Modifier = Modifier, detalle: String? = null) {
    val colores = LocalColoresPerfil.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(nombre = persona.nombre, imagenUri = persona.avatarUri, tamano = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = persona.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = colores.nombre,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (detalle != null) {
                Text(text = detalle, style = MaterialTheme.typography.bodySmall, color = colores.textoSecundario)
            }
        }
    }
}

// ---------- Botón de amistad del perfil de otra persona ----------

/**
 * Botón para pedir amistad a [idPersona], aceptar su solicitud o dejar de ser amigos. Va en el
 * perfil de otra persona (E4-08.1): `BotonAmistad(idPersona = id)`. En el perfil propio no se
 * dibuja.
 */
@Composable
fun BotonAmistad(idPersona: String, modifier: Modifier = Modifier) {
    val viewModel: AmistadViewModel = viewModel(key = "amistad-$idPersona", factory = AmistadViewModel.factory(idPersona))
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    BotonAmistadContenido(
        estado = estado,
        alPedir = viewModel::pedir,
        alAceptar = viewModel::aceptar,
        alBorrar = viewModel::borrar,
        modifier = modifier,
    )
}

/** Lo que enseña [BotonAmistad]. */
data class BotonAmistadUiState(
    val relacion: RelacionAmistad = RelacionAmistad.Ninguna,
    /** Se está enviando la petición: el botón se desactiva. */
    val ocupado: Boolean = false,
    val error: Int? = null,
)

@Composable
internal fun BotonAmistadContenido(
    estado: BotonAmistadUiState,
    alPedir: () -> Unit,
    alAceptar: () -> Unit,
    alBorrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (estado.relacion == RelacionAmistad.EsMiPerfil) return
    var preguntarDejar by rememberSaveable { mutableStateOf(false) }
    val colores = LocalColoresPerfil.current
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            when (estado.relacion) {
                RelacionAmistad.EsMiPerfil -> Unit
                RelacionAmistad.Ninguna -> Button(onClick = alPedir, enabled = !estado.ocupado) {
                    Text(stringResource(if (estado.ocupado) R.string.amistades_un_momento else R.string.amistades_anadir))
                }
                is RelacionAmistad.SolicitudEnviada -> {
                    Text(
                        text = stringResource(R.string.amistades_enviada),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.textoSecundario,
                    )
                    RetroButton(texto = stringResource(R.string.amistades_cancelar), onClick = { if (!estado.ocupado) alBorrar() })
                }
                is RelacionAmistad.SolicitudRecibida -> {
                    Button(onClick = alAceptar, enabled = !estado.ocupado) {
                        Text(stringResource(if (estado.ocupado) R.string.amistades_un_momento else R.string.amistades_aceptar))
                    }
                    RetroButton(texto = stringResource(R.string.amistades_rechazar), onClick = { if (!estado.ocupado) alBorrar() })
                }
                is RelacionAmistad.Amigos -> {
                    Text(
                        text = stringResource(R.string.amistades_son_amigos),
                        style = MaterialTheme.typography.labelLarge,
                        color = colores.titulo,
                    )
                    RetroButton(texto = stringResource(R.string.amistades_dejar), onClick = { preguntarDejar = true })
                }
            }
        }
        if (estado.error != null) {
            Text(
                text = stringResource(estado.error),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.error,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
    if (preguntarDejar) {
        AlertDialog(
            onDismissRequest = { preguntarDejar = false },
            title = { Text(stringResource(R.string.amistades_dejar_pregunta)) },
            text = { Text(stringResource(R.string.amistades_dejar_explicacion)) },
            confirmButton = {
                TextButton(onClick = {
                    preguntarDejar = false
                    alBorrar()
                }) { Text(stringResource(R.string.amistades_dejar)) }
            },
            dismissButton = {
                TextButton(onClick = { preguntarDejar = false }) { Text(stringResource(R.string.amistades_dejar_cancelar)) }
            },
        )
    }
}

/** Estado del botón de amistad con una persona (EX-04, #129). */
class AmistadViewModel(
    private val amistades: AmistadesRepository,
    private val idPersona: String,
) : ViewModel() {

    private val ocupado = MutableStateFlow(false)
    private val error = MutableStateFlow<Int?>(null)

    val estado: StateFlow<BotonAmistadUiState> = combine(amistades.relacionCon(idPersona), ocupado, error) { relacion, enviando, fallo ->
        BotonAmistadUiState(relacion, enviando, fallo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BotonAmistadUiState())

    init {
        viewModelScope.launch { amistades.refrescarMias() }
    }

    fun pedir() = ejecutar { amistades.pedirAmistad(idPersona) }

    fun aceptar() {
        val relacion = estado.value.relacion as? RelacionAmistad.SolicitudRecibida ?: return
        ejecutar { amistades.aceptar(relacion.idAmistad) }
    }

    /** Cancela la solicitud enviada, rechaza la recibida o deja de ser amigos. */
    fun borrar() {
        val id = when (val relacion = estado.value.relacion) {
            is RelacionAmistad.SolicitudEnviada -> relacion.idAmistad
            is RelacionAmistad.SolicitudRecibida -> relacion.idAmistad
            is RelacionAmistad.Amigos -> relacion.idAmistad
            else -> return
        }
        ejecutar { amistades.borrar(id) }
    }

    private fun ejecutar(accion: suspend () -> ResultadoRed<Unit>) {
        if (ocupado.value) return
        viewModelScope.launch {
            ocupado.value = true
            error.value = null
            val resultado = accion()
            if (resultado is ResultadoRed.Fallo) {
                error.value = resultado.mensaje()
                // Puede que la otra persona haya contestado mientras tanto: se trae lo último
                amistades.refrescarMias()
            }
            ocupado.value = false
        }
    }

    companion object {
        fun factory(idPersona: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RetroconverApp
                AmistadViewModel(app.container.amistadesRepository, idPersona)
            }
        }
    }
}
