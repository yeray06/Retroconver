package com.retroconver.data.repository

import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.local.UsuarioEntity
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.CambioAvatarDto
import com.retroconver.data.remote.CambiosPerfilDto
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.PerfilDto
import com.retroconver.data.remote.PerfilService
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.exigirExito
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.remote.map
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

/**
 * Única puerta de acceso a los perfiles (E4-05.1, #121).
 *
 * El servidor es la fuente de verdad y Room guarda la última copia: las pantallas leen de Room
 * ([usuarioActual], [usuario]) y se actualizan solas cuando [refrescar] trae datos nuevos.
 *
 * El usuario actual es el de la sesión abierta: su id es el UUID de la cuenta. Ya no hay
 * un usuario local con id fijo (E3-01.1).
 *
 * Al escribir (editar el perfil, cambiar el avatar) se guarda **primero en el servidor** y, si
 * responde bien, su respuesta en Room (E4-05.2, #122). Es el patrón que siguen todos los
 * módulos: docs/arquitectura.md, apartado 2.
 */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val perfilService: PerfilService,
    private val almacenamiento: AlmacenamientoService,
    private val sesion: SesionRepository,
    private val configuracion: ConfiguracionSupabase,
) {

    /** Id de la cuenta con la sesión abierta, o `null` si no hay sesión. Es el autor de lo que se publica. */
    suspend fun idActual(): String? = sesion.sesionActual()?.idCuenta

    /**
     * Perfil de la sesión abierta. Emite cada vez que cambia, y `null` mientras no hay copia en Room
     * (por ejemplo, justo después de entrar y antes de descargarlo).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun usuarioActual(): Flow<UsuarioEntity?> = sesion.idCuenta.flatMapLatest { id ->
        if (id == null) flowOf(null) else usuarioDao.observar(id)
    }

    /** Perfil de cualquier persona de la red (para ver perfiles ajenos, E4-08). */
    fun usuario(id: String): Flow<UsuarioEntity?> = usuarioDao.observar(id)

    /**
     * Descarga el perfil [id] del servidor y actualiza la copia de Room. Sin conexión no cambia
     * nada y la pantalla sigue con la copia que hay.
     */
    suspend fun refrescar(id: String): ResultadoRed<Unit> =
        llamarAlServidor { perfilService.obtener(igual(id)) }
            .map { filas ->
                filas.firstOrNull()?.let { usuarioDao.guardar(it.aEntidad(configuracion)) }
                Unit
            }

    /** [refrescar] el perfil de la sesión abierta. */
    suspend fun refrescarActual(): ResultadoRed<Unit> {
        val id = idActual() ?: return ResultadoRed.SesionCaducada
        return refrescar(id)
    }

    /**
     * Guarda los perfiles que llegan dentro de otras respuestas, por ejemplo los autores del muro
     * (`select=*,autor:perfiles(*)`). Así el nombre y el avatar nuevos de cada persona se ven
     * en sus publicaciones al refrescar.
     */
    suspend fun guardarDelServidor(perfiles: List<PerfilDto>) {
        usuarioDao.guardarTodos(perfiles.map { it.aEntidad(configuracion) })
    }

    /**
     * Guarda nombre, biografía y estado en el servidor y, si va bien, en la copia de Room
     * (E4-05.2, #122). Validarlos es cosa del ViewModel; el servidor los vuelve a comprobar.
     * Sin conexión no se guarda nada y la pantalla conserva lo escrito.
     */
    suspend fun actualizarPerfil(nombre: String, biografia: String, estado: String): ResultadoRed<Unit> {
        val id = idActual() ?: return ResultadoRed.SesionCaducada
        val resultado = llamarAlServidor {
            perfilService.actualizarDatos(igual(id), CambiosPerfilDto(nombre, biografia, estado))
        }
        return guardarFilaDevuelta(resultado)
    }

    /**
     * Cambia el avatar (E4-05.2, #122):
     * 1. Sube [imagen], ya reescalada a 720p, a `imagenes/{id de la cuenta}/{uuid}.jpg`.
     * 2. Apunta el perfil a la imagen nueva y guarda la respuesta en Room.
     * 3. Borra del servidor la imagen anterior.
     *
     * Si falla el paso 2, se borra la imagen recién subida para no dejarla suelta.
     * Si falla el paso 3, el avatar ya está cambiado: solo queda una imagen sin usar.
     */
    suspend fun cambiarAvatar(imagen: File): ResultadoRed<Unit> {
        val id = idActual() ?: return ResultadoRed.SesionCaducada
        // La ruta anterior se pide al servidor: la copia de Room podría no estar al día
        val actual = llamarAlServidor { perfilService.obtener(igual(id)) }
        if (actual is ResultadoRed.Fallo) return actual
        val anterior = (actual as ResultadoRed.Correcto).datos.firstOrNull()?.avatarRuta

        val esPng = imagen.extension.equals("png", ignoreCase = true)
        val ruta = "$id/${UUID.randomUUID()}.${if (esPng) "png" else "jpg"}"
        val cuerpo = imagen.asRequestBody((if (esPng) "image/png" else "image/jpeg").toMediaType())
        val subida = llamarAlServidor { almacenamiento.subir(ConfiguracionSupabase.BUCKET_IMAGENES, ruta, cuerpo).exigirExito() }
        if (subida is ResultadoRed.Fallo) return subida

        val cambio = guardarFilaDevuelta(
            llamarAlServidor { perfilService.actualizarAvatar(igual(id), CambioAvatarDto(ruta)) },
        )
        if (cambio is ResultadoRed.Fallo) {
            borrarImagen(ruta)
            return cambio
        }
        if (anterior != null && anterior != ruta) borrarImagen(anterior)
        return cambio
    }

    /** Quita el avatar: el perfil se queda sin foto y la imagen se borra del servidor. */
    suspend fun quitarAvatar(): ResultadoRed<Unit> {
        val id = idActual() ?: return ResultadoRed.SesionCaducada
        val anterior = usuarioDao.obtener(id)?.avatarRuta
        val cambio = guardarFilaDevuelta(
            llamarAlServidor { perfilService.actualizarAvatar(igual(id), CambioAvatarDto(null)) },
        )
        if (cambio is ResultadoRed.Correcto && anterior != null) borrarImagen(anterior)
        return cambio
    }

    /**
     * Con `Prefer: return=representation` el servidor devuelve la fila tal como ha quedado, y es
     * esa la que se guarda en Room. Si no devuelve ninguna, la política de seguridad no ha dejado
     * cambiarla.
     */
    private suspend fun guardarFilaDevuelta(resultado: ResultadoRed<List<PerfilDto>>): ResultadoRed<Unit> =
        when (resultado) {
            is ResultadoRed.Fallo -> resultado
            is ResultadoRed.Correcto -> {
                val fila = resultado.datos.firstOrNull()
                if (fila == null) {
                    ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
                } else {
                    usuarioDao.guardar(fila.aEntidad(configuracion))
                    ResultadoRed.Correcto(Unit)
                }
            }
        }

    /** Si no se puede borrar (por ejemplo, sin red), la imagen se queda en el servidor sin usar. No es grave. */
    private suspend fun borrarImagen(ruta: String) {
        llamarAlServidor { almacenamiento.borrar(ConfiguracionSupabase.BUCKET_IMAGENES, ruta).exigirExito() }
    }

    private companion object {
        /** Código de Postgres para «sin permiso» (lo traduce MensajesRed). */
        const val CODIGO_SIN_PERMISO = "42501"
    }
}
