package com.retroconver.data.repository

import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.data.local.ComentarioDao
import com.retroconver.data.local.ComentarioEntity
import com.retroconver.data.local.PublicacionConAutor
import com.retroconver.data.local.PublicacionDao
import com.retroconver.data.local.PublicacionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

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
    fun publicacion(id: Long): Flow<PublicacionConAutor?> = publicacionDao.observarPorId(id)

    /** Comentarios de una publicación, el más reciente primero. */
    fun comentarios(publicacionId: Long): Flow<List<ComentarioConAutor>> =
        comentarioDao.observarDePublicacion(publicacionId)

    /** Crea una publicación y devuelve su id. `ahora` solo se pasa en las pruebas. */
    suspend fun publicar(
        usuarioId: Long,
        texto: String,
        imagenUri: String? = null,
        ahora: Instant = Instant.now(),
    ): Long = publicacionDao.insertar(
        PublicacionEntity(usuarioId = usuarioId, texto = texto, imagenUri = imagenUri, timestamp = ahora),
    )

    /** Añade un comentario a una publicación y devuelve su id. */
    suspend fun comentar(
        publicacionId: Long,
        usuarioId: Long,
        texto: String,
        ahora: Instant = Instant.now(),
    ): Long = comentarioDao.insertar(
        ComentarioEntity(publicacionId = publicacionId, usuarioId = usuarioId, texto = texto, timestamp = ahora),
    )

    /** Borra la publicación y sus comentarios. */
    suspend fun eliminar(publicacionId: Long) = publicacionDao.eliminar(publicacionId)
}
