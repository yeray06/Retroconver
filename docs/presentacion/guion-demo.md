# Guion de la presentación y de la demo

Exposición de Retroconver (EF-02, [#27](https://github.com/yeray06/Retroconver/issues/27)): 10 minutos y después 5 de preguntas. Las diapositivas están en [Retroconver_Presentacion.pptx](Retroconver_Presentacion.pptx) y cada una lleva su guion en las notas.

## Antes de empezar

- [ ] Abrir el proyecto en el panel de Supabase para que no esté pausado (*Restore project* si hace falta).
- [ ] Dos móviles (o dos emuladores) con el APK de la Release `v1.0`: el **A** de Yeray y el **B** de David.
- [ ] Cada uno con una cuenta de prueba y la sesión iniciada. Las dos cuentas **no** son amigas todavía.
- [ ] Una foto en la galería del móvil B para publicarla.
- [ ] El README abierto en el portátil, por si algo falla en directo: tiene las capturas.

## Tiempos

| Diapositiva | Quién | Tiempo |
|---|---|---|
| 1. Portada | Yeray | 0:30 |
| 2. El problema | David | 1:00 |
| 3. La solución | Yeray | 1:00 |
| 4. Arquitectura con servidor | Yeray | 1:30 |
| 5. Pruebas y calidad | David | 1:00 |
| 6. Demo con dos móviles | Los dos | 4:00 |
| 7. Reparto | Los dos | 0:30 |
| 8. Qué hemos aprendido | Yeray | 0:30 |
| **Total** | | **10:00** |

## Demo (4 minutos)

| Paso | Móvil | Qué se hace | Qué se ve |
|---|---|---|---|
| 1 | A | **Perfil → Buscar amigos**, escribe el nombre de B, **Añadir** | «Solicitud enviada» |
| 2 | B | **Perfil → Solicitudes de amistad → Aceptar** | B pasa a **Amigos (1)** |
| 3 | B | **Muro → + →** texto y la foto **→ Publicar** | La publicación arriba del muro |
| 4 | A | Desliza el muro hacia abajo | Sale la foto de B (antes de ser amigos no salía) |
| 5 | A | Pulsa **♡** en la publicación de B, ábrela y comenta | **♥ 1** en rojo; el comentario y el contador suben |
| 6 | A | Toca el nombre de B | Su perfil, con su tema y su fondo |
| 7 | A | En el **Tablón** de B escribe «¡Hola desde la demo!» **→ Publicar en el tablón** | El mensaje arriba del tablón |
| 8 | B | **Mensajes** | El mensaje de A en el tablón; B lo puede borrar |
| 9 | A | En el perfil de B, **Mensaje privado**: «¿Has visto el tablón?» **→ Enviar** | El mensaje a la derecha, en azul |
| 10 | B | **Mensajes → Conversaciones privadas →** A, y pulsa **Enviar un zumbido** | El chat de los dos tiembla y vibra; «B te ha enviado un zumbido» |
| 11 | B | **Perfil → Editar perfil → Aspecto**: tema **Noche** y fondo **Verde menta → Guardar** | La vista previa cambia antes de guardar |
| 12 | A | Pulsa **Volver** y abre otra vez el perfil de B | El perfil de B con el tema nuevo (se descarga al abrirlo) |
| 13 | A | Activa el **modo avión** | Franja amarilla «Sin conexión»; se sigue viendo todo |

## Ensayo

Ensayado con cronómetro: hay que quedar por debajo de 10 minutos. Si la demo se alarga, se saltan los pasos 11 y 12.

## Preguntas que pueden hacer

- **¿Por qué Supabase y no Firebase?** Gratis y sin tarjeta también para las imágenes, y es SQL con claves foráneas, como Room.
- **¿Qué pasa sin conexión?** Las pantallas leen de Room: se ve lo último descargado. Para escribir hace falta red, y la app lo dice.
- **¿Cómo evitáis que alguien borre lo de otro?** Con las políticas RLS del servidor: la clave de la app es pública, así que la seguridad no puede estar solo en la app. Lo prueba `supabase/pruebas_seguridad.sql` (29 comprobaciones, también que solo se pueda escribir por privado a un amigo).
- **¿El chat es en tiempo real?** No: mientras está abierto pregunta al servidor cada 4 segundos. Con Supabase Realtime sería inmediato; está en las mejoras futuras.
- **¿Cómo sabéis que no rompéis nada?** 287 pruebas que pasan en cada Pull Request, con un emulador y comprobaciones de accesibilidad.
- **¿Por qué 720p?** Ahorra espacio y datos y encaja con la estética de los móviles de la época.
