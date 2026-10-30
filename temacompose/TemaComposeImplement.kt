package com.retroconver.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- PALETA DE COLORES EXTRAÍDA DEL HTML FINAL ---
val RetroGlossy1 = Color(0xFF2B82DF)
val RetroGlossy2 = Color(0xFF0056B3)
val RetroGlossy3 = Color(0xFF004085)
val RetroGlossy4 = Color(0xFF002D5E)

val RetroBackground = Color(0xFFC5D7E8)
val RetroProfileCardBg = Color(0xFFE6F0FA)
val RetroProfileCardBorder = Color(0xFFA3C2E0)
val RetroCardBorder = Color(0xFFB8CEEE)
val RetroTextDark = Color(0xFF222222)
val RetroLinkBlue = Color(0xFF0066CC)

@Composable
fun RetroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = RetroGlossy3,
            background = RetroBackground,
            surface = Color.White
        ),
        content = content
    )
}

// --- COMPONENTES BASE REUTILIZABLES ---

// 1. Cabecera Glossy con Sub-Tabs
@Composable
fun RetroHeader(
    activeSubTab: String = "MOMENTOS",
    onAddClick: () -> Unit = {},
    onSubTabSelect: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(RetroGlossy1, RetroGlossy2, RetroGlossy3, RetroGlossy4)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "retroconver",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3B97FF), Color(0xFF0060CB))
                        )
                    )
                    .border(1.dp, Color(0xFF003A7D), RoundedCornerShape(4.dp))
                    .clickable { onAddClick() }
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sub-tabs CHAT / MOMENTOS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.25f))
        ) {
            listOf("CHAT", "MOMENTOS").forEach { tab ->
                val isActive = activeSubTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSubTabSelect(tab) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isActive) Color.White else Color(0xFFB3D7FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 2. Tarjeta Base
@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, RetroCardBorder, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            content = content
        )
    }
}

// 3. Botón con Borde Discontinuo (Likes / Comentarios)
@Composable
fun CounterBtn(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFFBBBBBB), RoundedCornerShape(3.dp))
            .background(Color(0xFFF8F9FA), RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFF555555),
            fontWeight = FontWeight.Bold
        )
    }
}