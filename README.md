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
| Yeray Sánchez | [@yeray06](https://github.com/yeray06) | Organización del repositorio y planificación, base del proyecto, navegación, Room, módulo **Perfil** y módulo **Galería** |
| David Nicolás | [@DavidNC26](https://github.com/DavidNC26) | README y memoria, estilo retro y tema de Compose, módulo **Muro** (publicaciones y comentarios) y módulo **Mensajes** |

Cada integrante es responsable de sus módulos de principio a fin (datos, pantallas y pruebas). Todas las Pull Requests las revisa y aprueba el otro integrante.

## Qué es Retroconver

### Problema que aborda

Las redes sociales actuales giran en torno a algoritmos de recomendación, vídeo corto y un diseño cargado de estímulos y publicidad. Se ha perdido la idea original de una red pequeña y cercana: un muro con lo que publican tus amigos, un tablón en tu perfil, tus álbumes de fotos y un perfil que decoras a tu gusto.

Retroconver recupera esa experiencia en una aplicación Android sencilla, sin distracciones y sin algoritmos.

### Para quién es

- **Usuario nostálgico (25-40 años).** Vivió la época de Tuenti y MySpace y quiere volver a tener perfiles personalizados y una relación sin algoritmos.
- **Usuario que busca simplicidad (18-25 años).** Le atrae la estética retro y prefiere una red de círculo cerrado en la que sus datos se quedan en su dispositivo.

### Qué la hace diferente

- **Orden cronológico, sin algoritmo.** El muro muestra lo último publicado, nada más.
- **Datos en local.** Todo se guarda en el dispositivo con Room y DataStore. No hay servidor ni cuentas en la nube.
- **Perfil personalizable.** Avatar, biografía, estado y aspecto del perfil, al estilo de la época.
- **Multimedia limitado a 720p.** Las imágenes se reescalan a 720p como máximo. Ahorra espacio, mejora el rendimiento y refuerza la estética de las cámaras de entonces.

## Alcance inicial

### Imprescindible (primera versión)

| Módulo | Qué incluye | Responsable |
|---|---|---|
| Perfil | Avatar, biografía y estado guardados en Room. Color de fondo y tema guardados en DataStore | Yeray |
| Muro | Publicaciones de texto con imagen opcional en orden cronológico y comentarios | David |
| Galería | Elegir fotos del dispositivo con permisos puntuales, reescalado a 720p y álbumes | Yeray |
| Mensajes | Tablón de mensajes en el perfil | David |
| Estilo | Guía de estilo retro y tema de Compose común a toda la app | David |

### Si hay tiempo

- Mensajes directos entre usuarios (EX-01, [#28](https://github.com/yeray06/Retroconver/issues/28))
- Comentarios anidados o respuestas (EX-02, [#29](https://github.com/yeray06/Retroconver/issues/29))
- Temas clásicos y tipografías seleccionables por perfil (EX-03, [#30](https://github.com/yeray06/Retroconver/issues/30))
- Vídeos cortos, también limitados a 720p

### Fuera de alcance

- Servidor, registro online y sincronización entre dispositivos
- Notificaciones push

## Tecnologías

| Tecnología | Para qué la usaremos |
|---|---|
| Kotlin | Lenguaje de toda la aplicación |
| Jetpack Compose | Interfaz y tema visual retro |
| Navigation Compose | Navegación entre Perfil, Muro, Galería y Mensajes |
| Room | Usuarios, publicaciones, comentarios, álbumes, fotos y mensajes |
| DataStore | Preferencias del perfil: color de fondo, tema y ajustes |
| Photo Picker de Android | Elegir imágenes del dispositivo pidiendo solo los permisos necesarios |

## Estado actual

**Entrega 2 · Diseño y base técnica.**

### Qué funciona

| Qué | Dónde | Issue |
|---|---|---|
| Proyecto Android con Kotlin, Jetpack Compose y catálogo de versiones | `gradle/libs.versions.toml`, `app/` | E2-01 [#7](https://github.com/yeray06/Retroconver/issues/7) |
| Navegación con barra inferior entre Muro, Perfil, Galería y Mensajes (pantallas provisionales) | `navigation/`, `ui/screens/` | E2-02 [#8](https://github.com/yeray06/Retroconver/issues/8) |
| Base de datos Room con contenedor de dependencias y esquema exportado | `data/local/`, `AppContainer.kt`, `app/schemas/` | E2-03 [#9](https://github.com/yeray06/Retroconver/issues/9) |
| Prueba unitaria de los convertidores de fechas | `app/src/test/` | E2-03.1 [#38](https://github.com/yeray06/Retroconver/issues/38) |
| Guía de estilo, bocetos navegables, navegación y recorrido principal | `docs/` | E1-06 [#6](https://github.com/yeray06/Retroconver/issues/6), E2-04 [#10](https://github.com/yeray06/Retroconver/issues/10) |
| Requisitos y decisiones de arquitectura | `docs/requisitos.md`, `docs/arquitectura.md` | E2-07.1 [#49](https://github.com/yeray06/Retroconver/issues/49), E2-07.2 [#50](https://github.com/yeray06/Retroconver/issues/50) |

Al ejecutar la app se ve la barra inferior con las cuatro secciones. Cada una muestra de momento una pantalla provisional con su nombre.

### Pendiente

El estado al día de cada tarea está en el [tablero](https://github.com/users/yeray06/projects/2). En la entrega 3 se sustituyen las pantallas provisionales por el flujo principal: perfil, muro y publicaciones con imagen.

## Cómo trabajamos

- **Tablero Kanban** con las columnas Backlog, Por hacer, En curso, En revisión y Hecho.
- **82 issues en orden de avance.** Cada título empieza por un código: `E2-01` es la primera tarea principal de la entrega 2 y `E2-01.1` su primera subtarea. `EF` es la entrega final y `EX`, los extras. El tablero está ordenado por este código, así que se lee de arriba abajo en el orden en que hay que avanzar.
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

### Estructura del código

```
app/src/main/java/com/retroconver/
├── RetroconverApp.kt   Application: crea el AppContainer
├── MainActivity.kt     Única Activity: tema y navegación
├── AppContainer.kt     Base de datos y repositorios (una sola instancia)
├── data/local/         AppDatabase, entidades, DAOs y convertidores
├── data/repository/    Repositorios de cada módulo
├── navigation/         Rutas, barra inferior y NavHost
└── ui/                 Tema, componentes y pantallas de cada módulo
```

El porqué de cada decisión está en [docs/arquitectura.md](docs/arquitectura.md).

## Planificación

| Entrega | Fecha límite | Objetivo |
|---|---|---|
| 1 · Organización | 25/10/2026 | Repo, Kanban, issues, planificación, README y memoria v1 |
| 2 · Diseño y base técnica | 01/11/2026 | Estilo, bocetos, esquema de datos, proyecto Android, navegación y Room |
| 3 · Flujo principal | 08/11/2026 | Perfil, muro y publicaciones con imagen funcionando en local |
| 4 · Versión candidata | 15/11/2026 | Álbumes, tablón, comentarios, personalización y pruebas |
| Final | 20/11/2026 | APK, README definitivo, memoria final y presentación |

El reparto completo con dependencias está en [docs/planificacion.md](docs/planificacion.md).
