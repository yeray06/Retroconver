package com.retroconver.data.repository

import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PerfilDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/** Paso de una fila de `perfiles` del servidor a la copia de Room (E4-05.1, #121). */
class ConversionesTest {

    private val configuracion = ConfiguracionSupabase("https://abc.supabase.co", "clave")

    @Test
    fun unPerfilConAvatarLlevaSuRutaYSuUrlPublica() {
        val dto = PerfilDto(
            id = "cuenta",
            nombre = "Yeray",
            biografia = "Hola",
            estado = "Estudiando",
            avatarRuta = "cuenta/avatar.jpg",
            colorFondo = "#C5D7E8",
            tema = "clasico",
            creadoEn = "2026-11-11T09:30:00.123456+00:00",
        )

        val entidad = dto.aEntidad(configuracion)

        assertEquals("cuenta", entidad.id)
        assertEquals("Yeray", entidad.nombre)
        assertEquals("Hola", entidad.biografia)
        assertEquals("Estudiando", entidad.estado)
        assertEquals("cuenta/avatar.jpg", entidad.avatarRuta)
        assertEquals("https://abc.supabase.co/storage/v1/object/public/imagenes/cuenta/avatar.jpg", entidad.avatarUri)
        assertEquals("#C5D7E8", entidad.colorFondo)
        assertEquals("clasico", entidad.tema)
        assertEquals(Instant.parse("2026-11-11T09:30:00.123456Z"), entidad.fechaAlta)
    }

    @Test
    fun sinAvatarNoHayUrl() {
        val dto = PerfilDto(id = "cuenta", nombre = "Yeray", creadoEn = "2026-11-11T09:30:00+00:00")

        val entidad = dto.aEntidad(configuracion)

        assertNull(entidad.avatarRuta)
        assertNull(entidad.avatarUri)
        assertEquals("", entidad.biografia)
    }
}
