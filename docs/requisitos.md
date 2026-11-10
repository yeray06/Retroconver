# Requisitos de Retroconver

Requisitos de la primera versión, agrupados por módulo. Cada requisito enlaza a la issue que lo implementa; si la implementan varias subtareas, se enlazan todas.

**Prioridad:** *Imprescindible* entra en la primera versión. *Extra* solo se hace si hay tiempo.

> **Cambio de alcance en la entrega 4** (E4-01, [#109](https://github.com/yeray06/Retroconver/issues/109)). Hasta la entrega 3 todo se guardaba en el móvil y había un único usuario local. Así no era una red social: nadie más podía comentar ni escribir en tu tablón. Desde la entrega 4 hay cuentas de verdad y los datos se comparten en un servidor **Supabase** (plan gratuito). El móvil guarda una copia en Room para funcionar sin conexión. Los requisitos afectados están marcados con *(E4)*.

## Requisitos funcionales

### General y navegación

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-01 | La persona usuaria puede moverse entre Muro, Perfil, Galería y Mensajes con una barra de navegación inferior que marca la sección actual. | Imprescindible | E2-02.1 [#36](https://github.com/yeray06/Retroconver/issues/36), E2-02.2 [#37](https://github.com/yeray06/Retroconver/issues/37) |
| RF-02 | *(E4)* Lo último descargado del servidor se guarda en el móvil y se puede consultar sin conexión. | Imprescindible | E4-05 [#120](https://github.com/yeray06/Retroconver/issues/120) |
| RF-03 | Todas las pantallas comparten la estética retro: colores, tipografías y componentes de la guía de estilo. | Imprescindible | E2-06 [#12](https://github.com/yeray06/Retroconver/issues/12) |

### Perfil

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-04 | *(E4)* La persona usuaria se registra con nombre, email y contraseña, e inicia sesión. La sesión se mantiene al cerrar la app. En la entrega 3 había un usuario local automático (E3-01.1), que se sustituye por las cuentas. | Imprescindible | E4-04.1 [#117](https://github.com/yeray06/Retroconver/issues/117), E4-04.2 [#118](https://github.com/yeray06/Retroconver/issues/118) |
| RF-25 | *(E4)* Puede cerrar sesión. Al hacerlo no quedan sus datos en el móvil. | Imprescindible | E4-04.3 [#119](https://github.com/yeray06/Retroconver/issues/119) |
| RF-05 | La persona usuaria ve su perfil con avatar, nombre, biografía y estado. | Imprescindible | E3-06.1 [#53](https://github.com/yeray06/Retroconver/issues/53) |
| RF-06 | Puede editar nombre, biografía y estado. El nombre es obligatorio y los campos tienen un límite de caracteres. *(E4)* Los cambios se guardan en el servidor. | Imprescindible | E3-06.2 [#54](https://github.com/yeray06/Retroconver/issues/54), E4-05.2 [#122](https://github.com/yeray06/Retroconver/issues/122) |
| RF-07 | Puede cambiar el avatar eligiendo una foto del dispositivo. *(E4)* El avatar se sube al servidor y lo ven los demás. | Imprescindible | E3-06.3 [#55](https://github.com/yeray06/Retroconver/issues/55), E4-05.2 [#122](https://github.com/yeray06/Retroconver/issues/122) |
| RF-08 | *(E4)* Puede elegir el color de fondo y el tema de su perfil. Quien visita el perfil lo ve así. | Imprescindible | E4-11.1 [#75](https://github.com/yeray06/Retroconver/issues/75), E4-11.2 [#76](https://github.com/yeray06/Retroconver/issues/76) |
| RF-26 | *(E4)* Puede ver el perfil de otra persona (datos, tablón y álbumes) tocando su avatar o su nombre. | Imprescindible | E4-08.1 [#127](https://github.com/yeray06/Retroconver/issues/127) |
| RF-27 | *(E4)* Puede ver la lista de personas de la red y buscarlas por nombre. | Imprescindible | E4-08.2 [#128](https://github.com/yeray06/Retroconver/issues/128) |
| RF-31 | *(E4)* Puede enviar y aceptar solicitudes de amistad, y el muro muestra solo a sus amigos. | Extra | EX-04 [#129](https://github.com/yeray06/Retroconver/issues/129) |

### Muro

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-09 | El muro muestra las publicaciones de la más reciente a la más antigua, con avatar, nombre, fecha, texto e imagen. *(E4)* Son las publicaciones de todas las personas de la red. | Imprescindible | E3-03.1 [#58](https://github.com/yeray06/Retroconver/issues/58), E3-03.2 [#59](https://github.com/yeray06/Retroconver/issues/59), E4-06.2 [#125](https://github.com/yeray06/Retroconver/issues/125) |
| RF-10 | Si no hay publicaciones, el muro muestra un mensaje que invita a publicar. | Imprescindible | E3-03.2 [#59](https://github.com/yeray06/Retroconver/issues/59) |
| RF-28 | *(E4)* Puede actualizar el muro deslizando hacia abajo. | Imprescindible | E4-06.2 [#125](https://github.com/yeray06/Retroconver/issues/125) |
| RF-11 | La persona usuaria puede publicar un texto. No se permite publicar un texto vacío. | Imprescindible | E3-07.1 [#60](https://github.com/yeray06/Retroconver/issues/60), E4-06.1 [#124](https://github.com/yeray06/Retroconver/issues/124) |
| RF-12 | Puede adjuntar una imagen opcional a la publicación, verla antes de publicar y quitarla. | Imprescindible | E3-07.2 [#61](https://github.com/yeray06/Retroconver/issues/61), E4-06.1 [#124](https://github.com/yeray06/Retroconver/issues/124) |
| RF-29 | *(E4)* Puede borrar sus propias publicaciones, con su imagen. | Imprescindible | E4-06.1 [#124](https://github.com/yeray06/Retroconver/issues/124) |
| RF-13 | Puede abrir una publicación y ver sus comentarios por orden de fecha. | Imprescindible | E4-07.1 [#66](https://github.com/yeray06/Retroconver/issues/66) |
| RF-14 | Puede comentar cualquier publicación. La tarjeta del muro muestra cuántos comentarios tiene. | Imprescindible | E4-07.2 [#67](https://github.com/yeray06/Retroconver/issues/67) |
| RF-15 | Puede responder a un comentario concreto. | Extra | EX-02 [#29](https://github.com/yeray06/Retroconver/issues/29) |
| RF-30 | *(E4)* Puede dar «Me gusta» a una publicación, y la tarjeta muestra el contador. | Extra | EX-05 [#130](https://github.com/yeray06/Retroconver/issues/130) |

### Galería

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-16 | La persona usuaria puede elegir imágenes del dispositivo. La app solo pide permisos cuando la versión de Android lo exige. | Imprescindible | E3-04 [#19](https://github.com/yeray06/Retroconver/issues/19) |
| RF-17 | Toda imagen que entra en la app se reduce a un máximo de 1280 × 720 (720 × 1280 en vertical), manteniendo la proporción. *(E4)* Es la versión reducida la que se sube al servidor. | Imprescindible | E3-05.1 [#62](https://github.com/yeray06/Retroconver/issues/62), E3-05.2 [#63](https://github.com/yeray06/Retroconver/issues/63) |
| RF-18 | Puede crear álbumes con nombre y verlos en una lista con portada y número de fotos. | Imprescindible | E4-10.2 [#69](https://github.com/yeray06/Retroconver/issues/69) |
| RF-19 | Puede ver las fotos de un álbum en cuadrícula y abrir cada una a pantalla completa. *(E4)* También los álbumes de otras personas, desde su perfil. | Imprescindible | E4-10.3 [#70](https://github.com/yeray06/Retroconver/issues/70) |
| RF-20 | Puede añadir fotos a sus álbumes y borrarlas con confirmación. | Imprescindible | E4-10.4 [#71](https://github.com/yeray06/Retroconver/issues/71) |

### Mensajes

| ID | Requisito | Prioridad | Issue |
|---|---|---|---|
| RF-21 | Cada perfil tiene un tablón con los mensajes recibidos, del más reciente al más antiguo. | Imprescindible | E4-09.2 [#73](https://github.com/yeray06/Retroconver/issues/73) |
| RF-22 | *(E4)* La persona usuaria puede escribir en el tablón de cualquier persona, también en el suyo. No se permite un mensaje vacío. | Imprescindible | E4-09.3 [#74](https://github.com/yeray06/Retroconver/issues/74) |
| RF-23 | *(E4)* Puede borrar, tras confirmarlo, los mensajes que ha escrito y cualquier mensaje de su propio tablón. | Imprescindible | E4-09.3 [#74](https://github.com/yeray06/Retroconver/issues/74) |
| RF-24 | Puede mantener conversaciones privadas con otras personas de la red, con zumbido como en MSN. | Extra | EX-01 [#28](https://github.com/yeray06/Retroconver/issues/28) |

## Requisitos no funcionales

| ID | Requisito | Issue |
|---|---|---|
| RNF-01 | *(E4)* Los datos están en un servidor Supabase. Cada persona solo puede crear, cambiar o borrar lo suyo, también si alguien usa la clave pública fuera de la app (políticas de seguridad RLS). | E4-02.2 [#112](https://github.com/yeray06/Retroconver/issues/112) |
| RNF-02 | Ninguna imagen guardada supera los 720p, para ahorrar espacio y mantener la estética de la época. | E3-05 [#20](https://github.com/yeray06/Retroconver/issues/20) |
| RNF-03 | Las imágenes y los botones de icono tienen descripción para TalkBack, el texto tiene contraste suficiente y las zonas táctiles miden al menos 48 dp. | E4-12.2 [#78](https://github.com/yeray06/Retroconver/issues/78), E4-13.2 [#80](https://github.com/yeray06/Retroconver/issues/80) |
| RNF-04 | La lógica de reescalado, los DAOs, la sesión y el cliente de red tienen pruebas automáticas. | E4-12.1 [#77](https://github.com/yeray06/Retroconver/issues/77), E4-13.1 [#79](https://github.com/yeray06/Retroconver/issues/79) |
| RNF-05 | La app está en español y funciona en Android 8.0 (API 26) o superior. | E2-01 [#7](https://github.com/yeray06/Retroconver/issues/7) |
| RNF-06 | *(E4)* Sin conexión, la app muestra la última copia guardada en Room y avisa de que no puede publicar ni comentar. No se cierra por falta de red. | E4-03.2 [#115](https://github.com/yeray06/Retroconver/issues/115), E4-05 [#120](https://github.com/yeray06/Retroconver/issues/120) |
| RNF-07 | *(E4)* La URL y la clave del servidor no se suben al repositorio. El móvil guarda los tokens de sesión, nunca la contraseña. | E4-03.1 [#114](https://github.com/yeray06/Retroconver/issues/114), E4-04.1 [#117](https://github.com/yeray06/Retroconver/issues/117) |
| RNF-08 | *(E4)* Coste cero: el servidor funciona con el plan gratuito de Supabase (500 MB de base de datos y 1 GB de imágenes), sin tarjeta. | E4-02 [#110](https://github.com/yeray06/Retroconver/issues/110) |

## Decisiones sobre los bocetos

Los bocetos (E2-04) incluían cosas que no estaban en el alcance. Se decidieron al pasar a servidor:

| En los bocetos | Decisión |
|---|---|
| Pantalla de inicio de sesión (`docs/diseño/recorrido.md`) | **Sí.** Cuentas de verdad con Supabase Auth: registro, inicio de sesión y cierre de sesión (E4-04) |
| Pestaña «Chats» con conversaciones privadas y zumbidos | La pestaña Mensajes es el **tablón** del perfil (E4-09). El chat privado con zumbido queda como extra (EX-01) |
| Botón «Me gusta» con contador | **Extra** (EX-05) |
| Contactos y publicaciones de amigos | Con servidor, el muro muestra a **todas las personas de la red** (E4-06). Las amistades quedan como extra (EX-04) |
