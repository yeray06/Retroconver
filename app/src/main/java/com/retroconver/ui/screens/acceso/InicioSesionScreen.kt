package com.retroconver.ui.screens.acceso

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retroconver.R
import com.retroconver.ui.theme.RetroGlossyDark
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Inicio de sesión (E4-04.2, #118). Es lo primero que se ve si no hay sesión.
 * Al entrar, la app pasa sola al muro: esta pantalla no navega.
 */
@Composable
fun InicioSesionScreen(
    hayConexion: Boolean,
    alIrARegistro: () -> Unit,
    viewModel: InicioSesionViewModel = viewModel(factory = InicioSesionViewModel.Factory),
) {
    InicioSesionContenido(
        hayConexion = hayConexion,
        email = viewModel.email,
        contrasena = viewModel.contrasena,
        errores = viewModel.errores,
        errorServidor = viewModel.errorServidor,
        enviando = viewModel.enviando,
        alCambiarEmail = viewModel::cambiarEmail,
        alCambiarContrasena = viewModel::cambiarContrasena,
        alEntrar = viewModel::entrar,
        alIrARegistro = alIrARegistro,
    )
}

@Composable
internal fun InicioSesionContenido(
    hayConexion: Boolean,
    email: String,
    contrasena: String,
    errores: ErroresAcceso,
    errorServidor: Int?,
    enviando: Boolean,
    alCambiarEmail: (String) -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alEntrar: () -> Unit,
    alIrARegistro: () -> Unit,
) {
    PlantillaAcceso(hayConexion = hayConexion) {
        TarjetaAcceso {
            Text(
                text = stringResource(R.string.acceso_titulo_entrar),
                style = MaterialTheme.typography.titleLarge,
                color = RetroGlossyDark,
                modifier = Modifier.semantics { heading() },
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
                alPulsarAccion = alEntrar,
            )
            ErrorDelServidor(errorServidor)
            Button(onClick = alEntrar, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (enviando) R.string.acceso_entrando else R.string.acceso_entrar))
            }
        }
        EnlaceAcceso(
            pregunta = stringResource(R.string.acceso_sin_cuenta),
            enlace = stringResource(R.string.acceso_ir_a_registro),
            alPulsar = alIrARegistro,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InicioSesionPreview() {
    RetroconverTheme {
        InicioSesionContenido(
            hayConexion = true,
            email = "yeray@correo",
            contrasena = "",
            errores = ErroresAcceso(email = ErrorAcceso.EMAIL_NO_VALIDO, contrasena = ErrorAcceso.OBLIGATORIO),
            errorServidor = null,
            enviando = false,
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alEntrar = {},
            alIrARegistro = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InicioSesionSinConexionPreview() {
    RetroconverTheme {
        InicioSesionContenido(
            hayConexion = false,
            email = "yeray@retroconver.es",
            contrasena = "secreta",
            errores = ErroresAcceso(),
            errorServidor = R.string.red_sin_conexion,
            enviando = false,
            alCambiarEmail = {},
            alCambiarContrasena = {},
            alEntrar = {},
            alIrARegistro = {},
        )
    }
}
