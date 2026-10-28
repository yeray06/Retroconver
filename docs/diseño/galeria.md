# Diseños de Pantalla: Lista de Álbumes y Detalle de Fotos — Retroconver

**Responsable:** @DavidNC26  
**Tarea asociada:** #10  
**Dependencia:** E1-06 · Guía de estilo retro (`docs/estilo.md`)

---

## 1. Boceto 1: Lista de Álbumes (Album List View)

La vista principal de la sección de fotos muestra los diferentes álbumes organizados por el usuario, emulando la estructura clásica de Tuenti Web 2.0 (2008–2009).

### Elementos Visuales:
- **Cabecera Glossy:** Título centrado con el nombre de la sección y barra de navegación inferior activa en la pestaña de Galería/Fotos.
- **Tarjetas de Álbum:**
  - **Miniatura de Portada:** Cuadro de previsualización con borde fino y sombra sutil.
  - **Metadatos del Álbum:** Título del álbum en negrita (ej. *"Fiesta Nochevieja 2008"*, *"Viaje a Londres"*), contador de elementos fotográficos y fecha de última modificación.
  - **Acción de Apertura:** Al hacer clic sobre cualquier álbum, se transiciona a la vista en cuadrícula de su contenido.

---

## 2. Boceto 2: Detalle de Álbum en Cuadrícula (Album Grid View)

Vista detallada que muestra el contenido fotográfico de un álbum seleccionado organizado en formato de rejilla (*grid*).

### Estructura de la Cuadrícula:
- **Cabecera de Navegación:** Botón de retroceso (*"← Volver a Álbumes"*) y título del álbum activo.
- **Rejilla de Fotografías (Grid Layout):**
  - Cuadrícula de 3 columnas optimizada para pantallas móviles.
  - Restricción estricta de renderizado e importación a **720p máx.** para cumplir con los requisitos de optimización del almacenamiento local (Room / DataStore).
- **Vista Ampliada (Lightbox Modal):** Al pulsar sobre cualquier miniatura de la cuadrícula, la imagen se despliega a pantalla completa con sus botones de interacción y comentarios asociados.

---
