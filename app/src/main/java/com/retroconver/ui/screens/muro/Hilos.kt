package com.retroconver.ui.screens.muro

import com.retroconver.data.local.ComentarioConAutor

/** Un comentario con sus respuestas debajo (EX-02, #29). */
data class HiloComentario(
    val comentario: ComentarioConAutor,
    /** Respuestas, de la más antigua a la más reciente, como una conversación. */
    val respuestas: List<ComentarioConAutor>,
)

/**
 * Agrupa los comentarios de una publicación en hilos (EX-02, #29). Los comentarios salen en el
 * orden en que llegan (el más reciente primero) y cada respuesta, debajo del comentario al que
 * responde. Si se responde a una respuesta, va al mismo hilo. Si el comentario respondido ya no
 * está (se ha borrado), la respuesta se enseña como un comentario más.
 */
fun hilosDeComentarios(comentarios: List<ComentarioConAutor>): List<HiloComentario> {
    val porId = comentarios.associateBy { it.comentario.id }

    // Sube por las respuestas hasta el comentario que empieza el hilo (con tope, por si hubiera un ciclo)
    fun raiz(item: ComentarioConAutor): ComentarioConAutor {
        var actual = item
        repeat(comentarios.size) {
            val padre = actual.comentario.respuestaA?.let { porId[it] } ?: return actual
            actual = padre
        }
        return actual
    }

    val respuestas = comentarios
        .filter { raiz(it) !== it }
        .groupBy { raiz(it).comentario.id }
    return comentarios
        .filter { raiz(it) === it }
        .map { principal ->
            HiloComentario(
                comentario = principal,
                respuestas = respuestas[principal.comentario.id].orEmpty()
                    .sortedWith(compareBy({ it.comentario.timestamp }, { it.comentario.id })),
            )
        }
}
