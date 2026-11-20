package com.retroconver.ui.screens.muro

import com.retroconver.data.local.ComentarioConAutor
import com.retroconver.data.local.ComentarioEntity
import com.retroconver.data.local.UsuarioEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Respuestas a comentarios (EX-02, #29): cómo se agrupan en hilos. */
class HilosComentariosTest {

    private val ana = UsuarioEntity(id = "ana", nombre = "Ana", fechaAlta = Instant.EPOCH)

    private fun comentario(id: String, minuto: Long, respuestaA: String? = null) = ComentarioConAutor(
        ComentarioEntity(id, "p1", "ana", "Texto $id", Instant.ofEpochSecond(minuto * 60), respuestaA),
        ana,
    )

    @Test
    fun sinRespuestasCadaComentarioEsUnHiloEnElMismoOrden() {
        val hilos = hilosDeComentarios(listOf(comentario("c2", 2), comentario("c1", 1)))

        assertEquals(listOf("c2", "c1"), hilos.map { it.comentario.comentario.id })
        assertTrue(hilos.all { it.respuestas.isEmpty() })
    }

    @Test
    fun lasRespuestasVanDebajoDeSuComentarioDeLaMasAntiguaALaMasReciente() {
        val hilos = hilosDeComentarios(
            listOf(
                comentario("r2", 5, respuestaA = "c1"),
                comentario("c2", 4),
                comentario("r1", 3, respuestaA = "c1"),
                comentario("c1", 1),
            ),
        )

        assertEquals(listOf("c2", "c1"), hilos.map { it.comentario.comentario.id })
        assertEquals(listOf("r1", "r2"), hilos[1].respuestas.map { it.comentario.id })
    }

    @Test
    fun unaRespuestaAUnaRespuestaVaAlMismoHilo() {
        val hilos = hilosDeComentarios(
            listOf(comentario("r2", 3, respuestaA = "r1"), comentario("r1", 2, respuestaA = "c1"), comentario("c1", 1)),
        )

        assertEquals(1, hilos.size)
        assertEquals(listOf("r1", "r2"), hilos.single().respuestas.map { it.comentario.id })
    }

    @Test
    fun siElComentarioRespondidoYaNoEstaLaRespuestaSaleComoUnComentarioMas() {
        val hilos = hilosDeComentarios(listOf(comentario("r1", 2, respuestaA = "borrado")))

        assertEquals(listOf("r1"), hilos.map { it.comentario.comentario.id })
    }
}
