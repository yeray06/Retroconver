package com.retroconver.data.repository

import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.data.local.ComentarioDao
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.local.PublicacionDao
import com.retroconver.data.local.PublicacionEntity
import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.ComentarioDto
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.NuevaPublicacionDto
import com.retroconver.data.remote.NuevoComentarioDto
import com.retroconver.data.remote.PerfilDto
import com.retroconver.data.remote.PublicacionDto
import com.retroconver.data.remote.PublicacionService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.exigirExito
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.UUID

/**
 * Única puerta de acceso a las publicaciones y sus comentarios (E4-06 y E4-07, #123 y #18).
 * Sigue el patrón de docs/arquitectura.md, apartado 2: las pantallas leen de Room (`Flow`) y el
 * repositorio la mantiene al día con el servidor; al escribir, primero el servidor y después su
 * respuesta en Room. Sin conexión no se guarda nada y la pantalla avisa.
 *
 * Cualquiera con sesión ve las publicaciones de todas las personas y puede comentarlas, pero solo
 * borra las suyas (lo comprueban las políticas de seguridad del servidor).
 * Validar el texto (no vacío, longitud máxima…) es cosa del ViewModel.
 */
class PublicacionRepository(
    private val publicacionDao: PublicacionDao,
    private val comentarioDao: ComentarioDao,
    private val usuarioDao: UsuarioDao,
    private val servicio: PublicacionService,
    private val almacenamiento: AlmacenamientoService,
    private val sesion: SesionRepository,
    private val configuracion: ConfiguracionSupabase,
) {

    /**
     * Las publicaciones de [autores] (la cuenta de la sesión y sus amigos, EX-04), la más reciente
     * primero. Es lo que enseña el muro de amigos.
     */
    fun publicacionesDe(autores: Set<String>): Flow<List<PublicacionConAutor>> =
        publicaciones().map { lista -> lista.filter { it.publicacion.usuarioId in autores } }

    /** Todas las publicaciones con su autor y su número de comentarios, la más reciente primero. */
    fun publicaciones(): Flow<List<PublicacionConAutor>> = publicacionDao.observarTodas()

    /** Una publicación concreta; emite `null` si no existe. */
    fun publicacion(id: String): Flow<PublicacionConAutor?> = publicacionDao.observarPorId(id)

    /** Comentarios de una publicación, el más reciente primero. */
    fun comentarios(publicacionId: String): Flow<List<ComentarioConAutor>> =
        comentarioDao.observarDePublicacion(publicacionId)

    // ---------- Descargar ----------

    /**
     * Descarga las [LIMITE_MURO] publicaciones más recientes, con sus autores y sus comentarios, y
     * deja el muro de Room igual que el del servidor, también si se ha borrado algo desde otro móvil
     * (E4-06.2, #125). Sin conexión no cambia nada y el muro sigue mostrando la última copia.
     *
     * Con [autores] solo se descargan las de esas personas (el muro de amigos, EX-04, #129); sin
     * ellos, las de todas.
     */
    suspend fun refrescar(autores: Set<String>? = null): ResultadoRed<Unit> {
        // Con [autores] (muro de amigos, EX-04) solo se piden las suyas: así no se pierden entre las de los demás
        val filtro = autores?.takeIf { it.isNotEmpty() }?.let { ids -> "in.(${ids.sorted().joinToString(",")})" }
        val resultado = llamarAlServidor { servicio.ultimas(LIMITE_MURO, filtro) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val filas = (resultado as ResultadoRed.Correcto).datos
        guardarPerfiles(filas)
        val publicaciones = filas.filter { it.autor != null }
        publicacionDao.sincronizar(
            publicaciones = publicaciones.map { it.aEntidad(configuracion) },
            hayMasAntiguas = filas.size >= LIMITE_MURO,
        )
        publicaciones.forEach { guardarComentarios(it.id, it.comentarios) }
        return ResultadoRed.Correcto(Unit)
    }

    /**
     * Descarga una publicación con todos sus comentarios (detalle, E4-07.1, #66). Si se ha borrado
     * en el servidor, se quita también de Room y devuelve un error 404.
     */
    suspend fun refrescarPublicacion(id: String): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.publicacion(igual(id)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val fila = (resultado as ResultadoRed.Correcto).datos.firstOrNull()
        if (fila == null || fila.autor == null) {
            publicacionDao.eliminar(id)
            return ResultadoRed.ErrorServidor(404, "not_found")
        }
        guardarPerfiles(listOf(fila))
        publicacionDao.guardarUna(fila.aEntidad(configuracion))
        guardarComentarios(fila.id, fila.comentarios)
        return ResultadoRed.Correcto(Unit)
    }

    /** Descarga los comentarios de una publicación y quita los que se han borrado en el servidor. */
    suspend fun refrescarComentarios(publicacionId: String): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.comentariosDe(igual(publicacionId)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val comentarios = (resultado as ResultadoRed.Correcto).datos
        usuarioDao.guardarTodos(comentarios.mapNotNull { it.autor }.distinctBy { it.id }.map { it.aEntidad(configuracion) })
        guardarComentarios(publicacionId, comentarios)
        return ResultadoRed.Correcto(Unit)
    }

    // ---------- Escribir ----------

    /**
     * Publica en el muro (E4-06.1, #124) y devuelve el id de la publicación:
     * 1. Si lleva [imagen], ya reescalada a 720p, la sube a `imagenes/{id de la cuenta}/{uuid}.jpg`.
     * 2. Crea la fila en `publicaciones` y guarda en Room la que devuelve el servidor.
     *
     * Si falla el paso 2, se borra la imagen recién subida para no dejarla suelta.
     */
    suspend fun publicar(texto: String, imagen: ImagenParaSubir? = null): ResultadoRed<String> {
        val cuenta = idActual() ?: return ResultadoRed.SesionCaducada
        val ruta = imagen?.let { rutaNueva(cuenta, it) }
        if (imagen != null && ruta != null) {
            val subida = llamarAlServidor {
                almacenamiento.subir(ConfiguracionSupabase.BUCKET_IMAGENES, ruta, cuerpoDe(imagen)).exigirExito()
            }
            if (subida is ResultadoRed.Fallo) return subida
        }

        val fila = llamarAlServidor { servicio.crear(NuevaPublicacionDto(texto = texto, imagenRuta = ruta)) }
        val publicacion = (fila as? ResultadoRed.Correcto)?.datos?.firstOrNull()
        if (publicacion == null || publicacion.autor == null) {
            if (ruta != null) borrarImagen(ruta)
            return fila as? ResultadoRed.Fallo ?: ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        }
        guardarPerfiles(listOf(publicacion))
        publicacionDao.guardarUna(publicacion.aEntidad(configuracion))
        return ResultadoRed.Correcto(publicacion.id)
    }

    /**
     * Añade un comentario a una publicación (E4-07.2, #67). [texto] ya viene validado.
     * Se guarda en Room el comentario que devuelve el servidor y el contador del muro sube solo.
     */
    suspend fun comentar(publicacionId: String, texto: String): ResultadoRed<String> {
        val resultado = llamarAlServidor { servicio.comentar(NuevoComentarioDto(publicacionId, texto)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val comentario = (resultado as ResultadoRed.Correcto).datos.firstOrNull()
        if (comentario == null || comentario.autor == null) return ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        usuarioDao.guardar(comentario.autor.aEntidad(configuracion))
        comentarioDao.guardar(listOf(comentario.aEntidad()))
        return ResultadoRed.Correcto(comentario.id)
    }

    /**
     * Borra una publicación propia: primero su fila del servidor (los comentarios se borran en
     * cascada), después su imagen y por último la copia de Room. Si no se puede borrar la imagen,
     * la publicación ya no aparece en ningún sitio y solo queda un archivo sin usar.
     */
    suspend fun eliminar(publicacion: PublicacionEntity): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.borrar(igual(publicacion.id)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        if ((resultado as ResultadoRed.Correcto).datos.isEmpty()) {
            return ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        }
        publicacion.imagenRuta?.let { borrarImagen(it) }
        publicacionDao.eliminar(publicacion.id)
        return ResultadoRed.Correcto(Unit)
    }

    /**
     * Borra un comentario propio (#67): primero en el servidor y, si lo ha borrado, de Room, y el
     * contador del muro baja solo. Si no era suyo (la política de seguridad no lo deja), el servidor
     * no devuelve ninguna fila y no se toca nada.
     */
    suspend fun eliminarComentario(comentarioId: String): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.borrarComentario(igual(comentarioId)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        if ((resultado as ResultadoRed.Correcto).datos.isEmpty()) {
            return ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        }
        comentarioDao.eliminar(comentarioId)
        return ResultadoRed.Correcto(Unit)
    }

    /** Id de la cuenta con sesión: la autora de lo que se publica. */
    suspend fun idActual(): String? = sesion.sesionActual()?.idCuenta

    // ---------- Internos ----------

    /**
     * Las publicaciones y los comentarios tienen clave foránea a su autor en Room: los perfiles
     * se guardan antes. Así también se actualizan el nombre y el avatar de cada persona.
     */
    private suspend fun guardarPerfiles(publicaciones: List<PublicacionDto>) {
        val perfiles: List<PerfilDto> = publicaciones.flatMap { listOfNotNull(it.autor) + it.comentarios.mapNotNull(ComentarioDto::autor) }
            .distinctBy { it.id }
        if (perfiles.isNotEmpty()) usuarioDao.guardarTodos(perfiles.map { it.aEntidad(configuracion) })
    }

    /** Deja en Room los comentarios de la publicación igual que en el servidor. */
    private suspend fun guardarComentarios(publicacionId: String, comentarios: List<ComentarioDto>) {
        val conAutor = comentarios.filter { it.autor != null }
        comentarioDao.borrarQueYaNoEstan(publicacionId, conAutor.map { it.id })
        if (conAutor.isNotEmpty()) comentarioDao.guardar(conAutor.map { it.aEntidad() })
    }

    private fun rutaNueva(cuenta: String, imagen: ImagenParaSubir): String {
        val esPng = imagen.fichero.extension.equals("png", ignoreCase = true)
        return "$cuenta/${UUID.randomUUID()}.${if (esPng) "png" else "jpg"}"
    }

    private fun cuerpoDe(imagen: ImagenParaSubir) = imagen.fichero.asRequestBody(
        (if (imagen.fichero.extension.equals("png", ignoreCase = true)) "image/png" else "image/jpeg").toMediaType(),
    )

    /** Si no se puede borrar (por ejemplo, sin red), la imagen se queda en el servidor sin usar. No es grave. */
    private suspend fun borrarImagen(ruta: String) {
        llamarAlServidor { almacenamiento.borrar(ConfiguracionSupabase.BUCKET_IMAGENES, ruta).exigirExito() }
    }

    companion object {
        /** Cuántas publicaciones se descargan al abrir o actualizar el muro. */
        const val LIMITE_MURO = 50

        /** Código de Postgres para «sin permiso» (lo traduce MensajesRed). */
        private const val CODIGO_SIN_PERMISO = "42501"
    }
}
