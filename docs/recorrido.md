# Recorrido Principal del Usuario (User Journey) — Retroconver

**Responsable:** @DavidNC26  
**Tareas asociadas:** #10 (E2-04.4 / #43), #48 (E2-07)  

---

## 1. Diagrama de Secuencia del Recorrido Principal

El siguiente diagrama de secuencia detalla las interacciones del usuario con la aplicación, la base de datos local (Room) y el sistema de almacenamiento de imágenes a 720p:

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant App as App Retroconver (Jetpack Compose)
    participant DB as Base de Datos Local (Room)
    participant FS as Almacenamiento Local (File Storage 720p)

    Note over U, FS: 1. APERTURA DE LA APLICACIÓN
    U->>App: Abre Retroconver
    App->>DB: Consulta perfil del usuario local y publicaciones del muro
    DB-->>App: Retorna datos guardados
    App-->>U: Muestra Muro Principal ('MOMENTOS')

    Note over U, FS: 2. EDICIÓN DEL PERFIL
    U->>App: Pulsa icono '👤 Perfil' en la barra inferior
    App-->>U: Muestra pantalla de Perfil (Ficha superior iPod Touch)
    U->>App: Pulsa icono lápiz ('✏️') en la burbuja de estado
    App-->>U: Despliega el formulario 'Editar Estado'
    U->>App: Introduce texto: "el nuevo año promete! :D" y pulsa 'Guardar'
    App->>DB: Actualiza el estado personal en Room
    App-->>U: Cierra formulario y actualiza la burbuja del perfil

    Note over U, FS: 3. CREACIÓN DE UNA PUBLICACIÓN
    U->>App: Pulsa icono '🏠 Muro' (o el botón '+' de la cabecera)
    App-->>U: Muestra el Muro Principal y despliega 'Nuevo Momento'
    U->>App: Escribe el texto de la publicación
    U->>App: Pulsa '🖼️ Adjuntar Imagen'
    App->>FS: Llama al PhotoPicker de Android y reescala la imagen a 720p máx.
    FS-->>App: Retorna la URI local de la imagen procesada
    U->>App: Pulsa botón 'Publicar'
    App->>DB: Guarda la publicación (texto + ruta imagen) en Room

    Note over U, FS: 4. VISUALIZACIÓN EN EL MURO
    App-->>U: Refresca el Muro cronológico mostrando la nueva tarjeta en primer lugar
