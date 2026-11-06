# Servidor Supabase de Retroconver

Retroconver guarda las cuentas, los datos y las imágenes en un proyecto [Supabase](https://supabase.com) con el **plan gratuito** (E4-02, [#110](https://github.com/yeray06/Retroconver/issues/110)). Esta carpeta tiene todo lo necesario para crear el servidor desde cero.

| Fichero | Qué hace |
|---|---|
| `esquema.sql` | Crea las tablas, el perfil automático al registrarse, las políticas de seguridad y el bucket de imágenes. Se puede ejecutar varias veces sin perder datos |
| `pruebas_seguridad.sql` | Comprueba con dos cuentas que cada una solo puede tocar lo suyo. No deja nada guardado |

El porqué de cada tabla y de cada política está en [docs/datos/esquemadatos.md](../docs/datos/esquemadatos.md#5-servidor-supabase-entrega-4).

## Crear el servidor desde cero

1. Entra en [supabase.com](https://supabase.com) → *Start your project* → *Continue with GitHub*.
2. Crea una organización con el plan **Free** (no pide tarjeta) y un proyecto nuevo:
   - **Nombre:** `retroconver`.
   - **Región:** *West EU (Paris)*.
   - **Database password:** pulsa *Generate a password* y guárdala en un sitio seguro. La app no la usa, pero hace falta para administrar la base de datos.
3. En **Authentication → Sign In / Providers → Email**, desactiva **Confirm email**. El correo gratuito de Supabase envía muy pocos emails por hora, y con la confirmación activada no se podrían crear cuentas de prueba.
4. En **SQL Editor → New query**, pega el contenido de `esquema.sql` y pulsa **Run**. Tiene que terminar con *Success. No rows returned*.
5. Para comprobar la seguridad:
   1. En **Authentication → Users → Add user → Create new user**, crea dos cuentas de prueba con *Auto Confirm User*.
   2. Ejecuta `pruebas_seguridad.sql` en el SQL Editor. Tiene que salir una tabla con las 11 comprobaciones y ninguna línea que empiece por FALLO.

## Conectar la app

La app necesita la URL del proyecto y la clave pública (*anon key*). Están en **Project Settings → API**: *Project URL* y *Project API keys → anon public*.

Ponlas en el fichero `local.properties` de la raíz del proyecto, que Git no sube al repositorio:

```properties
SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi...
```

La clave *anon* está pensada para ir dentro de la app: no da acceso a nada que no permitan las políticas de seguridad. La que **nunca** se pone en la app ni en el repositorio es la *service_role*, que se salta todas las políticas.

## Límites del plan gratuito

| Qué | Límite |
|---|---|
| Base de datos | 500 MB |
| Imágenes | 1 GB en total y 2 MB por archivo (límite del bucket) |
| Cuentas activas al mes | 50.000 |
| Proyectos activos | 2 por organización: queda sitio para otra app |

- **Pausa por inactividad.** Si el proyecto pasa una semana sin uso, Supabase lo pausa y la app deja de conectar. Se reactiva en el panel con *Restore project*. Hay que comprobarlo antes de cada entrega y de la presentación.
- **No se cobra nada.** En el plan Free no hay tarjeta asociada. Si se llega a un límite, Supabase restringe el proyecto en vez de cobrar.
