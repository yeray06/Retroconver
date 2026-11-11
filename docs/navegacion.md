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
    Start([🚀 Arranque App / MainActivity]) -->|Con sesión| Muro
    Start -->|Sin sesión| Acceso[🔑 Inicio de sesión / Registro]
    Acceso -->|Entrar o crear cuenta| Muro

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

## 2. Acceso con cuenta (E4-04)

Desde la entrega 4 hay cuentas (E4-04, [#116](https://github.com/yeray06/Retroconver/issues/116)). Lo primero que decide la app es si hay sesión guardada en DataStore (`RetroconverRaiz`):

```mermaid
graph TD
    Arranque([Abrir la app]) --> Leer{¿Sesión guardada?}
    Leer -->|No| Inicio[🔑 Inicio de sesión]
    Leer -->|Sí| Muro[🏠 Muro]
    Inicio -->|Regístrate| Registro[📝 Registro]
    Registro -->|Entra| Inicio
    Inicio -->|Entrar| Muro
    Registro -->|Crear cuenta| Muro
    Registro -->|Si hay que confirmar el email| Correo[✉️ Revisa tu correo]
    Correo -->|Ir a entrar| Inicio
    Muro -.->|Perfil → Cerrar sesión| Inicio
    Muro -.->|La sesión caduca y no se puede renovar| Inicio
```

- **No se navega al entrar ni al salir.** Las pantallas de acceso y la app con la barra inferior son dos partes separadas. Al guardar o borrar la sesión cambia el estado y se muestra la otra parte. Así, después de cerrar sesión no se puede volver con «Atrás» al muro de la persona anterior.
- **Al cambiar de cuenta se empieza de cero:** la pila de navegación y los ViewModels se crean de nuevo.
- **Cerrar sesión** está al final del Perfil y pide confirmación, porque borra la copia de los datos del móvil (E4-04.3, [#119](https://github.com/yeray06/Retroconver/issues/119)).
