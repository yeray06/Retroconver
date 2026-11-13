package com.retroconver.ui.screens.acceso

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.R
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroGrayText
import com.retroconver.ui.theme.RetroTextDark
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Registro con nombre, email y contraseña (E4-04.2, #118). El servidor crea el perfil con ese
 * nombre. Si el proyecto pide confirmar el email, se explica en vez de entrar.
 */
@Composable
fun RegistroScreen(
    hayConexion: Boolean,
    alIrAEntrar: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory),
) {
    RegistroContenido(
        hayConexion = hayConexion,
        nombre = viewModel.nombre,
        email = viewModel.email,
        contrasena = viewModel.contrasena,
        errores = viewModel.errores,
        errorServidor = viewModel.errorServidor,
        enviando = viewModel.enviando,
        emailPorConfirmar = viewModel.emailPorConfirmar,
        alCambiarNombre = viewModel::cambiarNombre,
        alCambiarEmail = viewModel::cambiarEmail,
        alCambiarContrasena = viewModel::cambiarContrasena,
        alCrearCuenta = viewModel::crearCuenta,
        alIrAEntrar = alIrAEntrar,
    )
}

@Composable
internal fun RegistroContenido(
    hayConexion: Boolean,
    nombre: String,
    email: String,
    contrasena: String,
    errores: ErroresAcceso,
    errorServidor: Int?,
    enviando: Boolean,
    emailPorConfirmar: String?,
    alCambiarNombre: (String) -> Unit,
    alCambiarEmail: (String) -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alCrearCuenta: () -> Unit,
    alIrAEntrar: () -> Unit,
) {
    PlantillaAcceso(hayConexion = hayConexion) {
        if (emailPorConfirmar != null) {
            ConfirmaTuEmail(emailPorConfirmar, alIrAEntrar)
        } else {
            FormularioRegistro(
                nombre, email, contrasena, errores, errorServidor, enviando,
                alCambiarNombre, alCambiarEmail, alCambiarContrasena, alCrearCuenta, alIrAEntrar,
            )
        }
    }
}

@Composable
private fun FormularioRegistro(
    nombre: String,
    email: String,
    contrasena: String,
    errores: ErroresAcceso,
    errorServidor: Int?,
    enviando: Boolean,
    alCambiarNombre: (String) -> Unit,
    alCambiarEmail: (String) -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alCrearCuenta: () -> Unit,
    alIrAEntrar: () -> Unit,
) {
    TarjetaAcceso {
        Text(
            text = stringResource(R.string.acceso_titulo_registro),
            style = MaterialTheme.typography.titleLarge,
            color = RetroGlossyDark,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.acceso_registro_explicacion),
            style = MaterialTheme.typography.bodyMedium,
            color = RetroGrayText,
        )
        CampoAcceso(
            etiqueta = stringResource(R.string.acceso_nombre),
            valor = nombre,
            alCambiar = alCambiarNombre,
            error = textoError(errores.nombre),
            habilitado = !enviando,
            tipoTeclado = KeyboardType.Text,
            accionTeclado = ImeAction.Next,
            capitalizacion = KeyboardCapitalization.Words,
        )
        CampoAcceso(
            etiqueta = stringResource(R.string.acceso_email),
            valor = email,
            alCambiar = alCambiarEmail,
            error = textoError(errores.email),
            habilitado = !enviando,
            tipoTeclado = KeyboardType.Email,
            accionTeclado = ImeAction.Next,
        )
        CampoContrasena(
            valor = contrasena,
            alCambiar = alCambiarContrasena,
            error = textoError(errores.contrasena),
            habilitado = !enviando,
            accionTeclado = ImeAction.Done,
            alPulsarAccion = alCrearCuenta,
            pista = stringResource(R.string.acceso_pista_contrasena, LimitesAcceso.CONTRASENA_MIN),
        )
        ErrorDelServidor(errorServidor)
        Button(onClick = alCrearCuenta, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (enviando) R.string.acceso_creando else R.string.acceso_crear_cuenta))
        }
    }
    EnlaceAcceso(
        pregunta = stringResource(R.string.acceso_con_cuenta),
        enlace = stringResource(R.string.acceso_ir_a_entrar),
        alPulsar = alIrAEntrar,
    )
}

/** La cuenta está creada, pero el proyecto pide confirmar el email antes de entrar. */
@Composable
private fun ConfirmaTuEmail(email: String, alIrAEntrar: () -> Unit) {
    TarjetaAcceso {
        Text(
            text = stringResource(R.string.acceso_confirma_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = RetroGlossyDark,
            modifier = Modifier.semantics {
                heading()
                liveRegion = LiveRegionMode.Polite
            },
        )
        Text(
            text = stringResource(R.string.acceso_confirma_texto, email),
            style = MaterialTheme.typography.bodyLarge,
            color = RetroTextDark,
        )
        Button(onClick = alIrAEntrar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.acceso_volver_a_entrar))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroPreview() {
    RetroconverTheme {
        RegistroContenido(
            hayConexion = true,
            nombre = "",
            email = "yeray@retroconver.es",
            contrasena = "123",
            errores = ErroresAcceso(nombre = ErrorAcceso.OBLIGATORIO, contrasena = ErrorAcceso.CONTRASENA_CORTA),
            errorServidor = null,
            enviando = false,
            emailPorConfirmar = null,
            alCambiarNombre = {},
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alCrearCuenta = {},
            alIrAEntrar = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroConfirmarEmailPreview() {
    RetroconverTheme {
        RegistroContenido(
            hayConexion = true,
            nombre = "Yeray",
            email = "yeray@retroconver.es",
            contrasena = "",
            errores = ErroresAcceso(),
            errorServidor = null,
            enviando = false,
            emailPorConfirmar = "yeray@retroconver.es",
            alCambiarNombre = {},
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alCrearCuenta = {},
            alIrAEntrar = {},
        )
    }
}
