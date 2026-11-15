# Pruebas de Retroconver

Lista de las pruebas de la entrega 4 y su resultado (E4-12, [#24](https://github.com/yeray06/Retroconver/issues/24)): las automáticas, la prueba de seguridad del servidor y la revisión de accesibilidad. Al final están los errores encontrados, cada uno con su issue.

## 1. Cómo se ejecutan

- **En cada Pull Request**, GitHub las pasa todas: compila, ejecuta las unitarias y arranca un emulador con Android 14 para las demás. La PR enseña un check verde o rojo (README, *Integración continua*).
- **En Android Studio:** las unitarias, con la tarea `testDebugUnitTest`; las del emulador, con `connectedDebugAndroidTest` y el emulador abierto.

Ninguna prueba usa el servidor de verdad: la red se prueba con **MockWebServer**, un servidor falso que devuelve respuestas preparadas como las de Supabase.

## 2. Pruebas automáticas

**150 pruebas: 104 unitarias y 46 en el emulador.** Resultado: ✅ todas pasan en la CI ([#143](https://github.com/yeray06/Retroconver/pull/143)).

### Unitarias (sin Android)

| Clase | Qué comprueba | Pruebas | Issue |
|---|---|---|---|
| `ReescaladoTest` | Reescalado a 720p de fotos horizontales, verticales, 16:9, panorámicas, cuadradas y pequeñas; ninguna se pasa ni se deforma; orientaciones EXIF que giran la foto | 9 | #62 |
| `ConvertidoresTest` | Fechas a milisegundos y vuelta para Room | 2 | E3 |
| `ClienteSupabaseTest` | Cliente de red contra MockWebServer: cabeceras con y sin sesión, JSON correcto, login, registro con y sin confirmación de email, subida de imagen, 401, 422, 400, 503, 500 sin cuerpo, JSON inesperado, sin conexión y sin configurar | 18 | #114, #115 |
| `ResultadoRedTest` | Formatos de error de Auth, tablas y Storage; mensaje en español de cada caso; fechas del servidor; configuración | 14 | #115 |
| `SesionRepositoryTest` | Sesión con DataStore real: entrar guarda los tokens y **no la contraseña**, credenciales incorrectas, registro, renovar antes de caducar, 401 → renovar y repetir, renovación rechazada → sesión cerrada, sin red se mantiene, cerrar sesión con y sin red | 11 | #117, #119 |
| `ValidacionAccesoTest` | Formularios de acceso: emails válidos y no válidos, obligatorios, contraseña de 6 o más, límite del nombre | 10 | #118 |
| `UsuarioRepositoryTest` | Perfil en el servidor: usuario de la sesión, editar (PATCH y copia en Room), sin conexión, sin permiso, avatar (subir → apuntar → borrar el anterior), marcha atrás si falla | 8 | #121, #122 |
| `ConversionesTest` | Perfil del servidor → Room (URL pública del avatar, fechas) | 2 | #121 |
| `GaleriaRepositoryTest` | Álbumes y fotos en el servidor: refrescar, quitar lo borrado, crear álbum, subir foto y marcha atrás, borrar foto propia y no ajena, sin conexión | 9 | #68, #71 |
| `ValidacionGaleriaTest` | Nombre del álbum: obligatorio y de 40 caracteres como mucho | 3 | #69 |
| `ValidacionPerfilTest` | Formulario del perfil (E3) | 6 | #54 |
| `FechaRelativaTest` | Fecha relativa de las publicaciones del muro (David) | 6 | #59 |
| `ValidacionPublicacionTest` | Texto de una publicación (David) | 6 | #60 |

### En el emulador

| Clase | Qué comprueba | Pruebas | Issue |
|---|---|---|---|
| `UsuarioDaoTest` | Perfiles con UUID: guardar, `@Upsert` sin duplicar ni borrar sus publicaciones, editar sin tocar el avatar y al revés | 8 | #121 |
| `PublicacionDaoTest` | Orden, comentarios, cascadas, UUID y `@Upsert` de lo que llega del servidor | 8 | #58, #121 |
| `GaleriaDaoTest` | Álbumes con número de fotos y portada, orden, cascada, sincronizar quitando lo borrado sin tocar lo ajeno | 8 | #68 |
| `AlmacenImagenesTest` | Fotos reales de 12 MP a 720p, no agrandar, girar según EXIF, nombres distintos, borrar solo lo propio, medidas y borrar todo al cerrar sesión | 8 | #63, #119 |
| `AccesibilidadTest` | Revisión de accesibilidad automática (apartado 4) | 14 | #78 |

## 3. Servidor

| Prueba | Cómo | Resultado |
|---|---|---|
| Políticas de seguridad (RLS) | `supabase/pruebas_seguridad.sql` en el SQL Editor del proyecto: crea dos cuentas temporales, hace 12 comprobaciones y las borra | ✅ 12/12 el 11/11/2026, sin restos ([#136](https://github.com/yeray06/Retroconver/pull/136)) |

Las 12 comprobaciones:
1. Al registrarse se crea el perfil.
2. Se puede publicar.
3. No se puede publicar en nombre de otra persona.
4. El servidor rechaza una publicación vacía.
5. Se puede crear un álbum.
6. No se puede borrar la publicación de otra persona.
7. No se puede cambiar el perfil de otra persona.
8. Se puede comentar.
9. Se puede escribir en el tablón de otra persona.
10. No se pueden añadir fotos a un álbum ajeno.
11. El dueño de un tablón puede borrar los mensajes que le dejan.
12. Sin sesión no se ve nada.

## 4. Revisión de accesibilidad (E4-12.2, #78)

### Automática: Accessibility Test Framework

`AccesibilidadTest` dibuja cada pantalla y le pasa las comprobaciones del **Accessibility Test Framework** de Google, las mismas que hace el **Accessibility Scanner**:
- etiqueta para TalkBack en imágenes y elementos pulsables,
- contraste del texto,
- zonas táctiles de 48 dp,
- elementos pulsables duplicados.

Si encuentra un error, la prueba falla y dice qué elemento es.

| Pantalla | Estado probado |
|---|---|
| Inicio de sesión | Sin conexión (franja amarilla), email mal escrito, contraseña vacía y error del servidor |
| Registro | Nombre vacío, contraseña corta y email ya registrado; y la pantalla de «Revisa tu correo» |
| Perfil | Completo; con error al subir la foto; y sin poder descargarse (Reintentar / Cerrar sesión) |
| Editar perfil | Nombre vacío y error al guardar |
| Galería | Lista con y sin portada y con error; galería vacía de otra persona |
| Álbum | Propio con fotos y una subida fallida |
| Foto | Propia, con el botón Borrar y un error |

La comprobación de zonas táctiles del framework revisa que **todos** los elementos pulsables de cada pantalla midan al menos 48 dp. Estas comprobaciones necesitan Android 14 (API 34) o superior: la CI usa un emulador con API 34, y en uno más antiguo se saltan.

### Login y registro solo con TalkBack

Dos pruebas hacen lo que haría alguien con TalkBack: encuentran cada campo y botón **solo por lo que TalkBack lee en voz alta** («Email», «Contraseña», «Mostrar la contraseña», «Entrar»…), escriben y completan el inicio de sesión y el registro. Si algo no tuviera etiqueta, no se podría encontrar y la prueba fallaría.

### Contraste

Calculado con la fórmula de WCAG 2.1. El mínimo es 4,5:1 para texto normal y 3:1 para texto grande.

| Texto sobre fondo | Colores | Contraste |
|---|---|---|
| Texto principal sobre tarjeta blanca | `#222222` / `#FFFFFF` | 15,9:1 |
| Texto principal sobre el fondo azul claro | `#222222` / `#C5D7E8` | 10,8:1 |
| Texto secundario sobre blanco | `#555555` / `#FFFFFF` | 7,5:1 |
| Texto secundario sobre el fondo | `#555555` / `#C5D7E8` | 5,1:1 |
| Enlaces y nombres sobre blanco | `#0066CC` / `#FFFFFF` | 5,6:1 |
| Títulos sobre blanco | `#004085` / `#FFFFFF` | 10,1:1 |
| Títulos y botones de texto sobre el fondo | `#004085` / `#C5D7E8` | 6,9:1 |
| Botón principal | `#FFFFFF` / `#004085` | 10,1:1 |
| Texto de `RetroButton` | `#555555` / `#F8F9FA` | 7,1:1 |
| Estado del perfil | `#222222` / `#E6F0FA` | 13,8:1 |
| Franja «Sin conexión» | `#222222` / `#FFF4C2` | 14,4:1 |
| Logo de la cabecera (texto grande, 22 sp) | `#FFFFFF` / `#2B82DF` | 3,9:1 ✅ (mínimo 3:1) |
| **Errores sobre el fondo, antes** | `#B3261E` / `#C5D7E8` | **4,4:1 ❌** → [#144](https://github.com/yeray06/Retroconver/issues/144) |
| Errores sobre el fondo, ahora (`RetroErrorRed`) | `#A3211B` / `#C5D7E8` | 5,1:1 ✅ |
| Errores sobre blanco, ahora | `#A3211B` / `#FFFFFF` | 7,5:1 |

### Etiquetas (`contentDescription`)

| Elemento | Lo que lee TalkBack |
|---|---|
| Avatar | «Avatar de Yeray» |
| Imagen de una publicación | «Imagen publicada por Yeray» |
| Miniatura de la galería | «Foto 3 de 12» |
| Foto a pantalla completa | «Foto del álbum Viaje a Londres» |
| Botón de ver la contraseña | «Mostrar la contraseña» / «Ocultar la contraseña» |
| «← Volver» | «Volver atrás» |
| Botón «+» de la cabecera | «Nueva publicación» |
| Portadas de álbum, emojis de la barra y de las pantallas vacías | Nada: son decorativos y el texto de al lado ya lo dice |
| Errores, franja sin conexión y «Revisa tu correo» | Se leen solos al aparecer (`liveRegion`) |

### Zonas táctiles

Todos los botones son de Material 3 (`Button`, `TextButton`) o `RetroButton`, que usan `minimumInteractiveComponentSize()`: miden al menos 48 × 48 dp aunque se vean más pequeños. Las tarjetas de álbum y las miniaturas son mucho mayores.

### Pendiente en un móvil de verdad

Lo automático no sustituye a probarlo con la mano. Hay que hacerlo antes de la entrega final, con la app conectada al servidor:

- [ ] **TalkBack:** registrarse, entrar, editar el perfil, cambiar la foto, crear un álbum, añadir y borrar una foto y cerrar sesión, sin mirar la pantalla.
- [ ] **Accessibility Scanner:** pasarlo por inicio de sesión, registro, perfil, edición, galería, álbum y foto. Apuntar aquí lo que salga.
- [ ] **Letra grande:** con el tamaño de letra al máximo en Ajustes, nada se corta ni se solapa.

## 5. Errores encontrados

| Error | Issue | Estado |
|---|---|---|
| El rojo de error no llega a 4,5:1 sobre el fondo | [#144](https://github.com/yeray06/Retroconver/issues/144) | ✅ Corregido en #143 |
| Los diálogos salen con el fondo lila de Material 3 | [#145](https://github.com/yeray06/Retroconver/issues/145) | ✅ Corregido en #143 |
| Faltan en el repositorio los esquemas de Room 3, 4 y 5 | [#146](https://github.com/yeray06/Retroconver/issues/146) | Pendiente (Yeray) |
| Al salir del álbum mientras se suben fotos, se cancelan las que faltan | [#147](https://github.com/yeray06/Retroconver/issues/147) | Para la entrega final |
| *Confirm email* estaba activado en Supabase: cada cuenta nueva tenía que confirmar el correo | [#110](https://github.com/yeray06/Retroconver/issues/110) | ✅ Desactivado el 13/11/2026 |
