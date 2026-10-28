package com.retroconver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.retroconver.navigation.PantallaPrincipal
import com.retroconver.ui.theme.RetroconverTheme

/** Única Activity de la app. Aplica el tema y muestra la navegación entre secciones. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetroconverTheme {
                PantallaPrincipal()
            }
        }
    }
}
