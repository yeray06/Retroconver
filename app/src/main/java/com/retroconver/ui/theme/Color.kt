package com.retroconver.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta retro extraída del prototipo final (docs/diseño/diseñofinalvisual). Ver docs/estilo.md.

// Degradado "glossy" de la cabecera, de arriba abajo
val RetroGlossyTop = Color(0xFF2B82DF)
val RetroGlossyMid = Color(0xFF0056B3)
val RetroGlossyDark = Color(0xFF004085)
val RetroGlossyDeepDark = Color(0xFF002D5E)

// Botón de acción de la cabecera ("+")
val RetroActionTop = Color(0xFF3B97FF)
val RetroActionBottom = Color(0xFF0060CB)
val RetroActionBorder = Color(0xFF003A7D)

// Fondo general y superficies de tarjetas
val RetroBackground = Color(0xFFC5D7E8)
val RetroCardBackground = Color(0xFFFFFFFF)
val RetroCardBorder = Color(0xFFB8CEEE)
val RetroProfileCardBg = Color(0xFFE6F0FA)
val RetroProfileCardBorder = Color(0xFFA3C2E0)

// Botones planos (comentarios, "me gusta"...)
val RetroButtonBackground = Color(0xFFF8F9FA)
val RetroButtonBorder = Color(0xFFBBBBBB)

// Textos y acentos
val RetroTextDark = Color(0xFF222222)
val RetroLinkBlue = Color(0xFF0066CC)
val RetroGrayText = Color(0xFF555555)

// Avisos (franja de «Sin conexión»): amarillo pálido como los avisos de Tuenti. Con RetroTextDark el contraste es de 14:1
val RetroAvisoFondo = Color(0xFFFFF4C2)
val RetroAvisoBorde = Color(0xFFE0C860)

// Errores. Algo más oscuro que el rojo por defecto de Material 3 (#B3261E), que sobre el fondo
// azul claro se quedaba en 4,4:1. Este cumple 4,5:1 en todos los fondos (E4-12.2, #78)
val RetroErrorRed = Color(0xFFA3211B)
