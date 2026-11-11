package com.retroconver

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retroconver.navigation.PantallaPrincipal
import com.retroconver.ui.theme.RetroconverTheme

/** Única Activity de la app. Aplica el tema, muestra la navegación entre secciones y avisa si no hay red. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos de la barra de estado en blanco, para que se vean sobre la cabecera azul
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val monitorConexion = (application as RetroconverApp).container.monitorConexion
        setContent {
            val hayConexion by monitorConexion.hayConexion.collectAsStateWithLifecycle(initialValue = true)
            RetroconverTheme {
                PantallaPrincipal(hayConexion = hayConexion)
            }
        }
    }
}
