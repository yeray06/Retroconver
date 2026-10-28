# Diagrama de Navegación y Arquitectura de Pantallas — Retroconver

**Responsable:** @DavidNC26  
**Tareas asociadas:** #10 (E2-04.4 / #43), #48 (E2-07)  
**Proyecto:** Retroconver (Android Native / Jetpack Compose)

---

## 1. Mapa Global de Navegación (Diagrama Mermaid)

El siguiente flujo representa la arquitectura completa de pantallas de Retroconver y las vías de transición entre ellas a través de la barra de navegación inferior (`BottomNavigation`), pestañas secundarias y componentes modales/desplegables:

```mermaid
graph TD
    %% Nodos principales de la app
    Start([🚀 Arranque App / MainActivity]) -->|Navigation Compose| Muro

    subgraph BARRA_INFERIOR [Barra de Navegación Inferior - BottomNav]
        Muro[🏠 Muro / Momentos]
        Perfil[👤 Perfil de Usuario]
        Galeria[🖼️ Galería / Fotos]
        Mensajes[💬 Chats / Mensajes]
    end

    %% Transiciones del Muro
    Muro -->|Click '+' en Cabecera| FormPublicar[📝 Formulario Nueva Publicación]
    FormPublicar -->|Adjuntar Imagen| PhotoPicker1[📷 PhotoPicker Android 720p]
    FormPublicar -->|Publicar / Cancelar| Muro
    Muro -->|Click en '♥' / Comentar| ReaccionPost[💖 Reacción o Comentarios]

    %% Transiciones del Perfil
    Perfil -->|Click '✏️' en Burbuja| EditEstado[✏️ Formulario Editar Estado/Perfil]
    EditEstado -->|Guardar / Cancelar| Perfil
    Perfil -->|Pestaña 'Tablón'| TablonPerfil[📜 Tablón de Mensajes]
    Perfil -->|Pestaña 'Fotos'| FotosPerfil[🖼️ Álbumes Rápidos del Perfil]
    Perfil -->|Pestaña 'Información'| InfoPerfil[ℹ️ Datos de Usuario]

    %% Transiciones de la Galería
    Galeria -->|Click en Álbum| DetalleAlbum[GRID 3x3 Detalle del Álbum]
    DetalleAlbum -->|Click '← Volver'| Galeria
    DetalleAlbum -->|Click en Foto| LightboxFoto[🔍 Vista Ampliada Foto 720p]
    Galeria -->|Crear Álbum| FormAlbum[➕ Formulario Nuevo Álbum]

    %% Transiciones de Mensajes / Chat
    Mensajes -->|Click en Contacto| ConversacionChat[💬 Vista de Conversación]
    Mensajes -->|Click '🔔 Enviar Zumbido'| AnimacionZumbido[📳 Efecto Zumbido MSN]

    %% Estilos retro
    classDef mainApp fill:#004085,stroke:#001a38,color:#fff,stroke-width:2px;
    classDef modalStyle fill:#e6f0fa,stroke:#2b82df,color:#000,stroke-width:1px;
    class Muro,Perfil,Galeria,Mensajes mainApp;
    class FormPublicar,EditEstado,DetalleAlbum,ConversacionChat modalStyle;
