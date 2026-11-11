package com.retroconver.data.repository

import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.data.local.ComentarioDao
import com.retroconver.data.local.ComentarioEntity
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.local.PublicacionDao
import com.retroconver.data.local.PublicacionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

/**
 * Única puerta de acceso a las publicaciones y sus comentarios.
 *
 * Las lecturas devuelven `Flow`: cuando se guarda o se borra algo, las pantallas que
 * lo observan se actualizan solas. Las escrituras son funciones `suspend`.
 * Validar el texto (no vacío, longitud máxima…) es cosa del ViewModel.
 */
class PublicacionRepository(
    private val publicacionDao: PublicacionDao,
    private val comentarioDao: ComentarioDao,
) {

    /** Todas las publicaciones con su autor y su número de comentarios, la más reciente primero. */
    fun publicaciones(): Flow<List<PublicacionConAutor>> = publicacionDao.observarTodas()

    /** Una publicación concreta; emite `null` si no existe. */
    fun publicacion(id: String): Flow<PublicacionConAutor?> = publicacionDao.observarPorId(id)

    /** Comentarios de una publicación, el más reciente primero. */
    fun comentarios(publicacionId: String): Flow<List<ComentarioConAutor>> =
        comentarioDao.observarDePublicacion(publicacionId)

    /**
     * Crea una publicación y devuelve su id, un UUID como los del servidor.
     * `ahora` solo se pasa en las pruebas. En E4-06 (#123) se guarda primero en el servidor.
     */
    suspend fun publicar(
        usuarioId: String,
        texto: String,
        imagenUri: String? = null,
        ahora: Instant = Instant.now(),
    ): String {
        val id = UUID.randomUUID().toString()
        publicacionDao.insertar(
            PublicacionEntity(id = id, usuarioId = usuarioId, texto = texto, imagenUri = imagenUri, timestamp = ahora),
        )
        return id
    }

    /** Añade un comentario a una publicación y devuelve su id. */
    suspend fun comentar(
        publicacionId: String,
        usuarioId: String,
        texto: String,
        ahora: Instant = Instant.now(),
    ): String {
        val id = UUID.randomUUID().toString()
        comentarioDao.insertar(
            ComentarioEntity(id = id, publicacionId = publicacionId, usuarioId = usuarioId, texto = texto, timestamp = ahora),
        )
        return id
    }

    /** Borra la publicación y sus comentarios. */
    suspend fun eliminar(publicacionId: String) = publicacionDao.eliminar(publicacionId)
}
