# Diseños de Pantalla: Vista y Edición de Perfil — Retroconver

**Responsable:** @DavidNC26  
**Tarea asociada:** #10  
**Dependencia:** E1-06 · [Diseño] Guía de estilo retro (`docs/estilo.md`)

---

## 1. Boceto 1: Vista del Perfil (Profile View)

La pantalla del Perfil emula las fichas personales de las redes sociales Web 2.0 (Tuenti y MySpace), manteniendo la paleta de color azul habitual (`#0066CC`), fuentes Verdana/Arial y bordes definidos.

### Estructura y Elementos Visuales:
- **Cabecera de Perfil:**
  - **Foto de Avatar:** Imagen cuadrada (100x100 px) con borde en gris azulado (`#B0C4DE`).
  - **Nombre de Usuario:** Texto destacado en negrita (`DavidNC26`) situado **justo al lado derecho de la foto de avatar**.
  - **Indicador de Estado (MSN Style):** Punto verde con el mensaje de estado ("*Programando la app nativa en Android Studio 🚀*").
- **Sección de Biografía y Datos:**
  - **Información Básica:** Edad, ciudad, fecha de registro e intereses.
  - **Colores de Acento Personalizados:** Tono de fondo asignado mediante la configuración personal almacenada en **DataStore**.
- **Tablón de Mensajes (Muro del Perfil):**
  - Formulario superior para publicar una nota rápida en el tablón del usuario.
  - Lista de mensajes dejados por contactos con avatar al lado del nombre, timestamp y botón de reacción "Molar" / "Me gusta" (`♥`).

---

## 2. Boceto 2: Formulario de Edición de Perfil (Profile Edit)

Esta pantalla permite al usuario modificar sus datos personales de forma local de cara a persistirlos en **Room** y **DataStore**.

### Estructura y Formulario de Edición:
- **Cambio de Avatar:**
  - Vista previa del avatar actual.
  - Botón *"Subir nueva foto"* (ejecuta el selector de imágenes locales restringiendo la resolución máxima a 720p).
- **Campos de Texto:**
  - **Nombre / Nickname:** Campo de texto con validación de longitud.
  - **Estado actual (Mensaje de estado estilo MSN):** `EditText` / `OutlinedTextField` de una línea para la "Frase del día".
  - **Biografía:** Campo de texto multilínea para la descripción personal.
- **Personalización de Tema (MySpace Style):**
  - Selector de color de acento (Azul clásico `#0066CC`, Verde MSN `#28A745`, Granate Retro `#800020`).
- **Botones de Acción:**
  - Botón de **"Guardar Cambios"** (actualiza la base de datos local y regresa a la vista de perfil).
  - Botón de **"Cancelar"**.

---

## 3. Ubicación de Artefactos e Imágenes
Los bocetos e interfaces de referencia están integrados y representados visualmente en:
- `docs/diseno/movilvisual/retrodiseñofinal.html` (Prototipo interactivo navegable para móvil).
- `docs/diseno/navegacion.md` (Diagrama de flujo entre vista y edición).
- `docs/diseno/recorrido.md` (Secuencia paso a paso de uso).
