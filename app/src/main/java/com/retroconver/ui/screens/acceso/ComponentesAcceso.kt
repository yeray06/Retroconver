package com.retroconver.ui.screens.acceso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.retroconver.R
import com.retroconver.ui.components.AvisoSinConexion
import com.retroconver.ui.components.RetroCard
import com.retroconver.ui.components.RetroHeader
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText

/**
 * Estructura común de las pantallas de acceso: la cabecera azul con el logo, la franja de
 * «Sin conexión» si hace falta, el lema y el formulario centrado.
 */
@Composable
internal fun PlantillaAcceso(
    hayConexion: Boolean,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        RetroHeader(titulo = stringResource(R.string.logo_cabecera))
        if (!hayConexion) AvisoSinConexion()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.acceso_lema),
                style = MaterialTheme.typography.titleMedium,
                color = RetroGlossyDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            // En tablets y en horizontal el formulario no se estira a todo el ancho
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = contenido,
            )
        }
    }
}

/** Tarjeta blanca del formulario, con aire entre los campos. */
@Composable
internal fun TarjetaAcceso(contenido: @Composable ColumnScope.() -> Unit) {
    RetroCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido)
    }
}

/**
 * Campo de texto de los formularios de acceso. Si hay error, el mensaje aparece debajo y
 * el campo se marca en rojo; TalkBack lee el error al llegar al campo.
 */
@Composable
internal fun CampoAcceso(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    error: String?,
    habilitado: Boolean,
    tipoTeclado: KeyboardType,
    accionTeclado: ImeAction,
    modifier: Modifier = Modifier,
    pista: String? = null,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    alPulsarAccion: () -> Unit = {},
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        isError = error != null,
        singleLine = true,
        supportingText = if (error != null || pista != null) { { Text(error ?: pista.orEmpty()) } } else null,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalizacion,
            autoCorrectEnabled = false,
            keyboardType = tipoTeclado,
            imeAction = accionTeclado,
        ),
        keyboardActions = KeyboardActions(onAny = { alPulsarAccion() }),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Campo de contraseña con un botón para verla, útil para comprobar lo escrito en el móvil. */
@Composable
internal fun CampoContrasena(
    valor: String,
    alCambiar: (String) -> Unit,
    error: String?,
    habilitado: Boolean,
    accionTeclado: ImeAction,
    alPulsarAccion: () -> Unit,
    modifier: Modifier = Modifier,
    pista: String? = null,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val descripcionBoton = stringResource(
        if (visible) R.string.acceso_descripcion_ocultar else R.string.acceso_descripcion_ver,
    )
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(stringResource(R.string.acceso_contrasena)) },
        enabled = habilitado,
        isError = error != null,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(
                onClick = { visible = !visible },
                modifier = Modifier.semantics { contentDescription = descripcionBoton },
            ) {
                Text(stringResource(if (visible) R.string.acceso_ocultar else R.string.acceso_ver))
            }
        },
        supportingText = if (error != null || pista != null) { { Text(error ?: pista.orEmpty()) } } else null,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            imeAction = accionTeclado,
        ),
        keyboardActions = KeyboardActions(onAny = { alPulsarAccion() }),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Error que devuelve el servidor (credenciales, email ya registrado, sin conexión…). TalkBack lo lee al aparecer. */
@Composable
internal fun ErrorDelServidor(idTexto: Int?) {
    if (idTexto == null) return
    Text(
        text = stringResource(idTexto),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

/** «¿Todavía no tienes cuenta? Regístrate», con el enlace a la otra pantalla de acceso. */
@Composable
internal fun EnlaceAcceso(pregunta: String, enlace: String, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = pregunta, style = MaterialTheme.typography.bodyMedium, color = RetroGrayText)
        TextButton(onClick = alPulsar) { Text(enlace) }
    }
}

/** Texto del error de un campo, o `null` si está bien. */
@Composable
internal fun textoError(error: ErrorAcceso?): String? = when (error) {
    null -> null
    ErrorAcceso.OBLIGATORIO -> stringResource(R.string.acceso_error_obligatorio)
    ErrorAcceso.EMAIL_NO_VALIDO -> stringResource(R.string.acceso_error_email)
    ErrorAcceso.CONTRASENA_CORTA -> stringResource(R.string.acceso_error_contrasena_corta, LimitesAcceso.CONTRASENA_MIN)
    ErrorAcceso.DEMASIADO_LARGO -> stringResource(R.string.acceso_error_nombre_largo, LimitesAcceso.NOMBRE_MAX)
}
