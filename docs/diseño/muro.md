# Diseños de Pantalla: Muro Principal y Formulario de Publicación — Retroconver

**Responsable:** @DavidNC26  
**Tarea asociada:** #10  
**Dependencia:** E1-06 · Guía de estilo retro (`docs/estilo.md`)

---

## 1. Boceto 1: Muro Principal con Publicaciones y Estado Vacío

La pantalla de **Muro / Momentos** representa el núcleo de la interacción social de la app, emulando el *feed* clásico de Tuenti (2008–2009).

### Elementos Visuales:
- **Cabecera Glossy:** Barra superior azul degradada con el logo `retroconver` en minúsculas y un botón de acción rápida (`+`) para crear una nueva publicación.
- **Pestañas de Navegación Secundaria:** Selector de pestañas `CHAT` y `MOMENTOS` (Muro activo).
- **Tarjetas de Publicación (`.post-card`):**
  - **Cabecera del post:** Avatar del usuario con borde fino, nombre en negrita y enlace directo al perfil, acompañado de la marca temporal (*timestamp*, ej: "hace 1m").
  - **Cuerpo del texto:** Tipografía Verdana/Arial optimizada para legibilidad retro.
  - **Contenido multimedia:** Fotografías incrustadas con restricción estricta a **720p máx.** para asegurar la fidelidad técnica y optimizar el almacenamiento local con Room.
  - **Barra de reacciones:** Botones interactivos con borde discontinuo (*dashed*), contador de comentarios y botón de "Me gusta" con icono de corazón (`♥`).
- **Estado Vacío (Empty State):**
  - Vista controlada cuando el feed de publicaciones no contiene elementos o se queda sin conexión, mostrando un mensaje amigable al estilo retro invitando a romper el hielo publicando el primer "momento".

---

## 2. Boceto 2: Formulario de Nueva Publicación con Imagen

Pantalla modal o vista dedicada para la creación de contenido por parte del usuario.

### Estructura del Formulario:
- **Área de Texto (Caption):** Campo de entrada multilinea (*EditText*) optimizado para escribir pensamientos o estados rápidos.
- **Selector / Subida de Imagen:**
  - Botón de adjuntar imagen local desde el dispositivo.
  - Previsualización (*Thumbnail*) de la imagen seleccionada antes de publicar.
  - Restricción lógica integrada para evitar subidas que superen los 720p (cumpliendo con los requisitos del TFG).
- **Botones de Acción:**
  - **Publicar:** Botón con relieve y degradado azul clásico que valida el contenido y lo inserta en la base de datos local (Room).
  - **Cancelar:** Cierra el formulario y regresa al muro principal sin guardar.
---
