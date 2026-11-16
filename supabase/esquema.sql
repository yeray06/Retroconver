-- =====================================================================
-- Retroconver · esquema del servidor Supabase
-- E4-02.1 (#111): tablas y relaciones · E4-02.2 (#112): seguridad e imágenes
-- EX-03 (#30): tipografía del perfil · EX-04 (#129): amistades
--
-- Cómo usarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Se puede ejecutar las veces que haga falta: crea lo que falta y
-- actualiza funciones y políticas, pero nunca borra datos.
-- =====================================================================


-- =====================================================================
-- 1. Tablas
-- Claves UUID para que no choquen las que crean distintos móviles.
-- Los límites de texto son los mismos que valida la app.
-- =====================================================================

-- Perfil público de cada cuenta. La cuenta (email y contraseña) está en auth.users.
create table if not exists public.perfiles (
    id          uuid primary key references auth.users (id) on delete cascade,
    nombre      text not null check (char_length(btrim(nombre)) between 1 and 30),
    biografia   text not null default '' check (char_length(biografia) <= 200),
    estado      text not null default '' check (char_length(estado) <= 80),
    avatar_ruta text,                 -- ruta dentro del bucket imagenes
    color_fondo text,                 -- RF-08, E4-11
    tema        text,                 -- RF-08, E4-11 y EX-03 (clave del tema)
    tipografia  text check (tipografia in ('normal', 'clasica', 'maquina', 'manuscrita')), -- EX-03
    creado_en   timestamptz not null default now()
);

-- Columnas añadidas después de crear la tabla: así el script también pone al día un proyecto que ya existía
alter table public.perfiles add column if not exists tipografia text
    check (tipografia in ('normal', 'clasica', 'maquina', 'manuscrita'));

create table if not exists public.publicaciones (
    id          uuid primary key default gen_random_uuid(),
    autor_id    uuid not null default auth.uid() references public.perfiles (id) on delete cascade,
    texto       text not null check (char_length(btrim(texto)) between 1 and 500),
    imagen_ruta text,
    creada_en   timestamptz not null default now()
);

create table if not exists public.comentarios (
    id             uuid primary key default gen_random_uuid(),
    publicacion_id uuid not null references public.publicaciones (id) on delete cascade,
    autor_id       uuid not null default auth.uid() references public.perfiles (id) on delete cascade,
    texto          text not null check (char_length(btrim(texto)) between 1 and 300),
    creado_en      timestamptz not null default now()
);

-- Tablón: perfil_id es el dueño del tablón y autor_id quien escribe.
create table if not exists public.mensajes (
    id        uuid primary key default gen_random_uuid(),
    perfil_id uuid not null references public.perfiles (id) on delete cascade,
    autor_id  uuid not null default auth.uid() references public.perfiles (id) on delete cascade,
    texto     text not null check (char_length(btrim(texto)) between 1 and 300),
    creado_en timestamptz not null default now()
);

create table if not exists public.albumes (
    id             uuid primary key default gen_random_uuid(),
    propietario_id uuid not null default auth.uid() references public.perfiles (id) on delete cascade,
    titulo         text not null check (char_length(btrim(titulo)) between 1 and 40),
    descripcion    text not null default '' check (char_length(descripcion) <= 200),
    creado_en      timestamptz not null default now()
);

create table if not exists public.fotos (
    id             uuid primary key default gen_random_uuid(),
    album_id       uuid not null references public.albumes (id) on delete cascade,
    propietario_id uuid not null default auth.uid() references public.perfiles (id) on delete cascade,
    ruta           text not null,
    ancho          integer check (ancho > 0),
    alto           integer check (alto > 0),
    creada_en      timestamptz not null default now()
);

-- Amistades (EX-04): quien pide es solicitante_id y destinatario_id la acepta o la rechaza.
-- Mientras aceptada = false es una solicitud pendiente. Una sola fila por pareja, la pida quien la pida.
create table if not exists public.amistades (
    id              uuid primary key default gen_random_uuid(),
    solicitante_id  uuid not null default auth.uid() references public.perfiles (id) on delete cascade,
    destinatario_id uuid not null references public.perfiles (id) on delete cascade,
    aceptada        boolean not null default false,
    creada_en       timestamptz not null default now(),
    check (solicitante_id <> destinatario_id)
);

-- Índices para las consultas de cada pantalla (por fecha y por clave foránea)
create index if not exists publicaciones_creada_en_idx on public.publicaciones (creada_en desc);
create index if not exists publicaciones_autor_idx     on public.publicaciones (autor_id);
create index if not exists comentarios_publicacion_idx on public.comentarios (publicacion_id, creado_en);
create index if not exists comentarios_autor_idx       on public.comentarios (autor_id);
create index if not exists mensajes_perfil_idx         on public.mensajes (perfil_id, creado_en desc);
create index if not exists mensajes_autor_idx          on public.mensajes (autor_id);
create index if not exists albumes_propietario_idx     on public.albumes (propietario_id, creado_en desc);
create index if not exists fotos_album_idx             on public.fotos (album_id, creada_en desc);
create index if not exists fotos_propietario_idx       on public.fotos (propietario_id);
create index if not exists perfiles_nombre_idx         on public.perfiles (lower(nombre));
create unique index if not exists amistades_pareja_idx
    on public.amistades (least(solicitante_id, destinatario_id), greatest(solicitante_id, destinatario_id));
create index if not exists amistades_solicitante_idx   on public.amistades (solicitante_id);
create index if not exists amistades_destinatario_idx  on public.amistades (destinatario_id);


-- =====================================================================
-- 2. Perfil automático al registrarse
-- La app manda el nombre en los metadatos del registro: { "data": { "nombre": "..." } }.
-- Si no llega, se usa lo que va antes de la @ del email.
-- =====================================================================

create or replace function public.crear_perfil_al_registrarse()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.perfiles (id, nombre)
    values (
        new.id,
        left(coalesce(nullif(btrim(new.raw_user_meta_data ->> 'nombre'), ''), split_part(new.email, '@', 1)), 30)
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists al_registrarse_crear_perfil on auth.users;
create trigger al_registrarse_crear_perfil
    after insert on auth.users
    for each row execute function public.crear_perfil_al_registrarse();

-- Cuentas que ya existían antes de ejecutar este script: se les crea el perfil
insert into public.perfiles (id, nombre)
select u.id, left(coalesce(nullif(btrim(u.raw_user_meta_data ->> 'nombre'), ''), split_part(u.email, '@', 1)), 30)
from auth.users u
on conflict (id) do nothing;


-- =====================================================================
-- 3. Permisos y políticas de seguridad (RLS)
-- La clave pública (anon key) va dentro de la app, así que cualquiera
-- podría usarla fuera de ella. Las políticas son las que deciden:
-- con sesión se lee todo, pero solo se crea, cambia o borra lo propio.
-- Sin sesión (rol anon) no se ve nada.
-- =====================================================================

grant usage on schema public to authenticated;
grant select, update on public.perfiles to authenticated;
grant select, insert, update, delete on public.publicaciones, public.comentarios, public.albumes, public.fotos to authenticated;
grant select, insert, delete on public.mensajes to authenticated;
-- Amistades: de una fila ya creada solo se puede cambiar «aceptada»
grant select, insert, delete on public.amistades to authenticated;
grant update (aceptada) on public.amistades to authenticated;

alter table public.perfiles      enable row level security;
alter table public.publicaciones enable row level security;
alter table public.comentarios   enable row level security;
alter table public.mensajes      enable row level security;
alter table public.albumes       enable row level security;
alter table public.fotos         enable row level security;
alter table public.amistades     enable row level security;

-- perfiles: se crean con el trigger; cada cuenta solo cambia el suyo
drop policy if exists "perfiles: leer con sesión" on public.perfiles;
create policy "perfiles: leer con sesión" on public.perfiles
    for select to authenticated using (true);
drop policy if exists "perfiles: cambiar el propio" on public.perfiles;
create policy "perfiles: cambiar el propio" on public.perfiles
    for update to authenticated
    using ((select auth.uid()) = id) with check ((select auth.uid()) = id);

-- publicaciones
drop policy if exists "publicaciones: leer con sesión" on public.publicaciones;
create policy "publicaciones: leer con sesión" on public.publicaciones
    for select to authenticated using (true);
drop policy if exists "publicaciones: crear como autor" on public.publicaciones;
create policy "publicaciones: crear como autor" on public.publicaciones
    for insert to authenticated with check ((select auth.uid()) = autor_id);
drop policy if exists "publicaciones: cambiar las propias" on public.publicaciones;
create policy "publicaciones: cambiar las propias" on public.publicaciones
    for update to authenticated
    using ((select auth.uid()) = autor_id) with check ((select auth.uid()) = autor_id);
drop policy if exists "publicaciones: borrar las propias" on public.publicaciones;
create policy "publicaciones: borrar las propias" on public.publicaciones
    for delete to authenticated using ((select auth.uid()) = autor_id);

-- comentarios
drop policy if exists "comentarios: leer con sesión" on public.comentarios;
create policy "comentarios: leer con sesión" on public.comentarios
    for select to authenticated using (true);
drop policy if exists "comentarios: crear como autor" on public.comentarios;
create policy "comentarios: crear como autor" on public.comentarios
    for insert to authenticated with check ((select auth.uid()) = autor_id);
drop policy if exists "comentarios: cambiar los propios" on public.comentarios;
create policy "comentarios: cambiar los propios" on public.comentarios
    for update to authenticated
    using ((select auth.uid()) = autor_id) with check ((select auth.uid()) = autor_id);
drop policy if exists "comentarios: borrar los propios" on public.comentarios;
create policy "comentarios: borrar los propios" on public.comentarios
    for delete to authenticated using ((select auth.uid()) = autor_id);

-- mensajes del tablón: cualquiera escribe en cualquier tablón;
-- borra el autor o el dueño del tablón (RF-23). No se editan.
drop policy if exists "mensajes: leer con sesión" on public.mensajes;
create policy "mensajes: leer con sesión" on public.mensajes
    for select to authenticated using (true);
drop policy if exists "mensajes: escribir como autor" on public.mensajes;
create policy "mensajes: escribir como autor" on public.mensajes
    for insert to authenticated with check ((select auth.uid()) = autor_id);
drop policy if exists "mensajes: borrar el autor o el dueño del tablón" on public.mensajes;
create policy "mensajes: borrar el autor o el dueño del tablón" on public.mensajes
    for delete to authenticated
    using ((select auth.uid()) = autor_id or (select auth.uid()) = perfil_id);

-- álbumes
drop policy if exists "albumes: leer con sesión" on public.albumes;
create policy "albumes: leer con sesión" on public.albumes
    for select to authenticated using (true);
drop policy if exists "albumes: crear como propietario" on public.albumes;
create policy "albumes: crear como propietario" on public.albumes
    for insert to authenticated with check ((select auth.uid()) = propietario_id);
drop policy if exists "albumes: cambiar los propios" on public.albumes;
create policy "albumes: cambiar los propios" on public.albumes
    for update to authenticated
    using ((select auth.uid()) = propietario_id) with check ((select auth.uid()) = propietario_id);
drop policy if exists "albumes: borrar los propios" on public.albumes;
create policy "albumes: borrar los propios" on public.albumes
    for delete to authenticated using ((select auth.uid()) = propietario_id);

-- fotos: solo en álbumes propios
drop policy if exists "fotos: leer con sesión" on public.fotos;
create policy "fotos: leer con sesión" on public.fotos
    for select to authenticated using (true);
drop policy if exists "fotos: añadir a un álbum propio" on public.fotos;
create policy "fotos: añadir a un álbum propio" on public.fotos
    for insert to authenticated
    with check (
        (select auth.uid()) = propietario_id
        and exists (select 1 from public.albumes a where a.id = album_id and a.propietario_id = (select auth.uid()))
    );
drop policy if exists "fotos: cambiar las propias" on public.fotos;
create policy "fotos: cambiar las propias" on public.fotos
    for update to authenticated
    using ((select auth.uid()) = propietario_id) with check ((select auth.uid()) = propietario_id);
drop policy if exists "fotos: borrar las propias" on public.fotos;
create policy "fotos: borrar las propias" on public.fotos
    for delete to authenticated using ((select auth.uid()) = propietario_id);


-- amistades (EX-04): las aceptadas son públicas para quien tiene sesión (se ven en el perfil);
-- las pendientes, solo para sus dos personas. Se pide en nombre propio y sin aceptar;
-- acepta solo quien la recibe; borra cualquiera de las dos (rechazar, cancelar o dejar de ser amigos).
drop policy if exists "amistades: leer las aceptadas y las propias" on public.amistades;
create policy "amistades: leer las aceptadas y las propias" on public.amistades
    for select to authenticated
    using (aceptada or (select auth.uid()) in (solicitante_id, destinatario_id));
drop policy if exists "amistades: pedir en nombre propio" on public.amistades;
create policy "amistades: pedir en nombre propio" on public.amistades
    for insert to authenticated
    with check ((select auth.uid()) = solicitante_id and not aceptada);
drop policy if exists "amistades: aceptar las recibidas" on public.amistades;
create policy "amistades: aceptar las recibidas" on public.amistades
    for update to authenticated
    using ((select auth.uid()) = destinatario_id) with check ((select auth.uid()) = destinatario_id);
drop policy if exists "amistades: borrar las propias" on public.amistades;
create policy "amistades: borrar las propias" on public.amistades
    for delete to authenticated
    using ((select auth.uid()) in (solicitante_id, destinatario_id));


-- =====================================================================
-- 4. Imágenes: bucket «imagenes» (2 MB, solo JPEG y PNG)
-- Lectura pública por URL, para que Coil cargue las imágenes sin token;
-- las rutas llevan un UUID y no se pueden adivinar.
-- Cada cuenta solo sube, cambia y borra en su carpeta: imagenes/{id de la cuenta}/...
-- =====================================================================

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('imagenes', 'imagenes', true, 2097152, array['image/jpeg', 'image/png'])
on conflict (id) do update
    set public = excluded.public,
        file_size_limit = excluded.file_size_limit,
        allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "imagenes: ver con sesión" on storage.objects;
create policy "imagenes: ver con sesión" on storage.objects
    for select to authenticated using (bucket_id = 'imagenes');
drop policy if exists "imagenes: subir a la carpeta propia" on storage.objects;
create policy "imagenes: subir a la carpeta propia" on storage.objects
    for insert to authenticated
    with check (bucket_id = 'imagenes' and (storage.foldername(name))[1] = (select auth.uid())::text);
drop policy if exists "imagenes: cambiar en la carpeta propia" on storage.objects;
create policy "imagenes: cambiar en la carpeta propia" on storage.objects
    for update to authenticated
    using (bucket_id = 'imagenes' and (storage.foldername(name))[1] = (select auth.uid())::text)
    with check (bucket_id = 'imagenes' and (storage.foldername(name))[1] = (select auth.uid())::text);
drop policy if exists "imagenes: borrar de la carpeta propia" on storage.objects;
create policy "imagenes: borrar de la carpeta propia" on storage.objects
    for delete to authenticated
    using (bucket_id = 'imagenes' and (storage.foldername(name))[1] = (select auth.uid())::text);
