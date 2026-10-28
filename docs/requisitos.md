# Requisitos de Retroconver

Requisitos de la primera versión, agrupados por módulo. Cada requisito enlaza a la issue que lo implementa; si la implementan varias subtareas, se enlazan todas.

**Prioridad:** *Imprescindible* entra en la primera versión. *Extra* solo se hace si hay tiempo.

## Requisitos funcionales

### General y navegación

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-01 | La persona usuaria puede moverse entre Muro, Perfil, Galería y Mensajes con una barra de navegación inferior que marca la sección actual. | Imprescindible | E2-02.1 [#36](https://github.com/yeray06/Retroconver/issues/36), E2-02.2 [#37](https://github.com/yeray06/Retroconver/issues/37) |
| RF-02 | Toda la información se guarda en el dispositivo y está disponible sin conexión a internet. | Imprescindible | E2-03 [#9](https://github.com/yeray06/Retroconver/issues/9) |
| RF-03 | Todas las pantallas comparten la estética retro: colores, tipografías y componentes de la guía de estilo. | Imprescindible | E2-06 [#12](https://github.com/yeray06/Retroconver/issues/12) |

### Perfil

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-04 | Al abrir la app por primera vez se crea automáticamente un usuario local. No hay registro ni inicio de sesión. | Imprescindible | E3-01.1 [#52](https://github.com/yeray06/Retroconver/issues/52) |
| RF-05 | La persona usuaria ve su perfil con avatar, nombre, biografía y estado. | Imprescindible | E3-06.1 [#53](https://github.com/yeray06/Retroconver/issues/53) |
| RF-06 | Puede editar nombre, biografía y estado. El nombre es obligatorio y los campos tienen un límite de caracteres. | Imprescindible | E3-06.2 [#54](https://github.com/yeray06/Retroconver/issues/54) |
| RF-07 | Puede cambiar el avatar eligiendo una foto del dispositivo. | Imprescindible | E3-06.3 [#55](https://github.com/yeray06/Retroconver/issues/55) |
| RF-08 | Puede elegir el color de fondo y el tema de su perfil, y la elección se mantiene al cerrar la app. | Imprescindible | E4-04.1 [#75](https://github.com/yeray06/Retroconver/issues/75), E4-04.2 [#76](https://github.com/yeray06/Retroconver/issues/76) |

### Muro

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-09 | El muro muestra las publicaciones de la más reciente a la más antigua, con avatar, nombre, fecha, texto e imagen. | Imprescindible | E3-03.1 [#58](https://github.com/yeray06/Retroconver/issues/58), E3-03.2 [#59](https://github.com/yeray06/Retroconver/issues/59) |
| RF-10 | Si no hay publicaciones, el muro muestra un mensaje que invita a publicar. | Imprescindible | E3-03.2 [#59](https://github.com/yeray06/Retroconver/issues/59) |
| RF-11 | La persona usuaria puede publicar un texto. No se permite publicar un texto vacío. | Imprescindible | E3-07.1 [#60](https://github.com/yeray06/Retroconver/issues/60) |
| RF-12 | Puede adjuntar una imagen opcional a la publicación, verla antes de publicar y quitarla. | Imprescindible | E3-07.2 [#61](https://github.com/yeray06/Retroconver/issues/61) |
| RF-13 | Puede abrir una publicación y ver sus comentarios por orden de fecha. | Imprescindible | E4-01.1 [#66](https://github.com/yeray06/Retroconver/issues/66) |
| RF-14 | Puede comentar una publicación. La tarjeta del muro muestra cuántos comentarios tiene. | Imprescindible | E4-01.2 [#67](https://github.com/yeray06/Retroconver/issues/67) |
| RF-15 | Puede responder a un comentario concreto. | Extra | EX-02 [#29](https://github.com/yeray06/Retroconver/issues/29) |

### Galería

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-16 | La persona usuaria puede elegir imágenes del dispositivo. La app solo pide permisos cuando la versión de Android lo exige. | Imprescindible | E3-04 [#19](https://github.com/yeray06/Retroconver/issues/19) |
| RF-17 | Toda imagen que entra en la app se reduce a un máximo de 1280 × 720 (720 × 1280 en vertical), manteniendo la proporción, y se guarda una copia dentro de la app. | Imprescindible | E3-05.1 [#62](https://github.com/yeray06/Retroconver/issues/62), E3-05.2 [#63](https://github.com/yeray06/Retroconver/issues/63) |
| RF-18 | Puede crear álbumes con nombre y verlos en una lista con portada y número de fotos. | Imprescindible | E4-02.2 [#69](https://github.com/yeray06/Retroconver/issues/69) |
| RF-19 | Puede ver las fotos de un álbum en cuadrícula y abrir cada una a pantalla completa. | Imprescindible | E4-02.3 [#70](https://github.com/yeray06/Retroconver/issues/70) |
| RF-20 | Puede añadir fotos a un álbum y borrarlas con confirmación. | Imprescindible | E4-02.4 [#71](https://github.com/yeray06/Retroconver/issues/71) |

### Mensajes

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-21 | El perfil tiene un tablón con los mensajes recibidos, del más reciente al más antiguo. | Imprescindible | E4-03.2 [#73](https://github.com/yeray06/Retroconver/issues/73) |
| RF-22 | La persona usuaria puede escribir un mensaje en el tablón. No se permite un mensaje vacío. | Imprescindible | E4-03.3 [#74](https://github.com/yeray06/Retroconver/issues/74) |
| RF-23 | Puede borrar un mensaje del tablón tras confirmarlo. | Imprescindible | E4-03.3 [#74](https://github.com/yeray06/Retroconver/issues/74) |
| RF-24 | Puede mantener conversaciones privadas con otros usuarios locales. | Extra | EX-01 [#28](https://github.com/yeray06/Retroconver/issues/28) |

## Requisitos no funcionales

| ID | Requisito | Issue |
|---|---|---|
| RNF-01 | La app funciona sin conexión: no usa servidor, cuentas en la nube ni sincronización. | E2-03 [#9](https://github.com/yeray06/Retroconver/issues/9) |
| RNF-02 | Ninguna imagen guardada supera los 720p, para ahorrar espacio y mantener la estética de la época. | E3-05 [#20](https://github.com/yeray06/Retroconver/issues/20) |
| RNF-03 | Las imágenes y los botones de icono tienen descripción para TalkBack, el texto tiene contraste suficiente y las zonas táctiles miden al menos 48 dp. | E4-05.2 [#78](https://github.com/yeray06/Retroconver/issues/78), E4-06.2 [#80](https://github.com/yeray06/Retroconver/issues/80) |
| RNF-04 | La lógica de reescalado y los DAOs de todos los módulos tienen pruebas automáticas. | E4-05.1 [#77](https://github.com/yeray06/Retroconver/issues/77), E4-06.1 [#79](https://github.com/yeray06/Retroconver/issues/79) |
| RNF-05 | La app está en español y funciona en Android 8.0 (API 26) o superior. | E2-01 [#7](https://github.com/yeray06/Retroconver/issues/7) |

## Puntos a decidir entre los dos

Los bocetos (E2-04) incluyen cosas que no están en el alcance acordado. Hay que decidir si se añaden como requisito o se quitan del diseño:

| En los bocetos | En el alcance | Propuesta |
|---|---|---|
| Pantalla de inicio de sesión (`docs/diseño/recorrido.md`) | No hay cuentas: usuario local automático (RF-04) | Quitar el login de los bocetos |
| Pestaña «Chats» con conversaciones privadas y zumbidos | Mensajes = tablón del perfil. El chat privado es el extra RF-24 | Que la pestaña Mensajes muestre el tablón y dejar el chat para si hay tiempo |
| Botón «Me gusta» con contador en las publicaciones | No aparece | Añadirlo como extra o quitarlo de los bocetos |
