# Modelo de Datos Local y Estrategia de Persistencia — Retroconver

**Responsable:** @DavidNC26  
**Tarea Principal:** #11 (E2-05) — Subtareas #44 y #45  
**Proyecto:** Retroconver (Android Native / Jetpack Compose)  
**Ubicación:** `docs/datos/esquemadatos.md` / Memoria v2  

---

## 1. Diagrama Entidad-Relación (Room Database)

El siguiente diagrama Mermaid define las 6 entidades del sistema, sus campos, tipos de datos en Kotlin/Room, claves primarias (`PK`) y claves foráneas (`FK`):

```mermaid
erDiagram
    USUARIOS ||--o{ PUBLICACIONES : "1:N (publica)"
    USUARIOS ||--o{ COMENTARIOS : "1:N (escribe)"
    USUARIOS ||--o{ ALBUMES : "1:N (propietario)"
    USUARIOS ||--o{ MENSAJES : "1:N (envia / recibe)"
    PUBLICACIONES ||--o{ COMENTARIOS : "1:N (tiene)"
    ALBUMES ||--o{ FOTOS : "1:N (contiene)"

    USUARIOS {
        long id PK "Autoincremental"
        string nombre "Nombre publico (obligatorio)"
        string avatarUri "Ruta foto local (720p)"
        string estadoMsn "Frase estilo MSN Messenger"
        string biografia "Descripcion perfil"
        long fechaAlta "Timestamp ms"
    }

    PUBLICACIONES {
        long id PK "Autoincremental"
        long usuarioId FK "FK -> USUARIOS.id (CASCADE)"
        string texto "Contenido del post"
        string imagenUri "Ruta imagen local (720p)"
        long timestamp "Ordenacion DESC"
        int contadorLikes "Solo si se anade el Me gusta"
    }

    COMENTARIOS {
        long id PK "Autoincremental"
        long publicacionId FK "FK -> PUBLICACIONES.id (CASCADE)"
        long usuarioId FK "FK -> USUARIOS.id (CASCADE)"
        string texto "Texto del comentario"
        long timestamp "Fecha/Hora"
    }

    ALBUMES {
        long id PK "Autoincremental"
        long usuarioId FK "FK -> USUARIOS.id (CASCADE)"
        string titulo "Nombre del album"
        string descripcion "Descripcion opcional"
        long fechaCreacion "Timestamp ms"
    }

    FOTOS {
        long id PK "Autoincremental"
        long albumId FK "FK -> ALBUMES.id (CASCADE)"
        string fotoUri "Ruta interna local (720p)"
        string pieDeFoto "Descripcion corta"
        long fechaAnadida "Timestamp ms"
        int ancho "Resolucion max 720/1280"
        int alto "Resolucion vertical"
    }

    MENSAJES {
        long id PK "Autoincremental"
        long emisorId FK "FK -> USUARIOS.id"
        long receptorId FK "FK -> USUARIOS.id"
        string contenido "Texto del mensaje / Tablon"
        long timestamp "Fecha/Hora"
        boolean esZumbido "Solo si entra el chat (RF-24)"
    }
```

---

## 2. Definición de Claves y Relaciones

### 2.1 Claves Primarias (`PrimaryKey`)
Todas las entidades utilizan una clave primaria de tipo `Long` autoincremental (`autoGenerate = true`):
* `USUARIOS.id`
* `PUBLICACIONES.id`
* `COMENTARIOS.id`
* `ALBUMES.id`
* `FOTOS.id`
* `MENSAJES.id`

### 2.2 Claves Foráneas (`ForeignKey`) e Integridad Referencial
1. **`PUBLICACIONES.usuarioId`** $\rightarrow$ `USUARIOS.id` (`onDelete = CASCADE`). Al eliminar un usuario se borran sus publicaciones.
2. **`COMENTARIOS.publicacionId`** $\rightarrow$ `PUBLICACIONES.id` (`onDelete = CASCADE`). Al eliminar un post se borran sus comentarios.
3. **`COMENTARIOS.usuarioId`** $\rightarrow$ `USUARIOS.id` (`onDelete = CASCADE`).
4. **`ALBUMES.usuarioId`** $\rightarrow$ `USUARIOS.id` (`onDelete = CASCADE`).
5. **`FOTOS.albumId`** $\rightarrow$ `ALBUMES.id` (`onDelete = CASCADE`). Al eliminar un álbum se borran sus fotos asociadas.
6. **`MENSAJES.emisorId`** y **`MENSAJES.receptorId`** $\rightarrow$ `USUARIOS.id`. Doble relación para conversaciones y mensajes del tablón.

---

## 3. Justificación Técnica: Room vs DataStore

| Tipo de Dato / Requisito | Tecnología Elegida | Justificación Técnica |
| :--- | :--- | :--- |
| **Entidades Relacionales (Posts, Comentarios, Álbumes, Fotos, Mensajes)** | **Room (SQLite)** | Datos con estructura relacional 1:N que requieren consultas complejas (`ORDER BY timestamp DESC`), claves foráneas con borrado en cascada (`CASCADE`) y reactividad mediante `Flow`. |
| **Metadatos de Usuario (ID, Nombre, Avatar)** | **Room (SQLite)** | Al actuar como clave foránea en publicaciones y comentarios, debe residir en la base de datos relacional. |
| **Tema elegido para el perfil (RF-08)** | **DataStore Preferences** | Ajustes clave-valor ligeros. Se leen asíncronamente al iniciar la app para aplicar el tema del perfil sin sobrecargar la base de datos. |
| **Color de Fondo del Perfil** | **DataStore Preferences** | Preferencia de UI simple sin relaciones con otras entidades. |
| **Filtro activo del Muro (Ver todo / Solo fotos), si se añade** | **DataStore Preferences** | Mantiene el estado de la UI seleccionado por el usuario entre sesiones. |
| **Archivos de Imagen a 720p** | **Almacenamiento Interno + Room** | Las imágenes físicas no se guardan como BLOB en la base de datos (para no saturar `CursorWindow`). Se procesan a 720p, se guardan en el almacenamiento interno privado y Room almacena únicamente la ruta/URI (`String`). |

---

## 4. Control de Revisión del Equipo

- [x] **Diseñado por:** @DavidNC26
- [x] **Revisado por:** @yeray06 (30/10/2026), antes de crear la primera entidad con DAO en E3-01 ([#13](https://github.com/yeray06/Retroconver/issues/13))

### Cambios de la revisión

1. **USUARIOS** usa `nombre` y `fechaAlta`, los mismos nombres que la entidad mínima que ya está en el código (`UsuarioEntity`, E2-03). Se quita `nombreUsuario` único porque no hay cuentas ni inicio de sesión (RF-04): el usuario local se crea solo al abrir la app.
2. **`PUBLICACIONES.contadorLikes`** y **`MENSAJES.esZumbido`** dependen de los [puntos a decidir](../requisitos.md#puntos-a-decidir-entre-los-dos). Si no se añade el «Me gusta» o el chat privado, esas columnas no se crean.
3. El número de comentarios de cada publicación (RF-14) y el número de fotos y la portada de cada álbum (RF-18) no se guardan en columnas: se calculan con una consulta (`COUNT`, primera foto del álbum), así no se desincronizan.
4. Se corrige la ubicación del documento y la issue de los DAOs (`#38` es E2-03.1, la base de datos).
