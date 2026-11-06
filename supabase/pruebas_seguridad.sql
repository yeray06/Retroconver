-- =====================================================================
-- Retroconver · prueba de las políticas de seguridad (E4-02.2, #112)
--
-- Comprueba con las dos primeras cuentas del proyecto que cada una solo
-- puede tocar lo suyo. Antes hay que crear dos cuentas de prueba en
-- Authentication → Users → Add user → Create new user (con «Auto Confirm User»).
--
-- Cómo usarlo: SQL Editor → New query → pegar todo → Run.
-- Devuelve una tabla con cada comprobación. Si alguna falla, se para con
-- un error que empieza por FALLO. No deja nada guardado: borra lo que crea.
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
    select id into a from auth.users order by created_at limit 1;
    select id into b from auth.users order by created_at offset 1 limit 1;
    if b is null then
        raise exception 'Hacen falta dos cuentas de prueba: Authentication → Users → Add user';
    end if;

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
    delete from public.publicaciones where id = pub; -- borra también el comentario de B
    delete from public.albumes where id = alb;

    return query select t.n::int, t.r from unnest(informe) with ordinality as t(r, n);
end;
$$;

select * from pg_temp.probar_seguridad();
