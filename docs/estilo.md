# Guía de Estilo Visual — Retroconver

## 1. Paleta de Colores

| Elemento | Descripción | Código HEX |
| :--- | :--- | :--- |
| **Azul Tuenti (Primario)** | Color principal de cabeceras, botones y acentos | `#0066CC` |
| **Azul Claro (Secundario)** | Fondos de tarjetas y contenedores de mensajes | `#E6F2FF` |
| **Fondo General** | Fondo retro grisáceo claro / azul muy tenue | `#F4F7FA` |
| **Gris Borde** | Líneas divisoras y marcos de fotos/avatar | `#CCCCCC` |
| **Texto Principal** | Texto de publicaciones y nombres de usuario | `#222222` |
| **Texto Secundario** | Fechas, estados y detalles | `#666666` |

---

## 2. Tipografías

* **Titulares y Nombres de Usuario:** `Arial, sans-serif` (negrita) — Para emular la simplicidad de la Web 2.0.
* **Cuerpo de Texto y Mensajes:** `Verdana, sans-serif` — Tipografía clásica de lectura web en los años 2000.

---

## 3. Referencias Visuales (Años 2000)

1. **Tuenti (2008 - 2010):**
   * *Elementos adoptados:* Estructura del muro personal, cabecera azul superior con pestañas sencillas, distribución del perfil en columna izquierda (avatar + datos) y columna derecha (muro + fotos).
2. **MySpace (2005 - 2007):**
   * *Elementos adoptados:* Personalización de temas/colores en el perfil del usuario (mediante preferencias) y caja de estado visible en la cabecera del perfil.
3. **MSN Messenger (2007):**
   * *Elementos adoptados:* Indicador de estado (conectado/ausente o frase del día) y avatares con marco cuadrado retro.

---

## 4. Componentes Base (Jetpack Compose)

* **Cabecera (`RetroHeader`):** Barra superior de color azul Tuenti (`#0066CC`) con el logo en texto blanco y botones planos de navegación.
* **Avatar (`RetroAvatar`):** Imagen de perfil cuadrada o ligeramente redondeada con un borde sólido de 1-2dp en tono gris/azul.
* **Tarjeta de Muro (`RetroCard`):** Contenedor blanco con borde suave `#CCCCCC` para alojar publicaciones, comentarios y fotos.
* **Botones (`RetroButton`):** Botones rectangulares de estilo clásico Web 2.0 con texto en mayúsculas o negrita.
