package com.retroconver

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retroconver.data.sesion.EstadoSesion
import com.retroconver.navigation.RetroconverRaiz
import com.retroconver.ui.theme.RetroconverTheme

/**
 * Única Activity de la app. Aplica el tema y, según haya sesión o no, muestra el inicio de sesión
 * o la app con la navegación entre secciones. Avisa si no hay red.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos de la barra de estado en blanco, para que se vean sobre la cabecera azul
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val container = (application as RetroconverApp).container
        setContent {
            val estadoSesion by container.sesionRepository.estado.collectAsStateWithLifecycle(EstadoSesion.Cargando)
            val hayConexion by container.monitorConexion.hayConexion.collectAsStateWithLifecycle(initialValue = true)
            RetroconverTheme {
                RetroconverRaiz(estadoSesion = estadoSesion, hayConexion = hayConexion)
            }
        }
    }
}
