# Diagrama de Navegación — Retroconver

El flujo de navegación de la aplicación está diseñado mediante una barra inferior de pestañas (Bottom Navigation) accesible tras la autenticación, emulando la simplicidad de las apps clásicas.

```text
[ Pantalla de Login / Autenticación Local ]
                   │
                   ▼ (Inicio de sesión exitoso)
          [ Muro / Blog Principal ] <════════════════════════════╗
                   ║                                             ║
       ┌───────────┼───────────┬───────────┐                     ║
       ▼           ▼           ▼           ▼                     ║
   [ Perfil ]  [ Galería ] [ Mensajes ]  [ Crear Post ] ─────────╝
  (Personalizar (720p &     (Chat &       (Regresa al Muro)
    temas)      Permisos)   Zumbidos)
