# [Diseño] Tema retro en Compose (colores, tipografía y componentes base)

**Código de Tarea:** E2-06  
**Issue:** #12  
**Responsable:** @DavidNC26  
**Milestone:** Entrega 2 · Diseño y base técnica  

---

### Descripción
Trasladar la guía de estilo visual y el maquetado del prototipo final (`retroconverdiseñofinal.html`) a un tema reutilizable de Jetpack Compose (`RetroTheme`) y desarrollar los componentes UI base que usarán todas las pantallas de la aplicación.

---

### Criterios de Terminado (Acceptance Criteria)
- [x] **Paleta de Colores y Tipografía (`RetroTheme.kt`):**
  - Degradado Glossy en cabeceras: `#2B82DF` ➔ `#0056B3` ➔ `#004085` ➔ `#002D5E`.
  - Fondo general de pantalla: `#C5D7E8`.
  - Tarjeta de estado Perfil / iPod Touch: Fondo `#E6F0FA` con borde `#A3C2E0`.
  - Tarjetas del Muro / Galerías: Fondo blanco `#FFFFFF` con borde `#B8CEEE` o `#CCCCCC`.
  - Tipografía sans-serif de alta legibilidad (`Arial` / `Verdana`).
- [x] **Componentes Base Reutilizables:**
  - `RetroHeader`: Cabecera Glossy con título `retroconver`, pestañas de navegación secundaria (`CHAT` / `MOMENTOS`) y botón flotante de acción rápida (`+`).
  - `RetroCard`: Contenedor base de tarjeta blanca con bordes definidos y ligera sombra 1dp.
  - `RetroButton` / `CounterBtn`: Botones de interacción ligera (Likes `♥` y Comentarios `💬`) con borde discontinuo (`dashed`).
  - `RetroAvatar`: Imagen de avatar cuadrada con borde gris simple (1px).
  - `RetroSegmentedControl`: Selector de pestañas para el perfil estilo iPod Touch (*Tablón*, *Fotos*, *Información*).
- [x] **Aplicación:** Integrado en las pantallas principales (`MuroScreen`, `PerfilScreen`, `GaleriaScreen`, `MensajesScreen`).

---

### Dependencias
- **Blocked by:** #6 — E1-06 · [Diseño] Guía de estilo retro
- **Blocked by:** #7 — E2-01 · [Base] Crear proyecto Android y estructura de paquetes
