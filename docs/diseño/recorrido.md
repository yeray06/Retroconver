# Recorrido Principal — Retroconver

Este documento describe el flujo de uso principal que experimentará el usuario desde que abre la aplicación por primera vez hasta que interactúa con las funciones clave del MVP.

## Paso 1: Autenticación y Acceso
- El usuario abre la aplicación en su dispositivo Android.
- Se muestra una pantalla de bienvenida limpia con la identidad visual clásica de Tuenti.
- Introduce sus credenciales de acceso (validadas contra la base de datos local **Room**). Al pulsar "Entrar", la aplicación le redirige al **Muro Principal**.

## Paso 2: Consulta e Interacción en el Muro
- Una vez en el Muro, el usuario visualiza las publicaciones recientes de sus contactos.
- Cada publicación muestra de forma clara el **avatar del usuario alineado junto a su nombre**, seguido de la fecha y el contenido (texto o imagen optimizada a 720p).
- El usuario puede hacer clic en "Molar" o desplegar la sección de comentarios para participar en el hilo de texto.

## Paso 3: Personalización del Perfil
- Mediante la barra de navegación inferior, el usuario pulsa en **Perfil**.
- Se carga su vista personalizada: a la izquierda su foto de avatar y su estado de texto ("Frase del día"), y a la derecha su muro personal y gustos.
- El usuario cambia los colores de acento de su perfil (simulando la personalización al estilo MySpace almacenada localmente mediante **DataStore**).

## Paso 4: Exploración de la Galería Multimedia
- El usuario navega a la pestaña de **Galería**.
- Se visualiza una cuadrícula de álbumes fotográficos. 
- Al pulsar el botón "Subir nueva foto", la aplicación solicita un **permiso puntual de lectura de almacenamiento local** al sistema Android. Tras concederlo, selecciona una imagen que se redimensiona automáticamente al estándar de **720p** antes de guardarse localmente en la base de datos.

## Paso 5: Mensajería e Interacción Estilo MSN
- El usuario accede a la pestaña de **Mensajes**.
- Selecciona a un contacto de su lista para abrir la ventana de chat privado.
- Envía mensajes de texto que se almacenan de manera persistente en Room.
- Si lo desea, pulsa el botón de **"Zumbido"**, lo que provoca una vibración simulada en la pantalla y reproduce un sonido retro nostálgico, emulando la clásica experiencia de MSN Messenger.
