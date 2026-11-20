-- =====================================================================
-- Retroconver · prueba de las políticas de seguridad (E4-02.2, #112; extras EX-01, EX-02 y EX-05)
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
    amis uuid;
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

    update public.perfiles set tema = 'noche', tipografia = 'maquina' where id = a;
    get diagnostics filas = row_count;
    if filas <> 1 then raise exception 'FALLO: A no ha podido cambiar el aspecto de su perfil'; end if;
    informe := informe || 'A puede cambiar el tema y la tipografía de su perfil'::text;

    begin
        update public.perfiles set tipografia = 'comic-sans' where id = a;
        raise exception 'FALLO: el servidor ha aceptado una tipografía que no existe';
    exception when check_violation then
        informe := informe || 'El servidor rechaza una tipografía que no existe'::text;
    end;

    -- Amistades (EX-04)
    begin
        insert into public.amistades (destinatario_id, aceptada) values (b, true);
        raise exception 'FALLO: A se ha hecho amiga de B sin que B acepte';
    exception when insufficient_privilege then
        informe := informe || 'A no puede crear una amistad ya aceptada'::text;
    end;

    begin
        insert into public.amistades (destinatario_id) values (a);
        raise exception 'FALLO: A se ha pedido amistad a sí misma';
    exception when check_violation then
        informe := informe || 'Nadie puede pedirse amistad a sí mismo'::text;
    end;

    insert into public.amistades (destinatario_id) values (b) returning id into amis;
    informe := informe || 'A puede pedir amistad a B'::text;

    update public.amistades set aceptada = true where id = amis;
    get diagnostics filas = row_count;
    if filas <> 0 then raise exception 'FALLO: A ha aceptado su propia solicitud'; end if;
    informe := informe || 'A no puede aceptar su propia solicitud'::text;

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

    begin
        insert into public.amistades (destinatario_id) values (a);
        raise exception 'FALLO: hay dos solicitudes de amistad entre A y B';
    exception when unique_violation then
        informe := informe || 'Solo puede haber una solicitud entre dos personas'::text;
    end;

    begin
        update public.amistades set solicitante_id = b where id = amis;
        raise exception 'FALLO: B ha cambiado quién pidió la amistad';
    exception when insufficient_privilege then
        informe := informe || 'B no puede cambiar quién pidió la amistad'::text;
    end;

    -- Mensajes directos (EX-01): mientras la amistad no está aceptada, no se puede escribir
    begin
        insert into public.mensajes_directos (destinatario_id, texto) values (a, 'Hola A');
        raise exception 'FALLO: B ha enviado un mensaje directo a A sin ser amigos';
    exception when insufficient_privilege then
        informe := informe || 'B no puede enviar mensajes directos a A sin ser amigos'::text;
    end;

    update public.amistades set aceptada = true where id = amis;
    get diagnostics filas = row_count;
    if filas <> 1 then raise exception 'FALLO: B no ha podido aceptar la solicitud de A'; end if;
    informe := informe || 'B puede aceptar la solicitud de A'::text;

    insert into public.comentarios (publicacion_id, texto) values (pub, 'Comentario de B');
    informe := informe || 'B puede comentar la publicación de A'::text;

    insert into public.mensajes (perfil_id, texto) values (a, 'Hola A, soy B') returning id into msj;
    informe := informe || 'B puede escribir en el tablón de A'::text;

    -- «Me gusta» (EX-05): en nombre propio sí, en nombre de otra cuenta no
    insert into public.me_gusta (publicacion_id) values (pub);
    informe := informe || 'B puede dar «Me gusta» a la publicación de A'::text;
    begin
        insert into public.me_gusta (publicacion_id, usuario_id) values (pub, a);
        raise exception 'FALLO: B ha dado «Me gusta» en nombre de A';
    exception when insufficient_privilege then
        informe := informe || 'B no puede dar «Me gusta» en nombre de A'::text;
    end;

    -- Mensajes directos: ya son amigos
    insert into public.mensajes_directos (destinatario_id, texto, zumbido) values (a, '¡Zumbido!', true);
    informe := informe || 'B puede enviar un mensaje directo (un zumbido) a su amigo A'::text;

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

    if (select count(*) from public.mensajes_directos where destinatario_id = a) <> 1 then
        raise exception 'FALLO: A no ve el mensaje directo que le ha enviado B';
    end if;
    informe := informe || 'A ve el mensaje directo que le envía B'::text;

    delete from public.me_gusta where publicacion_id = pub and usuario_id = b;
    get diagnostics filas = row_count;
    if filas <> 0 then raise exception 'FALLO: A ha quitado el «Me gusta» de B'; end if;
    informe := informe || 'A no puede quitar el «Me gusta» de B'::text;

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
    begin
        select count(*) into filas from public.amistades;
        if filas <> 0 then raise exception 'FALLO: sin sesión se ven amistades'; end if;
    exception when insufficient_privilege then
        null;
    end;
    informe := informe || 'Sin sesión no se ve ninguna amistad'::text;
    begin
        select count(*) into filas from public.mensajes_directos;
        if filas <> 0 then raise exception 'FALLO: sin sesión se ven mensajes directos'; end if;
    exception when insufficient_privilege then
        null;
    end;
    informe := informe || 'Sin sesión no se ve ningún mensaje directo'::text;

    -- ---------- Limpieza ----------
    execute 'reset role';
    -- Borrar las cuentas borra en cascada sus perfiles y todo lo que han creado
    delete from auth.users where id in (a, b);

    return query select t.n::int, t.r from unnest(informe) with ordinality as t(r, n);
end;
$$;

select * from pg_temp.probar_seguridad();
