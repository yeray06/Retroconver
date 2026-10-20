# Retroconver

Red social para Android con la estética y la sencillez de las redes de finales de los 2000, inspirada en la primera época de Tuenti y en la personalización de MySpace.

**Proyecto Integrador · Desarrollo Móvil · 1º DAM**

| | |
|---|---|
| Tablero Kanban | [GitHub Project · Retroconver](https://github.com/users/yeray06/projects/2) |
| Propuesta inicial | [docs/propuesta/Retroconver.pdf](docs/propuesta/Retroconver.pdf) |
| Planificación | [docs/planificacion.md](docs/planificacion.md) |
| Cómo trabajamos | [CONTRIBUTING.md](CONTRIBUTING.md) |

## Equipo

| Integrante | GitHub | Se encarga de |
|---|---|---|
| Yeray Sánchez | [@yeray06](https://github.com/yeray06) | Base del proyecto, navegación, Room, módulo **Perfil** y módulo **Galería** |
| David Nicolás | [@DavidNC26](https://github.com/DavidNC26) | Estilo retro y tema de Compose, módulo **Muro** (publicaciones y comentarios) y módulo **Mensajes** |

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

- Mensajes directos entre usuarios ([#28](https://github.com/yeray06/Retroconver/issues/28))
- Comentarios anidados o respuestas ([#29](https://github.com/yeray06/Retroconver/issues/29))
- Temas clásicos y tipografías seleccionables por perfil ([#30](https://github.com/yeray06/Retroconver/issues/30))
- Vídeos cortos, también limitados a 720p

### Fuera de alcance

- Servidor, registro online y sincronización entre dispositivos
- Notificaciones push

## Tecnologías previstas

| Tecnología | Para qué la usaremos |
|---|---|
| Kotlin | Lenguaje de toda la aplicación |
| Jetpack Compose | Interfaz y tema visual retro |
| Navigation Compose | Navegación entre Perfil, Muro, Galería y Mensajes |
| Room | Usuarios, publicaciones, comentarios, álbumes, fotos y mensajes |
| DataStore | Preferencias del perfil: color de fondo, tema y ajustes |
| Photo Picker de Android | Elegir imágenes del dispositivo pidiendo solo los permisos necesarios |

## Estado actual

**Entrega 1 · Organización.** Repositorio, tablero Kanban, issues, planificación, README y primera versión de la memoria.

Todavía no hay código. El proyecto Android se crea en la entrega 2 ([#7](https://github.com/yeray06/Retroconver/issues/7)), y las instrucciones para ejecutarlo se añadirán aquí entonces.

## Cómo trabajamos

- **Tablero Kanban** con las columnas Backlog, Por hacer, En curso, En revisión y Hecho.
- **Una issue por tarea**, con responsable, criterios de terminado y dependencias.
- **Una rama por issue** (`feature/14-pantalla-perfil`, `docs/5-memoria`…), nunca commits directos a `main`.
- **Pull Request enlazada a la issue** (`Closes #14`). La revisa y la aprueba el otro integrante.

Los detalles están en [CONTRIBUTING.md](CONTRIBUTING.md).

## Planificación

| Entrega | Fecha límite | Objetivo |
|---|---|---|
| 1 · Organización | 25/10/2026 | Repo, Kanban, issues, planificación, README y memoria v1 |
| 2 · Diseño y base técnica | 01/11/2026 | Estilo, bocetos, esquema de datos, proyecto Android, navegación y Room |
| 3 · Flujo principal | 08/11/2026 | Perfil, muro y publicaciones con imagen funcionando en local |
| 4 · Versión candidata | 15/11/2026 | Álbumes, tablón, comentarios, personalización y pruebas |
| Final | 20/11/2026 | APK, README definitivo, memoria final y presentación |

El reparto completo con dependencias está en [docs/planificacion.md](docs/planificacion.md).
