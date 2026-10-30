package com.retroconver

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.retroconver.navigation.PantallaPrincipal
import com.retroconver.ui.theme.RetroconverTheme

/** Única Activity de la app. Aplica el tema y muestra la navegación entre secciones. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos de la barra de estado en blanco, para que se vean sobre la cabecera azul
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            RetroconverTheme {
                PantallaPrincipal()
            }
        }
    }
}
