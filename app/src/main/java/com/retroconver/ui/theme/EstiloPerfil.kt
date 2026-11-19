package com.retroconver.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.retroconver.R

/**
 * Colores con los que se dibuja un perfil (EX-03, #30). Todos los textos tienen un contraste
 * de 4,5:1 o más sobre [tarjeta] y [destacado] (lo comprueba EstiloPerfilTest).
 */
@Immutable
data class ColoresPerfil(
    /** Fondo de la pantalla, detrás de las tarjetas. */
    val fondo: Color,
    val tarjeta: Color,
    val bordeTarjeta: Color,
    /** Tarjeta del estado, como el mensaje personal de MSN. */
    val destacado: Color,
    val bordeDestacado: Color,
    /** Títulos de cada apartado («Sobre mí», «Estado»…). */
    val titulo: Color,
    /** Nombre de la persona. */
    val nombre: Color,
    val texto: Color,
    /** Fechas y textos de ayuda. */
    val textoSecundario: Color,
    val error: Color,
)

/**
 * Temas clásicos del perfil, al estilo de las redes de los 2000 (EX-03, #30). Se guardan en el
 * servidor por su [clave] (columna `perfiles.tema`) y cualquiera que visite el perfil lo ve así.
 */
enum class TemaPerfil(val clave: String, @StringRes val nombre: Int, val colores: ColoresPerfil) {
    /** El de la guía de estilo (docs/estilo.md): azul claro, como el primer Tuenti. */
    CLASICO(
        clave = "clasico",
        nombre = R.string.tema_clasico,
        colores = ColoresPerfil(
            fondo = RetroBackground,
            tarjeta = RetroCardBackground,
            bordeTarjeta = RetroCardBorder,
            destacado = RetroProfileCardBg,
            bordeDestacado = RetroProfileCardBorder,
            titulo = RetroGlossyDark,
            nombre = RetroLinkBlue,
            texto = RetroTextDark,
            textoSecundario = RetroGrayText,
            error = RetroErrorRed,
        ),
    ),

    /** Oscuro con rosa y azul neón, como los perfiles de MySpace. */
    NOCHE(
        clave = "noche",
        nombre = R.string.tema_noche,
        colores = ColoresPerfil(
            fondo = Color(0xFF0D0D1A),
            tarjeta = Color(0xFF1C1C2E),
            bordeTarjeta = Color(0xFF3D3D66),
            destacado = Color(0xFF2A1F3D),
            bordeDestacado = Color(0xFF7A5CB8),
            titulo = Color(0xFFFF9AD5),
            nombre = Color(0xFF8AD4FF),
            texto = Color(0xFFEEEEF5),
            textoSecundario = Color(0xFFB9B9CC),
            error = Color(0xFFFF8A80),
        ),
    ),

    /** Verde claro, como las ventanas de MSN Messenger. */
    LIMA(
        clave = "lima",
        nombre = R.string.tema_lima,
        colores = ColoresPerfil(
            fondo = Color(0xFFD5EFC8),
            tarjeta = Color.White,
            bordeTarjeta = Color(0xFF9CCF88),
            destacado = Color(0xFFECF8E4),
            bordeDestacado = Color(0xFF7DB866),
            titulo = Color(0xFF2B5F1C),
            nombre = Color(0xFF1D5E91),
            texto = RetroTextDark,
            textoSecundario = Color(0xFF4E5A49),
            error = RetroErrorRed,
        ),
    ),

    /** Rosa chicle, como los fotologs y los blogs más decorados de la época. */
    CHICLE(
        clave = "chicle",
        nombre = R.string.tema_chicle,
        colores = ColoresPerfil(
            fondo = Color(0xFFF9D3E5),
            tarjeta = Color.White,
            bordeTarjeta = Color(0xFFEFA6C6),
            destacado = Color(0xFFFDE9F2),
            bordeDestacado = Color(0xFFE38AB2),
            titulo = Color(0xFF9C1757),
            nombre = Color(0xFFA8156A),
            texto = Color(0xFF2A1A22),
            textoSecundario = Color(0xFF6B4757),
            error = RetroErrorRed,
        ),
    ),
    ;

    companion object {
        /** El tema guardado con esa clave, o [CLASICO] si no hay ninguno o no se conoce. */
        fun deClave(clave: String?): TemaPerfil = entries.firstOrNull { it.clave == clave } ?: CLASICO
    }
}

