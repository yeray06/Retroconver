-- =====================================================================
-- Retroconver · prueba de las políticas de seguridad (E4-02.2, #112)
--
-- Crea dos cuentas temporales (A y B, sin contraseña, no sirven para entrar),
-- comprueba que cada una solo puede tocar lo suyo y las borra al terminar.
--
-- Cómo usarlo: SQL Editor → New query → pegar todo → Run.
-- Devuelve una tabla con cada comprobación. Si alguna falla, se para con
-- un error que empieza por FALLO y se deshace todo. No deja nada guardado.
-- =====================================================================

create or replace function pg_temp.probar_seguridad()
returns table (paso int, comprobacion text)
language plpgsql
as $$
declare
    a uuid;
    b uuid;
    pub uuid;
    msj uuid;
    alb uuid;
    filas integer;
    informe text[] := '{}';
begin
    -- Dos cuentas temporales. El trigger del esquema les crea el perfil.
    a := gen_random_uuid();
    b := gen_random_uuid();
    insert into auth.users (id, instance_id, aud, role, email, raw_user_meta_data, raw_app_meta_data, created_at, updated_at)
    values
        (a, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
         'prueba-a-' || a || '@retroconver.invalid', '{"nombre": "Prueba A"}', '{}', now(), now()),
        (b, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
         'prueba-b-' || b || '@retroconver.invalid', '{"nombre": "Prueba B"}', '{}', now(), now());
    if (select count(*) from public.perfiles where id in (a, b)) <> 2 then
        raise exception 'FALLO: el trigger no ha creado los perfiles al registrar las cuentas';
    end if;
    informe := informe || 'Al registrarse una cuenta se crea su perfil'::text;

    -- ---------- Como la cuenta A ----------
    perform set_config('request.jwt.claims', json_build_object('sub', a, 'role', 'authenticated')::text, true);
    execute 'set local role authenticated';

    insert into public.publicaciones (texto) values ('Prueba de seguridad de A') returning id into pub;
    informe := informe || 'A puede publicar (el autor se pone solo)'::text;

    begin
        insert into public.publicaciones (autor_id, texto) values (b, 'Publicación suplantando a B');
        raise exception 'FALLO: A ha publicado en nombre de B';
    exception when insufficient_privilege then
        informe := informe || 'A no puede publicar en nombre de B'::text;
    end;

    begin
        insert into public.publicaciones (texto) values ('   ');
        raise exception 'FALLO: el servidor ha aceptado una publicación vacía';
    exception when check_violation then
        informe := informe || 'El servidor rechaza una publicación vacía'::text;
    end;

    insert into public.albumes (titulo) values ('Álbum de prueba de A') returning id into alb;
    informe := informe || 'A puede crear un álbum'::text;

    -- ---------- Como la cuenta B ----------
    perform set_config('request.jwt.claims', json_build_object('sub', b, 'role', 'authenticated')::text, true);

    delete from public.publicaciones where id = pub;
    get diagnostics filas = row_count;
    if filas <> 0 then raise exception 'FALLO: B ha borrado la publicación de A'; end if;
    informe := informe || 'B no puede borrar la publicación de A'::text;

    update public.perfiles set nombre = 'Cambiado por B' where id = a;
    get diagnostics filas = row_count;
    if filas <> 0 then raise exception 'FALLO: B ha cambiado el perfil de A'; end if;
    informe := informe || 'B no puede cambiar el perfil de A'::text;

    insert into public.comentarios (publicacion_id, texto) values (pub, 'Comentario de B');
    informe := informe || 'B puede comentar la publicación de A'::text;

    insert into public.mensajes (perfil_id, texto) values (a, 'Hola A, soy B') returning id into msj;
    informe := informe || 'B puede escribir en el tablón de A'::text;

    begin
        insert into public.fotos (album_id, ruta) values (alb, b::text || '/prueba.jpg');
        raise exception 'FALLO: B ha añadido una foto al álbum de A';
    exception when insufficient_privilege then
        informe := informe || 'B no puede añadir fotos al álbum de A'::text;
    end;

    -- ---------- Otra vez como A: dueño del tablón ----------
    perform set_config('request.jwt.claims', json_build_object('sub', a, 'role', 'authenticated')::text, true);

    delete from public.mensajes where id = msj;
    get diagnostics filas = row_count;
    if filas <> 1 then raise exception 'FALLO: A no ha podido borrar un mensaje de su tablón'; end if;
    informe := informe || 'A puede borrar un mensaje que B dejó en su tablón'::text;

    -- ---------- Sin sesión ----------
    execute 'reset role';
    perform set_config('request.jwt.claims', '', true);
    execute 'set local role anon';
    begin
        select count(*) into filas from public.publicaciones;
        if filas <> 0 then raise exception 'FALLO: sin sesión se ven publicaciones'; end if;
    exception when insufficient_privilege then
        null; -- sin permisos: también vale
    end;
    informe := informe || 'Sin sesión no se ve ninguna publicación'::text;

    -- ---------- Limpieza ----------
    execute 'reset role';
    -- Borrar las cuentas borra en cascada sus perfiles y todo lo que han creado
    delete from auth.users where id in (a, b);

    return query select t.n::int, t.r from unnest(informe) with ordinality as t(r, n);
end;
$$;

select * from pg_temp.probar_seguridad();
