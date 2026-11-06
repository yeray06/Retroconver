# Retroconver

Red social para Android con la estética y la sencillez de las redes de finales de los 2000, inspirada en la primera época de Tuenti y en la personalización de MySpace.

**Proyecto Integrador · Desarrollo Móvil · 1º DAM**

| | |
|---|---|
| Tablero Kanban | [GitHub Project · Retroconver](https://github.com/users/yeray06/projects/2) |
| Propuesta inicial | [docs/propuesta/Retroconver.pdf](docs/propuesta/Retroconver.pdf) |
| Planificación | [docs/planificacion.md](docs/planificacion.md) |
| Requisitos | [docs/requisitos.md](docs/requisitos.md) |
| Arquitectura y tecnologías | [docs/arquitectura.md](docs/arquitectura.md) |
| Guía de estilo y bocetos | [docs/estilo.md](docs/estilo.md) · [docs/diseño/](docs/diseño/) |
| Navegación y recorrido principal | [docs/navegacion.md](docs/navegacion.md) · [docs/recorrido.md](docs/recorrido.md) |
| Cómo trabajamos | [CONTRIBUTING.md](CONTRIBUTING.md) |

## Equipo

| Integrante | GitHub | Se encarga de |
|---|---|---|
| Yeray Sánchez | [@yeray06](https://github.com/yeray06) | Organización del repositorio y planificación, base del proyecto, navegación, Room, **servidor Supabase**, registro y sesión, módulo **Perfil** y módulo **Galería** |
| David Nicolás | [@DavidNC26](https://github.com/DavidNC26) | README y memoria, estilo retro y tema de Compose, personalización y perfiles de otras personas, módulo **Muro** (publicaciones y comentarios) y módulo **Mensajes** |

Cada integrante es responsable de sus módulos de principio a fin (datos, pantallas y pruebas). Todas las Pull Requests las revisa y aprueba el otro integrante.

## Qué es Retroconver

### Problema que aborda

Las redes sociales actuales giran en torno a algoritmos de recomendación, vídeo corto y un diseño cargado de estímulos y publicidad. Se ha perdido la idea original de una red pequeña y cercana: un muro con lo que publica tu gente, un tablón en tu perfil donde te dejan mensajes, tus álbumes de fotos y un perfil que decoras a tu gusto.

Retroconver recupera esa experiencia en una aplicación Android sencilla, sin distracciones y sin algoritmos.

### Para quién es

- **Usuario nostálgico (25-40 años).** Vivió la época de Tuenti y MySpace y quiere volver a tener perfiles personalizados y una relación sin algoritmos.
- **Usuario que busca simplicidad (18-25 años).** Le atrae la estética retro y prefiere una red de círculo cerrado, sin publicidad ni algoritmos.

### Qué la hace diferente

- **Orden cronológico, sin algoritmo.** El muro muestra lo último publicado, nada más.
- **Red propia y pequeña.** Las cuentas y lo que se publica están en un servidor Supabase del proyecto, con el plan gratuito. El móvil guarda una copia en Room para verlo sin conexión.
- **Perfil personalizable.** Avatar, biografía, estado y aspecto del perfil, al estilo de la época.
- **Multimedia limitado a 720p.** Las imágenes se reescalan a 720p como máximo. Ahorra espacio, mejora el rendimiento y refuerza la estética de las cámaras de entonces.

## Alcance

### Imprescindible (primera versión)

| Módulo | Qué incluye | Responsable |
|---|---|---|
| Servidor | Proyecto Supabase con cuentas, base de datos, políticas de seguridad e imágenes. Cliente Retrofit en la app | Yeray |
| Perfil | Registro e inicio de sesión. Avatar, biografía y estado en el servidor | Yeray |
| Perfiles de otras personas | Ver el perfil, el tablón y los álbumes de cualquiera, y buscar personas. Color de fondo y tema del perfil | David |
| Muro | Publicaciones de todas las personas, con imagen opcional y en orden cronológico, y comentarios | David |
| Galería | Elegir fotos del dispositivo sin permisos, reescalado a 720p y álbumes | Yeray |
| Mensajes | Tablón en cada perfil donde cualquiera puede dejar mensajes | David |
| Estilo | Guía de estilo retro y tema de Compose común a toda la app | David |

### Si hay tiempo

- Mensajes directos con zumbido, como en MSN (EX-01, [#28](https://github.com/yeray06/Retroconver/issues/28))
- Amistades: solicitudes y muro solo con amigos (EX-04, [#129](https://github.com/yeray06/Retroconver/issues/129))
- «Me gusta» en publicaciones (EX-05, [#130](https://github.com/yeray06/Retroconver/issues/130))
- Comentarios anidados o respuestas (EX-02, [#29](https://github.com/yeray06/Retroconver/issues/29))
- Temas clásicos y tipografías seleccionables por perfil (EX-03, [#30](https://github.com/yeray06/Retroconver/issues/30))
- Vídeos cortos, también limitados a 720p

### Fuera de alcance

- Notificaciones push
- Actualización en tiempo real: lo nuevo aparece al abrir la pantalla o al deslizar para actualizar
- Publicar o comentar sin conexión para enviarlo más tarde

## Tecnologías

| Tecnología | Para qué la usaremos |
|---|---|
| Kotlin | Lenguaje de toda la aplicación |
| Jetpack Compose | Interfaz y tema visual retro |
| Navigation Compose | Navegación entre Perfil, Muro, Galería y Mensajes |
| Supabase | Servidor: cuentas, base de datos Postgres e imágenes (plan gratuito) |
| Retrofit y OkHttp | Llamadas de la app al servidor |
| Room | Copia local de lo descargado, para que las pantallas se actualicen solas y funcionen sin conexión |
| DataStore | Sesión: tokens e id de la cuenta |
| Photo Picker de Android | Elegir imágenes del dispositivo sin pedir permisos |

## Estado actual

**Entrega 3 · Flujo principal.**

### Qué funciona

| Qué | Dónde | Issue |
|---|---|---|
| Usuario local creado automáticamente la primera vez que se abre la app (sin registro) | `data/local/UsuarioEntity.kt`, `UsuarioDao.kt`, `data/repository/UsuarioRepository.kt` | E3-01 [#13](https://github.com/yeray06/Retroconver/issues/13) |
| Perfil con avatar, nombre, estado y biografía guardados en Room | `ui/screens/perfil/` | E3-06 [#14](https://github.com/yeray06/Retroconver/issues/14) |
| Edición del perfil con nombre obligatorio, límite de caracteres y mensajes de error | `ui/screens/perfil/EditarPerfil*.kt` | E3-06.2 [#54](https://github.com/yeray06/Retroconver/issues/54) |
| Elegir fotos con el Photo Picker de Android, sin pedir permisos | `ui/components/SelectorDeImagen.kt` | E3-04 [#19](https://github.com/yeray06/Retroconver/issues/19) |
| Reescalado de imágenes a 720p como máximo, con la orientación EXIF corregida, y copia en el almacenamiento interno | `data/imagenes/` | E3-05 [#20](https://github.com/yeray06/Retroconver/issues/20) |
| Crear publicación desde el botón «+» del muro: texto obligatorio con contador y foto opcional (con vista previa, se puede quitar) guardada a 720p | `ui/screens/muro/NuevaPublicacion*.kt` | E3-07 [#17](https://github.com/yeray06/Retroconver/issues/17) |
| Muro con las publicaciones de la más reciente a la más antigua y estado vacío | `ui/screens/muro/` | E3-03 [#16](https://github.com/yeray06/Retroconver/issues/16) |
| Publicaciones y comentarios en Room con su DAO y repositorio | `data/local/Publicacion*.kt`, `Comentario*.kt` | E3-02 [#15](https://github.com/yeray06/Retroconver/issues/15) |
| Proyecto Android, navegación con barra inferior, Room, tema retro y componentes base | `app/`, `navigation/`, `ui/theme/`, `ui/components/` | Entrega 2 |
| Documentación: requisitos, arquitectura, esquema de datos, guía de estilo y bocetos | `docs/` | Entrega 2 |

## Cómo probar el flujo principal (entrega 3)

Primero abre y ejecuta el proyecto como se explica en [Cómo abrir y ejecutar el proyecto](#cómo-abrir-y-ejecutar-el-proyecto).

### 1. Primera apertura

1. Abre la app. Se abre el **Muro** con el mensaje «El muro está vacío».
2. Ve a **Perfil**. Ya existe tu usuario local, «Yo», con la inicial en el avatar. No hay registro ni contraseña: se crea solo la primera vez (RF-04).

### 2. Editar el perfil

1. En Perfil pulsa **Editar perfil**.
2. Borra el nombre y pulsa **Guardar**: aparece «El nombre es obligatorio.» y el campo se pone en rojo.
3. Escribe más de 80 caracteres en el estado y pulsa **Guardar**: aparece «Máximo 80 caracteres.». Debajo de cada campo se ve el contador.
4. Escribe un nombre, un estado y una biografía válidos y pulsa **Guardar**. Vuelves al perfil y ya se ven los datos nuevos.
5. Cierra la app del todo (deslízala fuera de las apps recientes) y ábrela otra vez: el perfil sigue igual. Los datos están en Room.

### 3. Cambiar el avatar

1. En Perfil pulsa **Cambiar foto**. Se abre el selector de fotos de Android sin pedir ningún permiso.
2. Elige una foto. Se ve en el perfil y en las publicaciones del muro.
3. Para probar con una foto grande, arrastra al emulador una foto de móvil (por ejemplo de 4000×3000) y elígela. En *View → Tool Windows → Device Explorer*, la carpeta `data/data/com.retroconver/files/images/` tiene una sola imagen, como mucho de 960×720. Al cambiar otra vez de foto, la anterior se borra.

### 4. Publicar en el muro

1. En el Muro pulsa el botón **+** de la cabecera. Se abre el formulario «Nueva publicación».
2. Pulsa **Publicar** sin escribir nada: aparece «Escribe algo para publicar.» y el campo se pone en rojo. Lo mismo si solo escribes espacios.
3. Escribe un texto. Debajo se ve el contador (por ejemplo `12/500`); con más de 500 caracteres aparece «Máximo 500 caracteres.».
4. Pulsa **Añadir foto** y elige una. Se ve una vista previa. Pulsa **Quitar foto** para quitarla, o elige otra para cambiarla.
5. Pulsa **Publicar**: vuelves al muro y la publicación aparece arriba con tu avatar, tu nombre, «hace 1m», el texto y la imagen. El estado vacío desaparece.
6. En *Device Explorer*, `files/images/` tiene la foto reducida a 720p. Si cancelas el formulario con una foto elegida, esa copia se borra.

### Evidencias de Room y DataStore

- **Room.** Con la app abierta, en *View → Tool Windows → App Inspection → Database Inspector* se ven las tablas `usuarios`, `publicaciones` y `comentarios` y cómo cambian al editar el perfil. El esquema de cada versión de la base de datos está en `app/schemas/`. Las pruebas `UsuarioDaoTest` y `PublicacionDaoTest` comprueban las consultas con una base de datos en memoria.
- **Imágenes.** Room guarda solo la ruta de cada imagen. La imagen está en `files/images/`, reducida a 720p (ver el paso 3).
- **DataStore.** La dependencia está en el proyecto, pero todavía no guarda nada. Se usa en la entrega 4 para el color de fondo y el tema del perfil (E4-04, [#23](https://github.com/yeray06/Retroconver/issues/23)).

### Qué falla o tiene límites

- Las fotos **HEIC** no se pueden leer en Android 8.0 y 8.1, porque esas versiones no saben abrir ese formato. La app muestra «No se ha podido usar esa foto». Desde Android 9 funcionan.
- Mientras desarrollamos, si cambia el esquema de la base de datos se borran los datos guardados. Antes de la entrega final se sustituye por migraciones.

### Qué queda pendiente

- **Entrega 3:** crear publicaciones con texto e imagen (E3-07, [#17](https://github.com/yeray06/Retroconver/issues/17)) y la memoria v3 (E3-08, [#64](https://github.com/yeray06/Retroconver/issues/64)).
- **Entrega 4:** comentarios, álbumes de fotos, tablón de mensajes, personalización del perfil con DataStore y pruebas de accesibilidad.

El estado al día de cada tarea está en el [tablero](https://github.com/users/yeray06/projects/2).

## Cómo trabajamos

- **Tablero Kanban** con las columnas Backlog, Por hacer, En curso, En revisión y Hecho.
- **104 issues en orden de avance.** Cada título empieza por un código: `E2-01` es la primera tarea principal de la entrega 2 y `E2-01.1` su primera subtarea. `EF` es la entrega final y `EX`, los extras. El tablero está ordenado por este código, así que se lee de arriba abajo en el orden en que hay que avanzar.
- **Tareas principales y subtareas.** Las tareas grandes se dividen en subtareas (*sub-issues*). Se trabaja sobre las subtareas, y la tarea principal se cierra cuando están todas terminadas. El tablero muestra el progreso de cada tarea principal.
- **Cada issue tiene** responsable, criterios de terminado, entrega (milestone) y dependencias (*Blocked by*).
- **Una rama por subtarea**, o por tarea si no tiene subtareas. La rama lleva el número de GitHub de la issue (`feature/53-vista-perfil`). Nunca se hacen commits directos a `main`.
- **Pull Request enlazada a la issue** (`Closes #53`). La revisa y la aprueba el otro integrante, y después la fusiona su autor. Las ramas no se borran: quedan como registro del trabajo.

Los detalles están en [CONTRIBUTING.md](CONTRIBUTING.md).

## Cómo abrir y ejecutar el proyecto

### Requisitos

- **Android Studio** Narwhal 3 (2025.1.3) o posterior.
- **JDK 21 para Gradle.** Las versiones nuevas de Android Studio traen un Java más reciente que el que admite la versión de Gradle del proyecto.
- Un **emulador o móvil con Android 8.0 o superior**. Recomendado: Pixel 7 con API 36.

### Pasos

1. Clona el repositorio:
   ```bash
   git clone https://github.com/yeray06/Retroconver.git
   ```
2. En Android Studio: *File → Open*, elige la carpeta `Retroconver` y pulsa *Trust Project*.
3. Espera al **Gradle Sync**. La primera vez descarga Gradle 8.14.3 y las librerías.
   - Si sale **«Incompatible Gradle JVM version»**, pulsa *Apply compatible Gradle JDK configuration and sync*. También puedes ir a *Settings → Build, Execution, Deployment → Build Tools → Gradle* y elegir un **Gradle JDK 21**; si no tienes ninguno, usa *Download JDK*.
   - Si Android Studio propone **actualizar el Android Gradle Plugin** (*Upgrade Assistant*), **no lo hagas**. El proyecto usa versiones fijas que ya están probadas juntas: AGP 8.13.0, Gradle 8.14.3 y Kotlin 2.2.20.
   - Si propone migrar a *Gradle Daemon toolchain*, pulsa *Ignore*.
4. Elige la configuración **app** y tu emulador, y pulsa **▶ Run**.

### Pruebas

Las pruebas se lanzan con Gradle, no con el botón ▶ de la clase de prueba:

1. Abre el panel **Gradle** (el elefante de la derecha) y pulsa *Execute Gradle Task*.
2. Escribe `testDebugUnitTest` y pulsa Enter.
3. Tiene que terminar en **BUILD SUCCESSFUL**. El informe queda en `app/build/reports/tests/testDebugUnitTest/index.html`.

Las pruebas de la base de datos y de las imágenes necesitan Android, así que se ejecutan en el emulador. Con el emulador abierto, lanza la tarea `connectedDebugAndroidTest`. El informe queda en `app/build/reports/androidTests/connected/debug/index.html`.

### Estructura del código

```
app/src/main/java/com/retroconver/
├── RetroconverApp.kt   Application: crea el AppContainer
├── MainActivity.kt     Única Activity: tema y navegación
├── AppContainer.kt     Base de datos y repositorios (una sola instancia)
├── data/local/         AppDatabase, entidades, DAOs y convertidores
├── data/repository/    Repositorios de cada módulo
├── data/imagenes/      Reescalado a 720p y copias de las imágenes
├── navigation/         Rutas, barra inferior y NavHost
└── ui/
    ├── theme/          Tema retro: colores, tipografía y formas
    ├── components/     Componentes base: RetroHeader, RetroCard, RetroButton, Avatar y selector de imágenes
    └── screens/        Pantallas de cada módulo
```

El porqué de cada decisión está en [docs/arquitectura.md](docs/arquitectura.md).

## Planificación

| Entrega | Fecha límite | Objetivo |
|---|---|---|
| 1 · Organización | 25/10/2026 | Repo, Kanban, issues, planificación, README y memoria v1 |
| 2 · Diseño y base técnica | 01/11/2026 | Estilo, bocetos, esquema de datos, proyecto Android, navegación y Room |
| 3 · Flujo principal | 08/11/2026 | Perfil, muro y publicaciones con imagen funcionando en local |
| 4 · Versión candidata | 15/11/2026 | Servidor Supabase, cuentas, muro compartido, perfiles ajenos, comentarios, tablón, álbumes, personalización y pruebas |
| Final | 20/11/2026 | APK, README definitivo, memoria final y presentación |

El reparto completo con dependencias está en [docs/planificacion.md](docs/planificacion.md).