/**
 * Tipografías del perfil (EX-03, #30). Son las familias que trae Android, así no hay que
 * descargar fuentes. Se guardan por su [clave] en `perfiles.tipografia`.
 */
enum class TipografiaPerfil(val clave: String, @StringRes val nombre: Int, val familia: FontFamily) {
    /** La de toda la app, parecida a Arial y Verdana. */
    NORMAL("normal", R.string.tipografia_normal, FontFamily.SansSerif),

    /** Con serifa, como Times New Roman y Georgia. */
    CLASICA("clasica", R.string.tipografia_clasica, FontFamily.Serif),

    /** De ancho fijo, como Courier en los blogs de la época. */
    MAQUINA("maquina", R.string.tipografia_maquina, FontFamily.Monospace),

    /** Manuscrita, para los perfiles más decorados. */
    MANUSCRITA("manuscrita", R.string.tipografia_manuscrita, FontFamily.Cursive),
    ;

    companion object {
        /** La tipografía guardada con esa clave, o [NORMAL] si no hay ninguna o no se conoce. */
        fun deClave(clave: String?): TipografiaPerfil = entries.firstOrNull { it.clave == clave } ?: NORMAL
    }
}

/**
 * Colores de fondo del perfil (E4-11, #23): el que se ve detrás de las tarjetas. Sin elegir
 * ninguno, el perfil usa el fondo de su tema. Se guardan por su [clave] en `perfiles.color_fondo`.
 * Todos los textos van sobre las tarjetas, así que el fondo no cambia su contraste.
 */
enum class FondoPerfil(val clave: String, @StringRes val nombre: Int, val color: Color) {
    CIELO("cielo", R.string.fondo_cielo, Color(0xFFBFD9F2)),
    MENTA("menta", R.string.fondo_menta, Color(0xFFC9EBD7)),
    LAVANDA("lavanda", R.string.fondo_lavanda, Color(0xFFDCD3F2)),
    MELOCOTON("melocoton", R.string.fondo_melocoton, Color(0xFFF8D9C4)),
    LIMON("limon", R.string.fondo_limon, Color(0xFFF4EDB2)),
    PIZARRA("pizarra", R.string.fondo_pizarra, Color(0xFF3B4252)),
    ;

    companion object {
        /** El fondo guardado con esa clave, o `null` (el de su tema) si no hay ninguno o no se conoce. */
        fun deClave(clave: String?): FondoPerfil? = entries.firstOrNull { it.clave == clave }
    }
}

/** Colores del perfil que se está dibujando. Fuera de [EstiloDePerfil] son los del tema clásico. */
val LocalColoresPerfil = staticCompositionLocalOf { TemaPerfil.CLASICO.colores }

/**
 * Dibuja [content] con el tema, la tipografía y el color de fondo de un perfil: los colores se
 * leen con `LocalColoresPerfil.current` y los textos de MaterialTheme salen con la tipografía
 * elegida. Sin [fondo], el de su tema.
 */
@Composable
fun EstiloDePerfil(
    tema: TemaPerfil,
    tipografia: TipografiaPerfil,
    fondo: FondoPerfil? = null,
    content: @Composable () -> Unit,
) {
    val colores = if (fondo == null) tema.colores else tema.colores.copy(fondo = fondo.color)
    CompositionLocalProvider(LocalColoresPerfil provides colores) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme,
            typography = MaterialTheme.typography.conFamilia(tipografia.familia),
            shapes = MaterialTheme.shapes,
            content = content,
        )
    }
}

/** La misma tipografía con otra familia de letra en todos los estilos. */
internal fun Typography.conFamilia(familia: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = familia),
    displayMedium = displayMedium.copy(fontFamily = familia),
    displaySmall = displaySmall.copy(fontFamily = familia),
    headlineLarge = headlineLarge.copy(fontFamily = familia),
    headlineMedium = headlineMedium.copy(fontFamily = familia),
    headlineSmall = headlineSmall.copy(fontFamily = familia),
    titleLarge = titleLarge.copy(fontFamily = familia),
    titleMedium = titleMedium.copy(fontFamily = familia),
    titleSmall = titleSmall.copy(fontFamily = familia),
    bodyLarge = bodyLarge.copy(fontFamily = familia),
    bodyMedium = bodyMedium.copy(fontFamily = familia),
    bodySmall = bodySmall.copy(fontFamily = familia),
    labelLarge = labelLarge.copy(fontFamily = familia),
    labelMedium = labelMedium.copy(fontFamily = familia),
    labelSmall = labelSmall.copy(fontFamily = familia),
)
