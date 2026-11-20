# Pruebas de Retroconver

Lista de las pruebas y su resultado (E4-12, [#24](https://github.com/yeray06/Retroconver/issues/24), y entrega final): las automáticas, la prueba de seguridad del servidor y la revisión de accesibilidad, diseño y usabilidad. Al final están los errores encontrados, cada uno con su issue y cómo se ha resuelto.

## 1. Cómo se ejecutan

- **En cada Pull Request**, GitHub las pasa todas: compila (también la variante release), ejecuta las unitarias, pasa Android lint y arranca un emulador con Android 14 para las demás. La PR enseña un check verde o rojo (README, *Integración continua*).
- **En Android Studio:** las unitarias, con la tarea `testDebugUnitTest`; las del emulador, con `connectedDebugAndroidTest` y el emulador abierto.

Ninguna prueba usa el servidor de verdad: la red se prueba con **MockWebServer**, un servidor falso que devuelve respuestas preparadas como las de Supabase.

## 2. Pruebas automáticas

**287 pruebas: 189 unitarias y 98 en el emulador.** Resultado: ✅ todas pasan en la CI. Además, `CapturasTest` dibuja 15 pantallas para el README; no es una prueba y solo se ejecuta cuando se piden las capturas.

Android lint: **0 avisos** desde la entrega final ([#156](https://github.com/yeray06/Retroconver/pull/156)).

### Unitarias (sin Android)

| Clase | Qué comprueba | Pruebas | Issue |
|---|---|---|---|
| `ReescaladoTest` | Reescalado a 720p de fotos horizontales, verticales, 16:9, panorámicas, cuadradas y pequeñas; ninguna se pasa ni se deforma; orientaciones EXIF que giran la foto | 9 | #62 |
| `ConvertidoresTest` | Fechas a milisegundos y vuelta para Room | 2 | E3 |
| `ClienteSupabaseTest` | Cliente de red contra MockWebServer: cabeceras con y sin sesión, JSON correcto, login, registro con y sin confirmación de email, subida de imagen, 401, 422, 400, 503, 500 sin cuerpo, JSON inesperado, sin conexión y sin configurar | 18 | #114, #115 |
| `ResultadoRedTest` | Formatos de error de Auth, tablas y Storage; mensaje en español de cada caso; fechas del servidor; configuración | 14 | #115 |
| `SesionRepositoryTest` | Sesión con DataStore real: entrar guarda los tokens y **no la contraseña**, credenciales incorrectas, registro, renovar antes de caducar, 401 → renovar y repetir, renovación rechazada → sesión cerrada, sin red se mantiene, cerrar sesión con y sin red | 11 | #117, #119 |
| `ValidacionAccesoTest` | Formularios de acceso: emails válidos y no válidos, obligatorios, contraseña de 6 o más, límite del nombre | 10 | #118 |
| `UsuarioRepositoryTest` | Perfil en el servidor: usuario de la sesión, editar (PATCH y copia en Room), sin conexión, sin permiso, avatar (subir → apuntar → borrar el anterior), marcha atrás si falla, guardar el tema y la tipografía y que el servidor rechace una que no existe. *(EF)* Buscar personas por nombre sin la propia, sin texto no se pregunta y los comodines se buscan como texto; lista de personas con y sin texto; color de fondo enviado o vacío | 16 | #121, #122, #30, #129, #128, #75 |
| `ConversionesTest` | Perfil del servidor → Room (URL pública del avatar, tema, tipografía, fechas) | 2 | #121 |
| `EstiloPerfilTest` | Temas clásicos y tipografías: al menos 3 temas, claves únicas, valores por defecto con claves desconocidas, el clásico es el de la guía y **contraste de 4,5:1 o más** de todos los textos en cada tema (WCAG). *(EF)* Colores de fondo: al menos 5, claves únicas y uno desconocido es el del tema | 6 | #30, #75 |
| `AmistadesRepositoryTest` | Amistades contra el servidor falso: descargar amigos y solicitudes con sus perfiles, quitar las borradas, amigos de otra persona (solo aceptadas), pedir, aceptar, borrar, solicitud repetida (23505) y sin permiso; relación con otra persona en cada caso | 9 | #129 |
| `GaleriaRepositoryTest` | Álbumes y fotos en el servidor: refrescar, quitar lo borrado, crear álbum, subir foto y marcha atrás, borrar foto propia y no ajena, sin conexión | 9 | #68, #71 |
| `SubidasFotosTest` | Subida en segundo plano: una tanda sigue si una foto no se lee o el servidor la rechaza, se para si caduca la sesión, limpia todas las copias; lo que ve la pantalla (progreso, en cola, último fallo de los últimos 10 minutos) | 11 | #147 |
| `ValidacionGaleriaTest` | Nombre del álbum: obligatorio y de 40 caracteres como mucho | 3 | #69 |
| `ValidacionPerfilTest` | Formulario del perfil (E3) | 6 | #54 |
| `FechaRelativaTest` | Fecha relativa de las publicaciones del muro (David) | 6 | #59 |
| `ValidacionPublicacionTest` | Texto de una publicación (David) | 6 | #60 |
| `ValidacionComentarioTest` | Texto de un comentario: vacío, normal y límite del servidor (David) | 3 | #66 |
| `MensajesRepositoryTest` | Tablón contra el servidor falso: descargar el de un perfil con sus autores, quitar lo borrado sin tocar otros tablones, escribir sin espacios de los bordes, sin permiso, borrar y sin conexión (David) | 6 | #72, #74 |
| `ValidacionMensajeTest` | Texto de un mensaje del tablón: vacío, normal y límite del servidor (David) | 3 | #74 |
| `PublicacionRepositoryTest` | Muro contra el servidor falso (David): descargar con autores, URL pública de la imagen, contador de comentarios, quitar lo borrado, sin conexión, publicar con y sin imagen y marcha atrás, comentar, borrar lo propio y no lo ajeno. *(EF)* Muro de amigos: con autores solo se piden y se enseñan los suyos. «Me gusta»: contador y el mío al descargar, quitar los que ya no están, dar (también si ya estaba, 23505), quitar solo el mío y sin conexión. Responder a un comentario | 29 | #123, #66, #67, #129, #130, #29 |
| `MensajesDirectosRepositoryTest` | *(EF)* Mensajes directos contra el servidor falso: descargar con las dos personas, una conversación por persona con su último mensaje, enviar sin espacios de los bordes, zumbido, sin ser amigos (42501) y sin conexión | 6 | #28 |
| `HilosComentariosTest` | *(EF)* Respuestas: cada comentario con sus respuestas debajo en orden, respuesta a una respuesta en el mismo hilo y respuesta suelta si se borró el comentario | 4 | #29 |

### En el emulador

| Clase | Qué comprueba | Pruebas | Issue |
|---|---|---|---|
| `UsuarioDaoTest` | Perfiles con UUID: guardar, `@Upsert` sin duplicar ni borrar sus publicaciones, editar sin tocar el avatar y al revés | 8 | #121 |
| `PublicacionDaoTest` | Orden, comentarios, cascadas, UUID y `@Upsert` de lo que llega del servidor; sincronizar quitando lo borrado, también los comentarios (David). *(EF)* Contador de «Me gusta» y el mío, una persona cuenta una vez, sincronizar y cascada; respuesta guardada | 15 | #58, #121, #123 |
| `GaleriaDaoTest` | Álbumes con número de fotos y portada, orden, cascada, sincronizar quitando lo borrado sin tocar lo ajeno | 8 | #68 |
| `AlmacenImagenesTest` | Fotos reales de 12 MP a 720p, no agrandar, girar según EXIF, nombres distintos, borrar solo lo propio, medidas y borrar todo al cerrar sesión | 8 | #63, #119 |
| `AmistadDaoTest` | Amigos por los dos lados y por orden alfabético, solicitudes recibidas y enviadas con la otra persona, relación entre dos personas, sincronizar sin tocar lo ajeno, cascada al borrar un perfil | 6 | #129 |
| `MensajeDaoTest` | Tablón con sus autores del más reciente al más antiguo, `@Upsert` sin duplicar, sincronizar sin tocar otros tablones y cascada al borrar un perfil (David) | 4 | #72 |
| `MigracionesTest` | Migraciones de Room: de 5 a 6, de 6 a 7, de 7 a 8 (tablón) y de 8 a 9 (extras) conservando los datos, la app abriendo una copia de la versión 5 y una de la 4 (sin servidor) que se crea de nuevo | 6 | Entrega final, #72 |
| `AccesibilidadTest` | Revisión de accesibilidad automática (apartado 4), también de cada tema del perfil, el selector de aspecto, las solicitudes de amistad, el botón de amistad y el buscador de amigos en cada estado. *(EF)* Muro y detalle de una publicación, tablón (propio, ajeno y en el tema Noche), Mensajes, Personas, perfil de otra persona y color de fondo; tocar el autor o una persona abre su perfil y cada «Borrar» del tablón dice de quién es el mensaje (David). «Me gusta» en el muro (pulsar el ♥ de una publicación), respuestas en el detalle, conversaciones privadas y chat con zumbido y error | 43 | #78, #30, #129, #80 |

## 3. Servidor

| Prueba | Cómo | Resultado |
|---|---|---|
| Políticas de seguridad (RLS) | `supabase/pruebas_seguridad.sql` en el SQL Editor del proyecto: crea dos cuentas temporales, hace 29 comprobaciones y las borra | ✅ 29/29 el 20/11/2026 con los extras, sin restos (22/22 el 16/11; en la entrega 4 eran 12/12, [#136](https://github.com/yeray06/Retroconver/pull/136)) |

Las comprobaciones de la entrega 4:
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

Las 10 nuevas de la entrega final (EX-03 y EX-04):
1. Se puede cambiar el tema y la tipografía del perfil propio.
2. El servidor rechaza una tipografía que no existe.
3. No se puede crear una amistad ya aceptada.
4. Nadie puede pedirse amistad a sí mismo.
5. Se puede pedir amistad a otra persona.
6. Quien pide la amistad no puede aceptarla.
7. Solo puede haber una solicitud entre dos personas.
8. Quien recibe la solicitud no puede cambiar quién la pidió.
9. Quien recibe la solicitud puede aceptarla.
10. Sin sesión no se ve ninguna amistad.

Las 7 de los extras (EX-01 y EX-05):
1. Sin ser amigos no se pueden enviar mensajes directos.
2. Se puede dar «Me gusta» a una publicación de otra persona.
3. No se puede dar «Me gusta» en nombre de otra persona.
4. Siendo amigos se puede enviar un mensaje directo (un zumbido).
5. Quien recibe un mensaje directo lo ve.
6. Nadie puede quitar el «Me gusta» de otra persona.
7. Sin sesión no se ve ningún mensaje directo.

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
| *(EF)* Muro | Con publicaciones de dos personas y el autor que se puede tocar; vacío y con error |
| *(EF)* Detalle de una publicación | Propia, con un comentario y el formulario para comentar |
| *(EF)* Tablón | Propio con mensajes, uno borrándose y un error; vacío dentro de un perfil con el tema Noche |
| *(EF)* Mensajes | El tablón propio con un mensaje |
| *(EF)* Personas | Lista con el buscador |
| *(EF)* Perfil de otra persona | Con tema Lima, fondo Lavanda, amigos, botón de amistad y tablón; sin «Editar perfil» ni «Cerrar sesión» |
| *(EF)* Editar perfil | Con los colores de fondo: cada uno se anuncia con su nombre y como opción marcada o no |
| *(EF)* Muro con «Me gusta» | El ♥ con contador se puede pulsar (48 dp) y TalkBack dice cuántos hay y si da o quita el «Me gusta» |
| *(EF)* Detalle con respuestas | Comentario con su respuesta sangrada, «Responder» que dice a quién, y el campo «Respondiendo a Ana» |
| *(EF)* Mensajes y chat | Conversaciones privadas con el último mensaje; chat con mensajes de los dos, un zumbido, el aviso del zumbido y el error de «solo amigos»; chat vacío |

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
| *(EF)* Botón «👥» de la cabecera | «Buscar personas» |
| *(EF)* Autor de una publicación | «Ana», con la acción «Ver el perfil de Ana» |
| *(EF)* «Borrar» de un mensaje del tablón | «Borrar el mensaje de Ana» |
| *(EF)* Cada color de fondo | Su nombre («Verde menta») y si está marcado |
| Portadas de álbum, emojis de la barra y de las pantallas vacías | Nada: son decorativos y el texto de al lado ya lo dice |
| Errores, franja sin conexión y «Revisa tu correo» | Se leen solos al aparecer (`liveRegion`) |

### Zonas táctiles

Todos los botones son de Material 3 (`Button`, `TextButton`) o `RetroButton`, que usan `minimumInteractiveComponentSize()`: miden al menos 48 × 48 dp aunque se vean más pequeños. Las tarjetas de álbum y las miniaturas son mucho mayores.

### Pendiente en un móvil de verdad

Lo automático no sustituye a probarlo con la mano. Hay que hacerlo antes de la entrega final, con la app conectada al servidor:

- [ ] **TalkBack:** registrarse, entrar, editar el perfil, cambiar la foto, crear un álbum, añadir y borrar una foto y cerrar sesión, sin mirar la pantalla.
- [ ] **Accessibility Scanner:** pasarlo por inicio de sesión, registro, perfil, edición, galería, álbum y foto. Apuntar aquí lo que salga.
- [ ] **Letra grande:** con el tamaño de letra al máximo en Ajustes, nada se corta ni se solapa.

## 5. Revisión de diseño y usabilidad

### Qué se ha comprobado

- **Diseño:** cada pantalla nueva de la entrega 4 (inicio de sesión, registro, galería, álbum y foto) se ha comparado con la guía de estilo ([docs/estilo.md](estilo.md)) y con los bocetos (`docs/diseño/`).
  - Todas usan las mismas piezas: la cabecera azul con brillo (`RetroHeader`), las tarjetas blancas con borde (`RetroCard`), los botones Web 2.0 (`RetroButton`) y los colores del tema.
  - La galería sigue el boceto de [docs/diseño/galeria.md](diseño/galeria.md): portadas con borde fino, cuadrícula de 3 columnas y cabecera con «← Volver» y el título del álbum.
- **Entrega final:** lo mismo con el aspecto del perfil (temas y tipografías) y las amistades. Los temas siguen las paletas de la época (azul Tuenti, oscuro MySpace, verde MSN y rosa fotolog) con las mismas piezas de la guía de estilo.
- **Usabilidad:** se ha recorrido cada flujo (registrarse, entrar, editar el perfil, cambiar la foto, crear un álbum, añadir y borrar fotos, cerrar sesión) buscando dónde una persona se puede quedar atascada, perder lo que ha escrito o hacer algo sin querer. También se ha probado sin conexión y con la sesión caducada.

### Mejoras aplicadas

| Mejora | Por qué |
|---|---|
| Los errores de cada campo salen debajo del campo al pulsar el botón, y desaparecen al corregirlo | Se ve qué falla y dónde, sin un mensaje genérico |
| Los errores del servidor se traducen al español y dicen qué hacer: «Ya hay una cuenta con ese email. Inicia sesión», «La imagen es demasiado grande. Prueba con otra»… | Nadie entiende «Error 422» |
| Franja amarilla de «Sin conexión» bajo la cabecera, como los avisos de Tuenti | Se sigue viendo lo último guardado y está claro por qué no se puede publicar |
| Sin conexión, los formularios no se cierran ni pierden lo escrito | No hay que volver a escribirlo |
| Cerrar sesión y borrar una foto piden confirmación | Son acciones que no se pueden deshacer |
| Los botones cambian de texto y se desactivan mientras esperan al servidor («Entrando…», «Creando…», «Borrando…») | Se sabe que algo está pasando y no se envía dos veces |
| Pantallas vacías que explican qué hacer: «Crea uno con “Nuevo álbum”…», «Pulsa “Añadir fotos” para llenarlo» | Una lista vacía sin texto parece un error |
| Al crear un álbum, se abre directamente para añadir fotos | Un paso menos en lo que se va a hacer siempre |
| Se pueden elegir hasta 10 fotos de una vez; se suben en segundo plano y se ve el progreso («Subiendo 2 de 5…») | No hay que esperar mirando la pantalla (#147) |
| Deslizar hacia abajo para actualizar la galería | El gesto que todo el mundo conoce |
| Botón «Ver» en la contraseña | Comprobar lo escrito en el teclado del móvil |
| La sesión se mantiene al cerrar la app, y después de cerrar sesión «Atrás» no vuelve al muro | No hay que entrar cada vez, y en un móvil compartido no se ve la cuenta anterior |
| En los álbumes de otra persona no salen «Nuevo álbum», «Añadir fotos» ni «Borrar» | No se ofrece lo que no se puede hacer |
| Los diálogos son blancos como las tarjetas, en vez del lila de Material 3 (#145) | Coherencia con el estilo retro |
| El formulario de acceso no pasa de 480 dp de ancho | En tablet y en horizontal no se estira de lado a lado |
| *(EF)* Los cuatro temas del perfil se comprueban solos: una prueba calcula el contraste de cada texto sobre su fondo | Un tema nuevo con poco contraste rompe la CI, no llega a la app |
| *(EF)* El aspecto del perfil tiene vista previa antes de guardar, y cada tipografía se ve escrita con su letra | Se elige viendo el resultado, sin guardar y volver atrás para probar |
| *(EF)* Las solicitudes de amistad van arriba del perfil, antes de la lista de amigos | Es lo único que hay que contestar |
| *(EF)* Cada botón de una solicitud dice a TalkBack de quién es («Aceptar la solicitud de Bea») | Con varias solicitudes, «Aceptar» repetido no dice cuál |
| *(EF)* «Buscar amigos» en el perfil propio: se busca por nombre y se pide amistad desde la lista, sin entrar en cada perfil; cada «Añadir» dice a quién («Pedir amistad a Ana») | El muro solo enseña a los amigos, así que hacerlos tiene que ser fácil |
| *(EF)* «Dejar de ser amigos» pide confirmación; cancelar una solicitud no | Lo primero no se deshace con un toque; lo segundo sí (se vuelve a pedir) |
| *(EF)* Tocar el avatar o el nombre de quien publica abre su perfil; tocar el resto de la tarjeta abre la publicación | Es lo que se espera de una red social, y TalkBack los anuncia como dos botones distintos |
| *(EF)* El perfil de otra persona es el mismo diseño que el propio, con su tema y su fondo, pero sin «Editar perfil» ni «Cerrar sesión» | No se ofrece lo que no se puede hacer; si es el propio, se abre el propio con sus botones |
| *(EF)* Borrar un mensaje del tablón pide confirmación, y solo sale en los mensajes que se pueden borrar | No se deshace, y no se ofrece en mensajes ajenos |
| *(EF)* El perfil propio, el de otra persona y el tablón se descargan otra vez cada vez que se vuelve a la pantalla (al cambiar de sección, al volver atrás o al volver a la app) | La barra inferior conserva cada sección: sin esto, las solicitudes de amistad y los mensajes nuevos no salían hasta cerrar la app |
| *(EF)* El campo del tablón usa los colores del tema del perfil y tiene contador de caracteres | En el tema Noche el texto se ve sobre la tarjeta oscura, y se sabe cuánto queda |
| *(EF)* Los colores de fondo son cuadrados de 48 dp con el nombre para TalkBack | Se eligen viendo el color, y se pueden pulsar y oír igual que el resto de opciones |
| *(EF)* El ♥ de «Me gusta» es un botón aparte dentro de la tarjeta, y TalkBack lo lee como «3 Me gusta, Quitar el Me gusta» | Se puede dar sin abrir la publicación, y se oye cuántos hay y qué hace |
| *(EF)* Al responder, encima del campo pone «Respondiendo a Ana» con **Cancelar respuesta** | Queda claro a quién va la respuesta y se puede volver atrás |
| *(EF)* El chat pone lo mío a la derecha y lo suyo a la izquierda, baja solo al último mensaje y pregunta al servidor cada 4 segundos mientras está abierto | Se lee como un chat de verdad sin tener que actualizar a mano |
| *(EF)* El zumbido hace temblar el chat, vibra y además escribe «Ana te ha enviado un zumbido» | Quien no ve el temblor (TalkBack) también se entera |
| *(EF)* Si no sois amigos, el chat dice «Solo puedes escribir a tus amigos…» en vez de «Sin permiso» | Dice qué hacer |
| *(EF)* «No se ha podido subir la foto» con una sola foto, en vez de «1 de 1 fotos» | Texto natural en los dos casos (plural de Android) |

### Pendiente
- Probarlo con dos o tres compañeros que no conozcan la app y apuntar dónde dudan.

## 6. Errores encontrados

| Error | Issue | Estado |
|---|---|---|
| El rojo de error no llega a 4,5:1 sobre el fondo | [#144](https://github.com/yeray06/Retroconver/issues/144) | ✅ Corregido en #143 |
| Los diálogos salen con el fondo lila de Material 3 | [#145](https://github.com/yeray06/Retroconver/issues/145) | ✅ Corregido en #143 |
| Faltan en el repositorio los esquemas de Room 3, 4 y 5 | [#146](https://github.com/yeray06/Retroconver/issues/146) | ✅ Generados por la CI desde el código de cada versión ([#150](https://github.com/yeray06/Retroconver/pull/150)) |
| Al salir del álbum mientras se suben fotos, se cancelan las que faltan | [#147](https://github.com/yeray06/Retroconver/issues/147) | ✅ Subida en segundo plano con WorkManager |
| *Confirm email* estaba activado en Supabase: cada cuenta nueva tenía que confirmar el correo | [#110](https://github.com/yeray06/Retroconver/issues/110) | ✅ Desactivado el 13/11/2026 |
| Quien recibía una solicitud de amistad podía cambiar quién la había pedido. Supabase da todos los permisos a las tablas nuevas, así que el permiso solo sobre la columna `aceptada` no limitaba nada. Lo encontró la prueba de seguridad | [#155](https://github.com/yeray06/Retroconver/pull/155) | ✅ `revoke all` antes de dar los permisos; la comprobación 8 lo vigila |
| Al actualizar la app con un esquema nuevo se borraba la copia local | Memoria v4, apartado 17 | ✅ Migraciones de Room con su prueba ([#154](https://github.com/yeray06/Retroconver/pull/154)) |
| 18 avisos de Android lint (ExifInterface del sistema, sin icono temático, plurales, `Uri.parse`, «momento» como errata) | [#156](https://github.com/yeray06/Retroconver/pull/156) | ✅ 0 avisos |
| Con una sola foto fallida, el aviso decía «No se han podido subir 1 de 1 fotos» | [#156](https://github.com/yeray06/Retroconver/pull/156) | ✅ Plural: «No se ha podido subir la foto» |
