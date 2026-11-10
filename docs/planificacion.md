# Planificación

Reparto por módulos: cada integrante se encarga de sus módulos de principio a fin (datos, pantallas y pruebas) y revisa las Pull Requests del otro.

| Integrante | Módulos |
|---|---|
| Yeray ([@yeray06](https://github.com/yeray06)) | Organización del repo, planificación, base del proyecto, navegación, Room, servidor Supabase, cliente de red, registro y sesión, **Perfil** y **Galería** |
| David ([@DavidNC26](https://github.com/DavidNC26)) | README, memoria, guía de estilo y tema retro, bocetos, esquema de datos, perfiles de otras personas, personalización, **Muro** y **Mensajes** |

El trabajo está dividido en **104 issues**: 46 tareas principales y 58 subtareas. Las subtareas son *sub-issues* de GitHub, así que el tablero muestra el progreso de cada tarea principal. Las dependencias están marcadas en cada issue como *Blocked by*.

> **Replanificación de la entrega 4.** Al pasar a red social con servidor (E4-01, [#109](https://github.com/yeray06/Retroconver/issues/109)) se añadieron 20 tareas de servidor, sesión, copia local y perfiles ajenos, y las tareas de la entrega 4 que ya existían se renumeraron y se adaptaron al servidor. También se añadieron dos extras (EX-04 y EX-05).

### Código de orden

Cada issue empieza por un código que indica en qué orden se avanza:

| Código | Significado |
|---|---|
| `E2-01` | Primera tarea principal de la entrega 2 |
| `E2-01.1` | Primera subtarea de `E2-01` |
| `EF-01` | Primera tarea de la entrega final |
| `EX-01` | Primera tarea extra (si hay tiempo) |

Dentro de cada entrega, las tareas están ordenadas respetando sus dependencias: una tarea nunca depende de otra con un código posterior de la misma entrega. El número de GitHub (#7, #34…) es el que se usa en ramas, commits y Pull Requests.

El estado real de cada tarea se sigue en el [tablero Kanban](https://github.com/users/yeray06/projects/2).

## Carga de trabajo

| Entrega | Yeray | David | Total |
|---|---|---|---|
| Entrega 1 · Organización | 3 | 3 | 6 |
| Entrega 2 · Diseño y base técnica | 12 | 12 | 24 |
| Entrega 3 · Flujo principal | 11 | 10 | 21 |
| Entrega 4 · Versión candidata | 23 | 20 | 43 |
| Entrega final | 2 | 3 | 5 |
| Si hay tiempo (extras) | 2 | 3 | 5 |
| **Total** | **53** | **51** | **104** |

En la entrega 4 Yeray tiene más tareas porque monta la base del servidor (Supabase, cliente de red, sesión y copia local), que necesitan los módulos de los dos. Para equilibrarlo, la personalización del perfil y los perfiles de otras personas pasan a David.

## Tareas por entrega

Las tareas están en orden de avance. Las subtareas aparecen con ↳ debajo de su tarea principal y las dependencias se indican con su código.

### Entrega 1 · Organización · hasta el 25/10/2026

| Código | Issue | Tarea | Responsable | Depende de |
|---|---|---|---|---|
| **E1-01** | [#1](https://github.com/yeray06/Retroconver/issues/1) | **Configurar GitHub Project (tablero Kanban)** | Yeray | — |
| **E1-02** | [#2](https://github.com/yeray06/Retroconver/issues/2) | **README inicial del repositorio** | David | E1-01 |
| **E1-03** | [#3](https://github.com/yeray06/Retroconver/issues/3) | **Plantillas de issues y PR, guía de contribución y .gitignore** | Yeray | — |
| **E1-04** | [#4](https://github.com/yeray06/Retroconver/issues/4) | **Planificación inicial y dependencias entre tareas** | Yeray | — |
| **E1-05** | [#5](https://github.com/yeray06/Retroconver/issues/5) | **Primera versión de la memoria (Word)** | David | E1-04 |
| **E1-06** | [#6](https://github.com/yeray06/Retroconver/issues/6) | **Guía de estilo retro (paleta, tipografía y referencias)** | David | — |

### Entrega 2 · Diseño y base técnica · hasta el 01/11/2026

| Código | Issue | Tarea | Responsable | Depende de |
|---|---|---|---|---|
| **E2-01** | [#7](https://github.com/yeray06/Retroconver/issues/7) | **Crear proyecto Android (Kotlin + Jetpack Compose) y estructura de paquetes** | Yeray | E1-03 |
| E2-01.1 | [#34](https://github.com/yeray06/Retroconver/issues/34) | ↳ Configurar dependencias en el catálogo de versiones | Yeray | — |
| E2-01.2 | [#35](https://github.com/yeray06/Retroconver/issues/35) | ↳ Crear estructura de paquetes, Application y MainActivity | Yeray | E2-01.1 |
| **E2-02** | [#8](https://github.com/yeray06/Retroconver/issues/8) | **Navegación principal entre pantallas** | Yeray | E2-01 |
| E2-02.1 | [#36](https://github.com/yeray06/Retroconver/issues/36) | ↳ Definir rutas y barra de navegación inferior | Yeray | E2-01.2 |
| E2-02.2 | [#37](https://github.com/yeray06/Retroconver/issues/37) | ↳ Pantallas provisionales de las cuatro secciones | Yeray | E2-02.1 |
| **E2-03** | [#9](https://github.com/yeray06/Retroconver/issues/9) | **Configurar la base de datos Room** | Yeray | E2-01 |
| E2-03.1 | [#38](https://github.com/yeray06/Retroconver/issues/38) | ↳ Crear AppDatabase y convertidores de tipos | Yeray | E2-01.1 |
| E2-03.2 | [#39](https://github.com/yeray06/Retroconver/issues/39) | ↳ Contenedor de dependencias para la base de datos y los repositorios | Yeray | E2-01.2, E2-03.1 |
| **E2-04** | [#10](https://github.com/yeray06/Retroconver/issues/10) | **Bocetos de pantallas y recorrido principal** | David | E1-06 |
| E2-04.1 | [#40](https://github.com/yeray06/Retroconver/issues/40) | ↳ Boceto de la pantalla de Perfil | David | E1-06 |
| E2-04.2 | [#41](https://github.com/yeray06/Retroconver/issues/41) | ↳ Boceto del Muro y de nueva publicación | David | E1-06 |
| E2-04.3 | [#42](https://github.com/yeray06/Retroconver/issues/42) | ↳ Boceto de la Galería y del detalle de álbum | David | E1-06 |
| E2-04.4 | [#43](https://github.com/yeray06/Retroconver/issues/43) | ↳ Diagrama de navegación y recorrido principal | David | E2-04.1, E2-04.2, E2-04.3 |
| **E2-05** | [#11](https://github.com/yeray06/Retroconver/issues/11) | **Esquema de datos (entidades y relaciones)** | David | — |
| E2-05.1 | [#44](https://github.com/yeray06/Retroconver/issues/44) | ↳ Diagrama entidad-relación de la base de datos | David | — |
| E2-05.2 | [#45](https://github.com/yeray06/Retroconver/issues/45) | ↳ Justificar qué guarda Room y qué guarda DataStore | David | E2-05.1 |
| **E2-06** | [#12](https://github.com/yeray06/Retroconver/issues/12) | **Tema retro en Compose (colores, tipografía y componentes base)** | David | E1-06, E2-01 |
| E2-06.1 | [#46](https://github.com/yeray06/Retroconver/issues/46) | ↳ Colores, tipografías y formas en el tema de Compose | David | E1-06, E2-01.2 |
| E2-06.2 | [#47](https://github.com/yeray06/Retroconver/issues/47) | ↳ Componentes base: tarjeta, botón, cabecera y avatar | David | E2-02.2, E2-06.1 |
| **E2-07** | [#48](https://github.com/yeray06/Retroconver/issues/48) | **Memoria v2: requisitos, navegación, datos y arquitectura** | David | E1-05, E2-04.4, E2-05.2 |
| E2-07.1 | [#49](https://github.com/yeray06/Retroconver/issues/49) | ↳ Requisitos funcionales principales | Yeray | — |
| E2-07.2 | [#50](https://github.com/yeray06/Retroconver/issues/50) | ↳ Decisiones de arquitectura y tecnologías | Yeray | E2-03.2 |
| **E2-08** | [#51](https://github.com/yeray06/Retroconver/issues/51) | **Revisar tablero y README para la entrega 2** | Yeray | E2-07 |

### Entrega 3 · Flujo principal · hasta el 08/11/2026

| Código | Issue | Tarea | Responsable | Depende de |
|---|---|---|---|---|
| **E3-01** | [#13](https://github.com/yeray06/Retroconver/issues/13) | **Entidad Usuario y DAO** | Yeray | E2-03, E2-05 |
| E3-01.1 | [#52](https://github.com/yeray06/Retroconver/issues/52) | ↳ Crear el usuario local en el primer arranque | Yeray | E2-03.2 |
| **E3-02** | [#15](https://github.com/yeray06/Retroconver/issues/15) | **Entidades Publicación y Comentario y DAO** | David | E2-03, E2-05 |
| E3-02.1 | [#56](https://github.com/yeray06/Retroconver/issues/56) | ↳ Entidad Publicación y su DAO | David | E2-03.1, E2-05.1 |
| E3-02.2 | [#57](https://github.com/yeray06/Retroconver/issues/57) | ↳ Entidad Comentario y su DAO | David | E3-02.1 |
| **E3-03** | [#16](https://github.com/yeray06/Retroconver/issues/16) | **Pantalla del muro (lista cronológica)** | David | E2-02, E2-06, E3-02 |
| E3-03.1 | [#58](https://github.com/yeray06/Retroconver/issues/58) | ↳ Lista del muro con ViewModel | David | E2-02.2, E3-02.1 |
| E3-03.2 | [#59](https://github.com/yeray06/Retroconver/issues/59) | ↳ Tarjeta de publicación y estado vacío | David | E2-06.2 |
| **E3-04** | [#19](https://github.com/yeray06/Retroconver/issues/19) | **Selección de imágenes con Photo Picker y permisos** | Yeray | E2-01 |
| **E3-05** | [#20](https://github.com/yeray06/Retroconver/issues/20) | **Reescalado de imágenes a 720p y guardado local** | Yeray | E3-04 |
| E3-05.1 | [#62](https://github.com/yeray06/Retroconver/issues/62) | ↳ Función de reescalado a 720p | Yeray | E3-04 |
| E3-05.2 | [#63](https://github.com/yeray06/Retroconver/issues/63) | ↳ Guardar las imágenes en el almacenamiento interno | Yeray | E3-05.1 |
| **E3-06** | [#14](https://github.com/yeray06/Retroconver/issues/14) | **Pantalla de perfil (avatar, biografía y estado)** | Yeray | E2-02, E2-06, E3-01, E3-04 |
| E3-06.1 | [#53](https://github.com/yeray06/Retroconver/issues/53) | ↳ Vista del perfil con ViewModel | Yeray | E2-06.2, E3-01.1 |
| E3-06.2 | [#54](https://github.com/yeray06/Retroconver/issues/54) | ↳ Edición del perfil con validación | Yeray | E3-06.1 |
| E3-06.3 | [#55](https://github.com/yeray06/Retroconver/issues/55) | ↳ Cambiar el avatar con una foto del dispositivo | Yeray | E3-04, E3-05, E3-06.1 |
| **E3-07** | [#17](https://github.com/yeray06/Retroconver/issues/17) | **Crear publicación con texto e imagen** | David | E3-03, E3-04, E3-05 |
| E3-07.1 | [#60](https://github.com/yeray06/Retroconver/issues/60) | ↳ Formulario de nueva publicación con validación | David | E3-03.1 |
| E3-07.2 | [#61](https://github.com/yeray06/Retroconver/issues/61) | ↳ Adjuntar una imagen a la publicación | David | E3-04, E3-05, E3-07.1 |
| **E3-08** | [#64](https://github.com/yeray06/Retroconver/issues/64) | **Memoria v3: arquitectura aplicada, cambios y problemas** | David | E2-07 |
| **E3-09** | [#65](https://github.com/yeray06/Retroconver/issues/65) | **Cómo probar el flujo principal (entrega 3)** | Yeray | E3-06.2, E3-07.2 |

### Entrega 4 · Versión candidata · hasta el 15/11/2026

| Código | Issue | Tarea | Responsable | Depende de |
|---|---|---|---|---|
| **E4-01** | [#109](https://github.com/yeray06/Retroconver/issues/109) | **Cambio de alcance: red social con servidor** | Yeray | — |
| **E4-02** | [#110](https://github.com/yeray06/Retroconver/issues/110) | **Proyecto Supabase: tablas, seguridad y almacenamiento** | Yeray | E4-01 |
| E4-02.1 | [#111](https://github.com/yeray06/Retroconver/issues/111) | ↳ Tablas y relaciones en supabase/esquema.sql | Yeray | E4-01 |
| E4-02.2 | [#112](https://github.com/yeray06/Retroconver/issues/112) | ↳ Políticas de seguridad (RLS) y almacenamiento de imágenes | Yeray | E4-02.1 |
| **E4-03** | [#113](https://github.com/yeray06/Retroconver/issues/113) | **Cliente de red con Retrofit** | Yeray | E4-02 |
| E4-03.1 | [#114](https://github.com/yeray06/Retroconver/issues/114) | ↳ Retrofit, OkHttp y configuración del proyecto Supabase | Yeray | E4-02.1 |
| E4-03.2 | [#115](https://github.com/yeray06/Retroconver/issues/115) | ↳ Errores de red y funcionamiento sin conexión | Yeray | E4-03.1 |
| **E4-04** | [#116](https://github.com/yeray06/Retroconver/issues/116) | **Registro e inicio de sesión** | Yeray | E4-03 |
| E4-04.1 | [#117](https://github.com/yeray06/Retroconver/issues/117) | ↳ Sesión con Supabase Auth guardada en DataStore | Yeray | E4-03.1 |
| E4-04.2 | [#118](https://github.com/yeray06/Retroconver/issues/118) | ↳ Pantallas de registro e inicio de sesión | Yeray | E4-04.1 |
| E4-04.3 | [#119](https://github.com/yeray06/Retroconver/issues/119) | ↳ Cerrar sesión | Yeray | E4-04.1 |
| **E4-05** | [#120](https://github.com/yeray06/Retroconver/issues/120) | **Room como copia local de los datos del servidor** | Yeray | E4-03, E4-04 |
| E4-05.1 | [#121](https://github.com/yeray06/Retroconver/issues/121) | ↳ Ids del servidor (UUID) y usuario de la sesión en Room | Yeray | E4-03.1, E4-04.1 |
| E4-05.2 | [#122](https://github.com/yeray06/Retroconver/issues/122) | ↳ Perfil y avatar guardados en el servidor | Yeray | E3-06.3, E4-02.2, E4-05.1 |
| **E4-06** | [#123](https://github.com/yeray06/Retroconver/issues/123) | **Publicaciones compartidas en el servidor** | David | E3-07, E4-05 |
| E4-06.1 | [#124](https://github.com/yeray06/Retroconver/issues/124) | ↳ Publicar con la imagen subida al almacenamiento | David | E3-07.2, E4-02.2, E4-05.1 |
| E4-06.2 | [#125](https://github.com/yeray06/Retroconver/issues/125) | ↳ Muro con las publicaciones de todas las personas | David | E4-06.1 |
| **E4-07** | [#18](https://github.com/yeray06/Retroconver/issues/18) | **Comentarios en publicaciones** | David | E3-03, E4-06 |
| E4-07.1 | [#66](https://github.com/yeray06/Retroconver/issues/66) | ↳ Detalle de publicación con sus comentarios | David | E3-02.2, E3-03.2, E4-06.2 |
| E4-07.2 | [#67](https://github.com/yeray06/Retroconver/issues/67) | ↳ Añadir comentarios y mostrar el contador | David | E4-07.1 |
| **E4-08** | [#126](https://github.com/yeray06/Retroconver/issues/126) | **Perfiles de otras personas** | David | E4-05 |
| E4-08.1 | [#127](https://github.com/yeray06/Retroconver/issues/127) | ↳ Ver el perfil de otra persona | David | E4-05.2, E4-06.2 |
| E4-08.2 | [#128](https://github.com/yeray06/Retroconver/issues/128) | ↳ Lista de personas de la red con buscador | David | E4-08.1 |
| **E4-09** | [#22](https://github.com/yeray06/Retroconver/issues/22) | **Tablón de mensajes en el perfil** | David | E2-05, E3-06, E4-08 |
| E4-09.1 | [#72](https://github.com/yeray06/Retroconver/issues/72) | ↳ Mensajes del tablón en el servidor y en Room | David | E2-03.1, E2-05.1, E4-02.1, E4-05.1 |
| E4-09.2 | [#73](https://github.com/yeray06/Retroconver/issues/73) | ↳ Tablón de mensajes en la pantalla de perfil | David | E3-06.1, E4-08.1, E4-09.1 |
| E4-09.3 | [#74](https://github.com/yeray06/Retroconver/issues/74) | ↳ Escribir y borrar mensajes del tablón | David | E4-09.2 |
| **E4-10** | [#21](https://github.com/yeray06/Retroconver/issues/21) | **Álbumes de fotos (crear, listar y ver)** | Yeray | E2-03, E3-05, E4-05 |
| E4-10.1 | [#68](https://github.com/yeray06/Retroconver/issues/68) | ↳ Álbumes y fotos en el servidor y en Room | Yeray | E2-03.1, E2-05.1, E4-02.1, E4-05.1 |
| E4-10.2 | [#69](https://github.com/yeray06/Retroconver/issues/69) | ↳ Lista de álbumes y crear álbum | Yeray | E2-02.2, E4-10.1 |
| E4-10.3 | [#70](https://github.com/yeray06/Retroconver/issues/70) | ↳ Detalle de álbum en cuadrícula | Yeray | E4-08.1, E4-10.2 |
| E4-10.4 | [#71](https://github.com/yeray06/Retroconver/issues/71) | ↳ Añadir y borrar fotos de un álbum | Yeray | E3-05.2, E4-02.2, E4-10.3 |
| **E4-11** | [#23](https://github.com/yeray06/Retroconver/issues/23) | **Personalización del perfil (color de fondo y tema)** | David | E2-06, E3-06, E4-05.2 |
| E4-11.1 | [#75](https://github.com/yeray06/Retroconver/issues/75) | ↳ Color de fondo y tema guardados en el perfil | David | E2-03.2, E4-05.2 |
| E4-11.2 | [#76](https://github.com/yeray06/Retroconver/issues/76) | ↳ Selector de color de fondo y tema del perfil | David | E3-06.1, E4-11.1 |
| **E4-12** | [#24](https://github.com/yeray06/Retroconver/issues/24) | **Pruebas y accesibilidad de Perfil, Galería y servidor** | Yeray | E3-06, E4-04, E4-05, E4-10, E4-11 |
| E4-12.1 | [#77](https://github.com/yeray06/Retroconver/issues/77) | ↳ Pruebas de reescalado, DAOs, sesión y cliente de red | Yeray | E3-05.1, E4-03.2, E4-04.1, E4-10.1 |
| E4-12.2 | [#78](https://github.com/yeray06/Retroconver/issues/78) | ↳ Revisión de accesibilidad de Perfil, Galería y login | Yeray | E3-06.2, E4-04.2, E4-10.4 |
| **E4-13** | [#25](https://github.com/yeray06/Retroconver/issues/25) | **Pruebas y accesibilidad de Muro y Mensajes** | David | E3-07, E4-06, E4-07, E4-08, E4-09 |
| E4-13.1 | [#79](https://github.com/yeray06/Retroconver/issues/79) | ↳ Pruebas de DAOs y repositorios de Muro y Mensajes | David | E3-02.2, E4-06.2, E4-09.1 |
| E4-13.2 | [#80](https://github.com/yeray06/Retroconver/issues/80) | ↳ Revisión de accesibilidad de Muro y Mensajes | David | E4-07.2, E4-09.3 |
| **E4-14** | [#81](https://github.com/yeray06/Retroconver/issues/81) | **Memoria v4: servidor, pruebas, diseño, usabilidad y accesibilidad** | David | E3-08, E4-01, E4-12.2, E4-13.2 |
| **E4-15** | [#82](https://github.com/yeray06/Retroconver/issues/82) | **Revisar tablero y README para la versión candidata** | Yeray | E4-14 |

### Entrega final · hasta el 20/11/2026

| Código | Issue | Tarea | Responsable | Depende de |
|---|---|---|---|---|
| **EF-01** | [#26](https://github.com/yeray06/Retroconver/issues/26) | **APK final y README definitivo** | Yeray | E4-12, E4-13 |
| EF-01.1 | [#83](https://github.com/yeray06/Retroconver/issues/83) | ↳ Generar APK firmado y publicar la Release | Yeray | E4-15 |
| **EF-02** | [#27](https://github.com/yeray06/Retroconver/issues/27) | **Presentación de la exposición** | David | E4-12, E4-13 |
| EF-02.1 | [#84](https://github.com/yeray06/Retroconver/issues/84) | ↳ Diapositivas y guion de la demo | David | EF-01.1 |
| **EF-03** | [#85](https://github.com/yeray06/Retroconver/issues/85) | **Memoria final** | David | E4-14 |

### Si hay tiempo (extras)

| Código | Issue | Tarea | Responsable | Depende de |
|---|---|---|---|---|
| **EX-01** | [#28](https://github.com/yeray06/Retroconver/issues/28) | **Mensajes directos entre usuarios** | David | E4-09 |
| **EX-02** | [#29](https://github.com/yeray06/Retroconver/issues/29) | **Comentarios anidados (respuestas)** | David | E4-07 |
| **EX-03** | [#30](https://github.com/yeray06/Retroconver/issues/30) | **Temas clásicos y tipografías seleccionables por perfil** | Yeray | E4-11 |
| **EX-04** | [#129](https://github.com/yeray06/Retroconver/issues/129) | **Amistades: solicitudes y muro solo con amigos** | Yeray | E4-08 |
| **EX-05** | [#130](https://github.com/yeray06/Retroconver/issues/130) | **«Me gusta» en publicaciones** | David | E4-06 |

## Dependencias entre tareas principales

Cada flecha va de la tarea que hay que terminar antes a la que depende de ella. Si una subtarea depende de otra, la flecha se dibuja entre sus tareas principales. Azul: Yeray. Verde: David. No se incluyen los extras.

```mermaid
flowchart LR
  subgraph E1["Entrega 1 · Organización"]
    I1["E1-01 · Configurar GitHub Project (tablero Kanban)"]
    I2["E1-02 · README inicial del repositorio"]
    I3["E1-03 · Plantillas de issues y PR, guía de contribución y .gitignore"]
    I4["E1-04 · Planificación inicial y dependencias entre tareas"]
    I5["E1-05 · Primera versión de la memoria (Word)"]
    I6["E1-06 · Guía de estilo retro (paleta, tipografía y referencias)"]
  end
  subgraph E2["Entrega 2 · Diseño y base técnica"]
    I7["E2-01 · Crear proyecto Android (Kotlin + Jetpack Compose) y estructura de paquetes"]
    I8["E2-02 · Navegación principal entre pantallas"]
    I9["E2-03 · Configurar la base de datos Room"]
    I10["E2-04 · Bocetos de pantallas y recorrido principal"]
    I11["E2-05 · Esquema de datos (entidades y relaciones)"]
    I12["E2-06 · Tema retro en Compose (colores, tipografía y componentes base)"]
    I48["E2-07 · Memoria v2: requisitos, navegación, datos y arquitectura"]
    I51["E2-08 · Revisar tablero y README para la entrega 2"]
  end
  subgraph E3["Entrega 3 · Flujo principal"]
    I13["E3-01 · Entidad Usuario y DAO"]
    I15["E3-02 · Entidades Publicación y Comentario y DAO"]
    I16["E3-03 · Pantalla del muro (lista cronológica)"]
    I19["E3-04 · Selección de imágenes con Photo Picker y permisos"]
    I20["E3-05 · Reescalado de imágenes a 720p y guardado local"]
    I14["E3-06 · Pantalla de perfil (avatar, biografía y estado)"]
    I17["E3-07 · Crear publicación con texto e imagen"]
    I64["E3-08 · Memoria v3: arquitectura aplicada, cambios y problemas"]
    I65["E3-09 · Cómo probar el flujo principal (entrega 3)"]
  end
  subgraph E4["Entrega 4 · Versión candidata"]
    I109["E4-01 · Cambio de alcance: red social con servidor"]
    I110["E4-02 · Proyecto Supabase: tablas, seguridad y almacenamiento"]
    I113["E4-03 · Cliente de red con Retrofit"]
    I116["E4-04 · Registro e inicio de sesión"]
    I120["E4-05 · Room como copia local de los datos del servidor"]
    I123["E4-06 · Publicaciones compartidas en el servidor"]
    I18["E4-07 · Comentarios en publicaciones"]
    I126["E4-08 · Perfiles de otras personas"]
    I22["E4-09 · Tablón de mensajes en el perfil"]
    I21["E4-10 · Álbumes de fotos (crear, listar y ver)"]
    I23["E4-11 · Personalización del perfil (color de fondo y tema)"]
    I24["E4-12 · Pruebas y accesibilidad de Perfil, Galería y servidor"]
    I25["E4-13 · Pruebas y accesibilidad de Muro y Mensajes"]
    I81["E4-14 · Memoria v4: servidor, pruebas, diseño, usabilidad y accesibilidad"]
    I82["E4-15 · Revisar tablero y README para la versión candidata"]
  end
  subgraph E5["Entrega final"]
    I26["EF-01 · APK final y README definitivo"]
    I27["EF-02 · Presentación de la exposición"]
    I85["EF-03 · Memoria final"]
  end
  I1 --> I2
  I3 --> I7
  I4 --> I5
  I5 --> I48
  I6 --> I10
  I6 --> I12
  I7 --> I8
  I7 --> I9
  I7 --> I12
  I7 --> I19
  I8 --> I12
  I8 --> I16
  I8 --> I14
  I8 --> I21
  I9 --> I48
  I9 --> I13
  I9 --> I15
  I9 --> I22
  I9 --> I21
  I9 --> I23
  I10 --> I48
  I11 --> I48
  I11 --> I13
  I11 --> I15
  I11 --> I22
  I11 --> I21
  I12 --> I16
  I12 --> I14
  I12 --> I23
  I48 --> I51
  I48 --> I64
  I13 --> I14
  I15 --> I16
  I15 --> I18
  I15 --> I25
  I16 --> I17
  I16 --> I18
  I19 --> I20
  I19 --> I14
  I19 --> I17
  I20 --> I14
  I20 --> I17
  I20 --> I21
  I20 --> I24
  I14 --> I65
  I14 --> I120
  I14 --> I22
  I14 --> I23
  I14 --> I24
  I17 --> I65
  I17 --> I123
  I17 --> I25
  I64 --> I81
  I109 --> I110
  I109 --> I81
  I110 --> I113
  I110 --> I120
  I110 --> I123
  I110 --> I22
  I110 --> I21
  I113 --> I116
  I113 --> I120
  I113 --> I24
  I116 --> I120
  I116 --> I24
  I120 --> I123
  I120 --> I126
  I120 --> I22
  I120 --> I21
  I120 --> I23
  I120 --> I24
  I123 --> I18
  I123 --> I126
  I123 --> I25
  I18 --> I25
  I126 --> I22
  I126 --> I21
  I126 --> I25
  I22 --> I25
  I21 --> I24
  I23 --> I24
  I24 --> I81
  I24 --> I26
  I24 --> I27
  I25 --> I81
  I25 --> I26
  I25 --> I27
  I81 --> I82
  I81 --> I85
  I82 --> I26
  I26 --> I27
  classDef yeray fill:#dbeafe,stroke:#1d4ed8,color:#0f172a
  classDef david fill:#dcfce7,stroke:#15803d,color:#0f172a
  class I1,I3,I4,I7,I8,I9,I51,I13,I19,I20,I14,I65,I109,I110,I113,I116,I120,I21,I24,I82,I26 yeray
  class I2,I5,I6,I10,I11,I12,I48,I15,I16,I17,I64,I123,I18,I126,I22,I23,I25,I81,I27,I85 david
```

## Puntos críticos

- **La base del servidor bloquea toda la entrega 4.** El proyecto Supabase (E4-02, [#110](https://github.com/yeray06/Retroconver/issues/110)), el cliente de red (E4-03, [#113](https://github.com/yeray06/Retroconver/issues/113)), la sesión (E4-04.1, [#117](https://github.com/yeray06/Retroconver/issues/117)) y los ids del servidor en Room (E4-05.1, [#121](https://github.com/yeray06/Retroconver/issues/121)) van primero. Yeray los deja documentados antes de que David adapte Muro y Mensajes.
- **El cambio de ids a UUID (E4-05.1, [#121](https://github.com/yeray06/Retroconver/issues/121)) toca las entidades de los dos.** Se hace en una sola Pull Request que revisa David, y mientras tanto nadie cambia las entidades.
- **La selección de imágenes (E3-04, [#19](https://github.com/yeray06/Retroconver/issues/19)) y el reescalado a 720p (E3-05, [#20](https://github.com/yeray06/Retroconver/issues/20))** se reutilizan: la imagen reducida es la que se sube al servidor.
- **Supabase pausa los proyectos gratuitos tras una semana sin uso.** Antes de cada entrega y de la presentación hay que comprobar que está activo.
- **La URL y la clave del servidor no están en el repositorio.** Cada integrante las pone en su `local.properties` (ver el README).

