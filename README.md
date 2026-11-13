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
| Servidor Supabase | [supabase/README.md](supabase/README.md) |
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

**Entrega 4 · Versión candidata** (15/11/2026). Retroconver pasa a ser una red social de verdad, con cuentas y datos compartidos en un servidor Supabase (E4-01, [#109](https://github.com/yeray06/Retroconver/issues/109)).

### Qué funciona

| Qué | Dónde | Issue | Estado |
|---|---|---|---|
| Servidor Supabase (plan gratuito): tablas, políticas de seguridad (12/12 comprobaciones) e imágenes | `supabase/` | E4-02 [#110](https://github.com/yeray06/Retroconver/issues/110) | ✅ |
| Cliente de red con Retrofit: errores traducidos al español y franja de «Sin conexión» | `data/remote/` | E4-03 [#113](https://github.com/yeray06/Retroconver/issues/113) | ✅ ([#137](https://github.com/yeray06/Retroconver/pull/137)) |
| Registro, inicio de sesión que se mantiene al cerrar la app, renovación del token y cierre de sesión que borra los datos del móvil | `data/sesion/`, `ui/screens/acceso/` | E4-04 [#116](https://github.com/yeray06/Retroconver/issues/116) | ✅ ([#138](https://github.com/yeray06/Retroconver/pull/138)) |
| Room como copia del servidor (ids UUID, `@Upsert`); perfil y avatar guardados en el servidor | `data/local/`, `data/repository/UsuarioRepository.kt` | E4-05 [#120](https://github.com/yeray06/Retroconver/issues/120) | ✅ ([#139](https://github.com/yeray06/Retroconver/pull/139), [#140](https://github.com/yeray06/Retroconver/pull/140)) |
| Galería: álbumes con portada, cuadrícula, foto a pantalla completa, subir varias fotos y borrarlas; álbumes de otras personas en solo lectura | `ui/screens/galeria/`, `data/repository/GaleriaRepository.kt` | E4-10 [#21](https://github.com/yeray06/Retroconver/issues/21) | ✅ ([#141](https://github.com/yeray06/Retroconver/pull/141), [#142](https://github.com/yeray06/Retroconver/pull/142)) |
| 150 pruebas automáticas, revisión de accesibilidad e integración continua | `app/src/test/`, `app/src/androidTest/`, [docs/pruebas.md](docs/pruebas.md) | E4-12 [#24](https://github.com/yeray06/Retroconver/issues/24) | ✅ ([#135](https://github.com/yeray06/Retroconver/pull/135), [#143](https://github.com/yeray06/Retroconver/pull/143)) |
| De la entrega 3: perfil con edición validada, Photo Picker sin permisos, reescalado a 720p, nueva publicación y muro | `ui/screens/perfil/`, `ui/screens/muro/`, `data/imagenes/` | Entrega 3 | ✅ |

### Qué falta para la versión candidata

Son las tareas de David de la entrega 4. Ahora mismo las publicaciones y los comentarios todavía se guardan solo en el móvil.

| Tarea | Issue |
|---|---|
| Publicaciones compartidas en el servidor y muro de todas las personas | E4-06 [#123](https://github.com/yeray06/Retroconver/issues/123) |
| Comentarios | E4-07 [#18](https://github.com/yeray06/Retroconver/issues/18) |
| Perfiles de otras personas y buscador | E4-08 [#126](https://github.com/yeray06/Retroconver/issues/126) |
| Tablón de mensajes | E4-09 [#22](https://github.com/yeray06/Retroconver/issues/22) |
| Color de fondo y tema del perfil | E4-11 [#23](https://github.com/yeray06/Retroconver/issues/23) |
| Pruebas y accesibilidad de Muro y Mensajes | E4-13 [#25](https://github.com/yeray06/Retroconver/issues/25) |
| Memoria v4 | E4-14 [#81](https://github.com/yeray06/Retroconver/issues/81) |

Lo que ya está hecho les deja el camino preparado:
- El patrón para pasar un módulo al servidor está en [docs/arquitectura.md](docs/arquitectura.md), apartado 2.
- `UsuarioRepository.guardarDelServidor()` guarda los autores del muro.
- Los álbumes de otra persona se abren con `RutasGaleria.albumesDe(id)`.
- `UsuarioEntity` ya tiene `colorFondo` y `tema`.

### Qué se termina o corrige antes de la entrega final

- **Subir los esquemas de Room 3, 4 y 5** a `app/schemas/` ([#146](https://github.com/yeray06/Retroconver/issues/146)) y sustituir el borrado de la base de datos al actualizar por migraciones.
- **Subir fotos en segundo plano** con WorkManager, para que no se cancelen al salir del álbum ([#147](https://github.com/yeray06/Retroconver/issues/147)).
- **Probar con TalkBack, el Accessibility Scanner y letra grande** en un móvil de verdad ([docs/pruebas.md](docs/pruebas.md), apartado 4).
- Extras si hay tiempo: amistades ([#129](https://github.com/yeray06/Retroconver/issues/129)), «Me gusta» ([#130](https://github.com/yeray06/Retroconver/issues/130)) y mensajes directos ([#28](https://github.com/yeray06/Retroconver/issues/28)).

## Cómo probar la versión candidata (entrega 4)

Primero abre el proyecto y **conecta la app con el servidor** como se explica en [Cómo abrir y ejecutar el proyecto](#cómo-abrir-y-ejecutar-el-proyecto) (paso 4: `SUPABASE_URL` y `SUPABASE_ANON_KEY` en `local.properties`). Para crear el servidor desde cero, mira [supabase/README.md](supabase/README.md).

### 1. Registro e inicio de sesión

1. Abre la app. Sale **Entra en Retroconver** con la cabecera azul.
2. Pulsa **Regístrate** y luego **Crear cuenta** sin rellenar nada: cada campo dice qué le falta. Prueba un email sin dominio y una contraseña de 3 caracteres.
3. Rellena nombre, email y una contraseña de 6 o más caracteres: entras directamente al **Muro**. El proyecto tiene *Confirm email* desactivado, así que no hay que esperar ningún correo. Si alguien lo activa, la app muestra «Revisa tu correo» y hay que pulsar el enlace antes de entrar.
4. Cierra la app del todo y ábrela: **sigue la sesión**.
5. En Supabase, en *Authentication → Users*, está la cuenta, y en *Table Editor → perfiles*, su perfil con el nombre.

### 2. Perfil en el servidor

1. **Perfil → Editar perfil.** Cambia el estado y guarda. En *Table Editor → perfiles* se ve el cambio.
2. Activa el **modo avión**. Sale la franja amarilla de «Sin conexión» y el perfil se sigue viendo. Edita y guarda: sale «No hay conexión…» y lo escrito sigue en el formulario.
3. Quita el modo avión. **Cambiar foto**: el avatar se sube a *Storage → imagenes → (tu id)*. Si lo cambias otra vez, el anterior se borra del servidor.

### 3. Galería

1. **Galería → Nuevo álbum.** Si lo dejas sin nombre, avisa. Ponle «Viaje a Londres» y se abre vacío.
2. **Añadir fotos:** elige varias. Sale «Subiendo 1 de 3…» y aparecen en la cuadrícula de 3 columnas.
3. Pulsa una foto para verla a pantalla completa. **Borrar** pide confirmación, y la foto desaparece del álbum y de *Storage*.
4. Vuelve a la lista: el álbum tiene portada y número de fotos. Desliza hacia abajo para actualizar.

### 4. Cerrar sesión

1. **Perfil → Cerrar sesión** (al final) → confirmar. Vuelve al inicio de sesión y «Atrás» no lleva al muro.
2. Entra otra vez con la misma cuenta: el perfil y los álbumes vuelven, porque están en el servidor.

### Evidencias de Room, DataStore y el servidor

- **Room.** En *App Inspection → Database Inspector* están las tablas `usuarios`, `publicaciones`, `comentarios`, `albumes` y `fotos`, con los ids UUID del servidor. Al cerrar sesión quedan vacías.
- **DataStore.** En *Device Explorer*, `files/datastore/sesion.preferences_pb` guarda los tokens y el id de la cuenta, nunca la contraseña. Lo comprueba `SesionRepositoryTest`.
- **Servidor.** En el panel de Supabase: *Authentication* (cuentas), *Table Editor* (perfiles, álbumes y fotos) y *Storage → imagenes* (una carpeta por cuenta).
- **Pruebas.** Lista y resultados en [docs/pruebas.md](docs/pruebas.md). Cada PR las pasa en GitHub (ver [Integración continua](#integración-continua-el-check-de-las-pr)).

### Qué falla o tiene límites

- **Publicaciones y comentarios** todavía se guardan solo en el móvil hasta E4-06 y E4-07 (David).
- Si se sale del álbum **mientras se suben fotos**, las que faltan no se suben ([#147](https://github.com/yeray06/Retroconver/issues/147)).
- Las fotos **HEIC** no se pueden leer en Android 8.0 y 8.1. La app dice «No se ha podido usar esa foto». Desde Android 9 funcionan.
- Al actualizar la app con un esquema de base de datos nuevo se borra la copia local. No se pierde nada, porque está en el servidor, pero hay que estar conectado para volver a verlo.
- Si se borra una imagen del servidor sin conexión, puede quedar el archivo en *Storage* sin usar. No se ve en ningún sitio y no afecta a la app.
- El plan gratuito de Supabase **pausa el proyecto tras una semana sin uso**. Antes de cada entrega y de la presentación hay que abrirlo en el panel (*Restore project* si hace falta).

El estado al día de cada tarea está en el [tablero](https://github.com/users/yeray06/projects/2).

## Cómo trabajamos

- **Tablero Kanban** con las columnas Backlog, Por hacer, En curso, En revisión y Hecho.
- **105 issues en orden de avance.** Cada título empieza por un código: `E2-01` es la primera tarea principal de la entrega 2 y `E2-01.1` su primera subtarea. `EF` es la entrega final y `EX`, los extras. El tablero está ordenado por este código, así que se lee de arriba abajo en el orden en que hay que avanzar.
- **Errores encontrados al probar:** se registran como issues aparte con la etiqueta `bug`, en la entrega en la que se van a corregir (por ejemplo [#144](https://github.com/yeray06/Retroconver/issues/144)).
- **Tareas principales y subtareas.** Las tareas grandes se dividen en subtareas (*sub-issues*). Se trabaja sobre las subtareas, y la tarea principal se cierra cuando están todas terminadas. El tablero muestra el progreso de cada tarea principal.
- **Cada issue tiene** responsable, criterios de terminado, entrega (milestone) y dependencias (*Blocked by*).
- **Una rama por subtarea**, o por tarea si no tiene subtareas. La rama lleva el número de GitHub de la issue (`feature/53-vista-perfil`). Nunca se hacen commits directos a `main`.
- **Pull Request enlazada a la issue** (`Closes #53`). La revisa y la aprueba el otro integrante, y después la fusiona su autor. Las ramas no se borran: quedan como registro del trabajo.
- **Solo se fusiona con el check verde** de la integración continua: compila y pasan todas las pruebas ([más abajo](#integración-continua-el-check-de-las-pr)).
- **PR encadenadas.** Cuando una tarea necesita otra que aún está en revisión, su rama sale de la de esa tarea y la PR lo dice en «Depende de». Se fusionan en ese orden.

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
4. **Conecta la app con el servidor.** Abre el fichero `local.properties` de la raíz (lo crea Android Studio y Git no lo sube) y añade al final la URL y la clave pública del proyecto Supabase:
   ```properties
   SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOi...
   ```
   Están en el panel de Supabase, en *Project Settings → API*. Pídeselas a Yeray si no tienes acceso. Sin ellas la app abre igual, pero no puede conectar y lo dice al intentarlo. Más detalle en [supabase/README.md](supabase/README.md#conectar-la-app).
5. Elige la configuración **app** y tu emulador, y pulsa **▶ Run**.

### Pruebas

La lista de pruebas, su resultado y la revisión de accesibilidad están en [docs/pruebas.md](docs/pruebas.md). Las pruebas se lanzan con Gradle, no con el botón ▶ de la clase de prueba:

1. Abre el panel **Gradle** (el elefante de la derecha) y pulsa *Execute Gradle Task*.
2. Escribe `testDebugUnitTest` y pulsa Enter.
3. Tiene que terminar en **BUILD SUCCESSFUL**. El informe queda en `app/build/reports/tests/testDebugUnitTest/index.html`.

Las pruebas de la base de datos y de las imágenes necesitan Android, así que se ejecutan en el emulador. Con el emulador abierto, lanza la tarea `connectedDebugAndroidTest`. El informe queda en `app/build/reports/androidTests/connected/debug/index.html`.

### Integración continua (el check de las PR)

En cada Pull Request y en cada cambio de `main`, GitHub compila la app y pasa todas las pruebas (E4-12.3, [#134](https://github.com/yeray06/Retroconver/issues/134)). Lo hace `.github/workflows/android.yml` con dos trabajos:

| Trabajo | Qué hace | Tarda |
|---|---|---|
| **Compilar y pruebas unitarias** | `assembleDebug` y `testDebugUnitTest` | ~5 min |
| **Pruebas en el emulador** | Arranca un emulador con Android 14 y pasa `connectedDebugAndroidTest` (Room, imágenes, pantallas) | ~10 min |

En la PR, abajo del todo:

- ✅ **Check verde:** compila y pasan todas las pruebas. Se puede revisar y fusionar.
- ❌ **Check rojo:** algo no compila o alguna prueba falla. **No se fusiona.** Pulsa *Details*: arriba sale un resumen con los errores (*Errores de compilación o de pruebas*) y en *Summary → Artifacts* están los informes de las pruebas (`informe-pruebas-unitarias`, `informe-pruebas-emulador`).
- 🟡 **En curso:** hay que esperar a que termine.

Los esquemas de Room que genera cada compilación quedan en el artefacto `esquemas-room`. Si una PR sube la versión de la base de datos, hay que descargarlo y subir el `.json` nuevo a `app/schemas/`.

La CI no tiene `local.properties`, así que compila sin la URL ni la clave de Supabase. Las pruebas usan un servidor falso (MockWebServer) y no necesitan el de verdad.

### Estructura del código

```
app/src/main/java/com/retroconver/
├── RetroconverApp.kt   Application: crea el AppContainer
├── MainActivity.kt     Única Activity: tema, y acceso o app según haya sesión
├── AppContainer.kt     Base de datos, cliente de red y repositorios (una sola instancia)
├── data/local/         AppDatabase, entidades, DAOs y convertidores
├── data/remote/        Cliente de Supabase: servicios de Retrofit, errores de red y aviso sin conexión
├── data/sesion/        Sesión con Supabase Auth guardada en DataStore
├── data/repository/    Repositorios de cada módulo
├── data/imagenes/      Reescalado a 720p y copias de las imágenes
├── navigation/         Rutas, barra inferior, NavHost y RetroconverRaiz (acceso o app)
└── ui/
    ├── theme/          Tema retro: colores, tipografía y formas
    ├── components/     Componentes base: RetroHeader, RetroCard, RetroButton, Avatar y selector de imágenes
    └── screens/        Pantallas: acceso, perfil, muro, galeria y mensajes
supabase/               Esquema del servidor y prueba de seguridad
.github/workflows/      Integración continua
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
