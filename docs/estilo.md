# Guía de estilo visual — Retroconver

Guía de estilo de la app (E1-06, [#6](https://github.com/yeray06/Retroconver/issues/6)), actualizada con el tema de Compose de E2-06 ([#12](https://github.com/yeray06/Retroconver/issues/12)). Los valores de esta guía son los que hay en el código: si se cambia un color o un tamaño, se cambia en los dos sitios.

**Código:** `app/src/main/java/com/retroconver/ui/theme/` (colores, tipografía, formas y tema) y `app/src/main/java/com/retroconver/ui/components/` (componentes base).

## 1. Paleta de colores

Sale del prototipo final de los bocetos (E2-04). Está en `Color.kt`.

### Cabecera y botones de acción

| Color | Uso | HEX |
|---|---|---|
| `RetroGlossyTop` | Arriba del degradado de la cabecera | `#2B82DF` |
| `RetroGlossyMid` | Degradado de la cabecera | `#0056B3` |
| `RetroGlossyDark` | Degradado de la cabecera. Color `primary` del tema | `#004085` |
| `RetroGlossyDeepDark` | Abajo del degradado de la cabecera | `#002D5E` |
| `RetroActionTop` / `RetroActionBottom` | Degradado del botón de acción de la cabecera (`+`) | `#3B97FF` → `#0060CB` |
| `RetroActionBorder` | Borde del botón de acción | `#003A7D` |

### Fondos, tarjetas y texto

| Color | Uso | HEX |
|---|---|---|
| `RetroBackground` | Fondo de todas las pantallas | `#C5D7E8` |
| `RetroCardBackground` | Fondo de las tarjetas del muro y la galería | `#FFFFFF` |
| `RetroCardBorder` | Borde de las tarjetas | `#B8CEEE` |
| `RetroProfileCardBg` | Tarjeta de estado del perfil y fondo del avatar | `#E6F0FA` |
| `RetroProfileCardBorder` | Borde de la tarjeta de estado y del avatar | `#A3C2E0` |
| `RetroButtonBackground` | Fondo de los botones secundarios | `#F8F9FA` |
| `RetroButtonBorder` | Borde de los botones secundarios | `#BBBBBB` |
| `RetroTextDark` | Texto principal | `#222222` |
| `RetroGrayText` | Fechas, contadores y texto secundario | `#555555` |
| `RetroLinkBlue` | Nombres y enlaces | `#0066CC` |

### Colores del tema de Material

`Theme.kt` traduce la paleta a un `lightColorScheme`, así los componentes de Material (barra de navegación, campos de texto, diálogos) salen con los colores retro sin tocarlos uno a uno:

| Rol de Material | Color retro |
|---|---|
| `primary` / `onPrimary` | `RetroGlossyDark` / blanco |
| `secondary` / `onSecondary` | `RetroGlossyTop` / blanco |
| `secondaryContainer` / `onSecondaryContainer` (indicador de la barra inferior) | `RetroProfileCardBg` / `RetroGlossyDark` |
| `background` / `onBackground` | `RetroBackground` / `RetroTextDark` |
| `surface`, `surfaceContainer` / `onSurface` | `RetroCardBackground` / `RetroTextDark` |
| `surfaceVariant` / `onSurfaceVariant` | `RetroProfileCardBg` / `RetroGrayText` |
| `outline` | `RetroCardBorder` |

La app solo tiene tema claro. El color de fondo del perfil que elige la persona usuaria (RF-08) se guarda en DataStore en E4-04 y no cambia este tema.

### Contraste (RNF-03)

Calculado con la fórmula de WCAG 2.1. El mínimo es 4,5:1 para texto normal y 3:1 para texto grande (18 sp o más, o 14 sp en negrita).

| Texto sobre fondo | Contraste | Uso |
|---|---|---|
| `RetroTextDark` sobre blanco | 15,9:1 | Texto de las tarjetas |
| `RetroTextDark` sobre `RetroBackground` | 10,8:1 | Texto directo sobre el fondo |
| `RetroGrayText` sobre blanco | 7,5:1 | Fechas y contadores |
| `RetroGrayText` sobre `RetroBackground` | 5,1:1 | Texto secundario sobre el fondo |
| `RetroLinkBlue` sobre blanco | 5,6:1 | Nombres y enlaces |
| Blanco sobre `RetroGlossyDark` | 10,1:1 | Botones y barra de estado |
| Blanco sobre `RetroGlossyTop` | 3,9:1 | Solo texto grande: el título de la cabecera (22 sp) |

## 2. Tipografía

Está en `Type.kt` (`RetroTypography`). Todos los estilos usan la sans-serif del sistema (`FontFamily.SansSerif`, Roboto en casi todos los móviles).

Arial y Verdana, las letras de la web de los 2000 que proponía la primera versión de la guía, no vienen en Android. Para usarlas habría que meter los ficheros de fuente en la app, y Verdana y Arial tienen licencia de Microsoft. La sans-serif del sistema es parecida, se lee bien en pantallas pequeñas y respeta el tamaño de letra que elige la persona en Ajustes.

| Estilo | Tamaño | Peso | Uso |
|---|---|---|---|
| `headlineMedium` | 28 sp | Negrita | Títulos grandes (pantalla vacía, perfil) |
| `titleLarge` | 22 sp | Negrita | Logo de la cabecera |
| `titleMedium` | 16 sp | Negrita | Nombre en las tarjetas, título de sección |
| `bodyLarge` | 16 sp | Normal | Texto de publicaciones y mensajes |
| `bodyMedium` | 14 sp | Normal | Comentarios y texto secundario |
| `bodySmall` | 12 sp | Normal | Fechas |
| `labelLarge` | 14 sp | Negrita | Botones |
| `labelMedium` | 12 sp | Negrita | Contadores |
| `labelSmall` | 11 sp | Normal | Etiquetas pequeñas |

Los estilos no llevan color: el color lo pone el tema o el componente, para que el mismo estilo valga sobre fondo claro y sobre la cabecera azul.

## 3. Formas

Están en `Shapes.kt` (`RetroShapes`). Esquinas casi rectas, como en la web de la época:

| Forma | Radio | Uso |
|---|---|---|
| `extraSmall` | 2 dp | Avatar |
| `small` | 3 dp | Botones secundarios |
| `medium` | 4 dp | Tarjetas y botón de la cabecera |
| `large` | 6 dp | Botones flotantes y menús laterales de Material |
| `extraLarge` | 8 dp | Diálogos y hojas inferiores |

## 4. Componentes base

Están en `ui/components/`. Cada uno tiene una `@Preview` para verlo en Android Studio sin arrancar la app.

| Componente | Qué es | Notas |
|---|---|---|
| `RetroHeader(titulo, accion)` | Cabecera con el degradado glossy y el logo `retroconver` en blanco. Admite un botón a la derecha | Se dibuja debajo de la barra de estado, que deja los iconos en blanco (`MainActivity`) |
| `RetroHeaderBoton(texto, descripcion, onClick)` | Botón de acción de la cabecera, por ejemplo `+` para publicar | `descripcion` es lo que lee TalkBack |
| `RetroCard { … }` | Tarjeta blanca con borde `RetroCardBorder`, esquinas de 4 dp, sombra de 1 dp y 12 dp de margen interior | Base de publicaciones, comentarios, mensajes y álbumes |
| `RetroButton(texto, onClick)` | Botón secundario gris claro con borde fino | Para contadores y acciones pequeñas (`💬 3`) |
| `Avatar(nombre, tamano)` | Avatar cuadrado con borde, como en MSN y Tuenti. Muestra la inicial del nombre | La foto de perfil se añade en E3-06.3 ([#55](https://github.com/yeray06/Retroconver/issues/55)) |

Todas las zonas táctiles miden al menos 48 dp (`minimumInteractiveComponentSize`) y los botones se anuncian como botón en TalkBack (RNF-03).

### Pendiente de los bocetos

Los bocetos tienen más elementos que dependen de los [puntos a decidir](requisitos.md#puntos-a-decidir-entre-los-dos) o que se hacen con su pantalla:

| En los bocetos | Cuándo |
|---|---|
| Pestañas `CHAT` / `MOMENTOS` en la cabecera | Depende de si entra el chat privado (RF-24). Mientras tanto se navega con la barra inferior |
| Botón «Me gusta» con contador (`♥`) | Depende de si se añade el «Me gusta» |
| Selector de pestañas del perfil estilo iPod Touch (*Tablón*, *Fotos*, *Información*) | Con la pantalla de perfil, E3-06.1 ([#53](https://github.com/yeray06/Retroconver/issues/53)) |
| Contador de comentarios (`💬`) | Con los comentarios, E4-01.2 ([#67](https://github.com/yeray06/Retroconver/issues/67)). Usa `RetroButton` |

## 5. Referencias visuales (años 2000)

1. **Tuenti (2008-2010).** Estructura del muro personal, cabecera azul con pestañas sencillas y perfil con avatar y datos arriba y muro y fotos debajo.
2. **MySpace (2005-2007).** Personalizar el color y el tema del perfil (RF-08) y caja de estado visible en la cabecera del perfil.
3. **MSN Messenger (2007).** Estado o frase del día y avatares cuadrados con marco.
