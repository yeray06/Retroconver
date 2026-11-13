package com.retroconver.data.subidas

import com.retroconver.R
import com.retroconver.data.remote.ResultadoRed
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Subida de fotos en segundo plano (#147): la lógica de una tanda y lo que ve la pantalla
 * a partir de los trabajos de WorkManager. Sin Android: las «fotos» son textos.
 */
class SubidasFotosTest {

    /** Lo que ha pasado durante una tanda de prueba. */
    private val subidas = mutableListOf<String>()
    private val limpiadas = mutableListOf<String>()
    private val avances = mutableListOf<Pair<Int, Int>>()

    private fun tanda(
        origenes: List<String>,
        sinLeer: Set<String> = emptySet(),
        fallos: Map<String, ResultadoRed.Fallo> = emptyMap(),
    ): ResumenTanda = runBlocking {
        subirTanda(
            origenes = origenes,
            preparar = { origen -> if (origen in sinLeer) null else "$origen-720p" },
            subir = { imagen ->
                subidas += imagen
                fallos[imagen.removeSuffix("-720p")] ?: ResultadoRed.Correcto(Unit)
            },
            limpiar = { imagen -> limpiadas += imagen },
            alAvanzar = { actual, total -> avances += actual to total },
        )
    }

    // ---------- Una tanda ----------

    @Test
    fun unaTandaSinProblemasSubeTodasYLimpiaLasCopias() {
        val resumen = tanda(listOf("a", "b", "c"))

        assertEquals(ResumenTanda(total = 3, fallidas = 0, motivo = null), resumen)
        assertEquals(listOf("a-720p", "b-720p", "c-720p"), subidas)
        assertEquals("Ninguna copia se queda en el móvil", subidas, limpiadas)
        assertEquals(listOf(1 to 3, 2 to 3, 3 to 3), avances)
    }

    @Test
    fun siUnaNoSePuedeLeerSeSigueConLasDemas() {
        val resumen = tanda(listOf("a", "rota", "c"), sinLeer = setOf("rota"))

        assertEquals(ResumenTanda(3, 1, R.string.galeria_error_foto_no_valida), resumen)
        assertEquals(listOf("a-720p", "c-720p"), subidas)
    }

    @Test
    fun siElServidorRechazaUnaSeSigueYSeGuardaElMotivo() {
        val resumen = tanda(listOf("a", "grande", "c"), fallos = mapOf("grande" to ResultadoRed.ErrorServidor(413)))

        assertEquals(ResumenTanda(3, 1, R.string.red_imagen_demasiado_grande), resumen)
        assertEquals(3, subidas.size)
        assertEquals("También se limpia la copia de la que ha fallado", 3, limpiadas.size)
    }

    @Test
    fun siLaSesionCaducaSeParaYLasQueFaltanCuentanComoFallidas() {
        val resumen = tanda(listOf("a", "b", "c", "d"), fallos = mapOf("b" to ResultadoRed.SesionCaducada))

        assertEquals(ResumenTanda(4, 3, R.string.red_sesion_caducada), resumen)
        assertEquals(listOf("a-720p", "b-720p"), subidas)
    }

    @Test
    fun unaTandaVaciaNoHaceNada() {
        assertEquals(ResumenTanda(0, 0, null), tanda(emptyList()))
        assertTrue(subidas.isEmpty())
    }

    // ---------- Lo que ve la pantalla ----------

    private val ahora = 1_000_000_000L

    @Test
    fun sinTrabajosNoHayNadaQueEnsenar() {
        assertEquals(EstadoSubidas(), resumirSubidas(emptyList(), ahora))
    }

    @Test
    fun unaTandaEnMarchaEnsenaPorQueFotoVa() {
        val estado = resumirSubidas(listOf(InfoTanda("1", enMarcha = true, enCola = false, terminada = false, actual = 2, total = 5)), ahora)

        assertTrue(estado.subiendo)
        assertEquals(2, estado.actual)
        assertEquals(5, estado.total)
        assertFalse(estado.enCola)
    }

    @Test
    fun unaTandaEsperandoRedEstaEnCola() {
        val estado = resumirSubidas(listOf(InfoTanda("1", enMarcha = false, enCola = true, terminada = false)), ahora)

        assertFalse(estado.subiendo)
        assertTrue(estado.enCola)
    }

    @Test
    fun seEnsenaElFalloMasRecienteDeLosUltimosMinutos() {
        val estado = resumirSubidas(
            listOf(
                InfoTanda("vieja", false, false, terminada = true, total = 3, fallidas = 3, motivo = 7, terminadaEn = ahora - 60_000),
                InfoTanda("nueva", false, false, terminada = true, total = 5, fallidas = 1, motivo = 9, terminadaEn = ahora - 1_000),
                InfoTanda("bien", false, false, terminada = true, total = 2, fallidas = 0, terminadaEn = ahora),
            ),
            ahora,
        )

        assertEquals(ResumenTanda(total = 5, fallidas = 1, motivo = 9), estado.ultimoFallo)
        assertEquals("nueva", estado.idUltimoFallo)
    }

    @Test
    fun unFalloDeHaceMasDeDiezMinutosYaNoSeEnsena() {
        val estado = resumirSubidas(
            listOf(InfoTanda("1", false, false, terminada = true, total = 2, fallidas = 1, terminadaEn = ahora - VIGENCIA_FALLO_MS - 1)),
            ahora,
        )

        assertNull(estado.ultimoFallo)
    }

    @Test
    fun sinMotivoGuardadoElFalloNoTieneMotivo() {
        val estado = resumirSubidas(
            listOf(InfoTanda("1", false, false, terminada = true, total = 2, fallidas = 1, motivo = 0, terminadaEn = ahora)),
            ahora,
        )

        assertNull(estado.ultimoFallo?.motivo)
    }
}
