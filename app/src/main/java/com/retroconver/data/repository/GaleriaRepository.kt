package com.retroconver.data.repository

import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.GaleriaDao
import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.remote.AlbumDto
import com.retroconver.data.remote.AlmacenamientoService
import com.retroconver.data.remote.ConfiguracionSupabase
import com.retroconver.data.remote.GaleriaService
import com.retroconver.data.remote.NuevaFotoDto
import com.retroconver.data.remote.NuevoAlbumDto
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.exigirExito
import com.retroconver.data.remote.igual
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.sesion.SesionRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

/** Imagen ya reescalada a 720p en el móvil, lista para subirla. */
data class ImagenParaSubir(
    val fichero: File,
    val ancho: Int? = null,
    val alto: Int? = null,
)

/**
 * Única puerta de acceso a los álbumes y las fotos (E4-10.1, #68). Sigue el patrón de
 * docs/arquitectura.md: las pantallas leen de Room y el repositorio la mantiene al día con
 * el servidor; al escribir, primero el servidor y después su respuesta en Room.
 *
 * Cualquiera con sesión puede ver los álbumes de cualquier persona, pero solo crear, añadir
 * fotos o borrar en los suyos (lo comprueban las políticas de seguridad del servidor).
 */
class GaleriaRepository(
    private val galeriaDao: GaleriaDao,
    private val usuarioDao: UsuarioDao,
    private val servicio: GaleriaService,
    private val almacenamiento: AlmacenamientoService,
    private val sesion: SesionRepository,
    private val configuracion: ConfiguracionSupabase,
) {

    /** Id de la cuenta con sesión: la dueña de los álbumes que se crean. */
    suspend fun idActual(): String? = sesion.sesionActual()?.idCuenta

    /** Álbumes de una persona con su número de fotos y su portada, el más reciente primero. */
    fun albumesDe(propietarioId: String): Flow<List<AlbumConPortada>> = galeriaDao.observarAlbumesDe(propietarioId)

    fun album(id: String): Flow<AlbumEntity?> = galeriaDao.observarAlbum(id)

    /** Fotos de un álbum, la más reciente primero. */
    fun fotos(albumId: String): Flow<List<FotoEntity>> = galeriaDao.observarFotos(albumId)

    fun foto(id: String): Flow<FotoEntity?> = galeriaDao.observarFoto(id)

    /**
     * Descarga los álbumes de [propietarioId] con todas sus fotos y deja Room igual que el
     * servidor, también si se ha borrado algo desde otro móvil.
     */
    suspend fun refrescarAlbumesDe(propietarioId: String): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.albumesDe(igual(propietarioId)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val albumes = (resultado as ResultadoRed.Correcto).datos
        guardarDuenos(albumes)
        galeriaDao.sincronizarAlbumesDe(
            propietarioId = propietarioId,
            albumes = albumes.map { it.aEntidad() },
            fotos = albumes.flatMap { album -> album.fotos.map { it.aEntidad(configuracion) } },
        )
        return ResultadoRed.Correcto(Unit)
    }

    /** Descarga un álbum con sus fotos. Si se ha borrado en el servidor, se quita también de Room. */
    suspend fun refrescarAlbum(albumId: String): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.album(igual(albumId)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val album = (resultado as ResultadoRed.Correcto).datos.firstOrNull()
        if (album == null) {
            galeriaDao.borrarAlbum(albumId)
            return ResultadoRed.ErrorServidor(404, "not_found")
        }
        guardarDuenos(listOf(album))
        galeriaDao.sincronizarAlbum(album.aEntidad(), album.fotos.map { it.aEntidad(configuracion) })
        return ResultadoRed.Correcto(Unit)
    }

    /** Crea un álbum propio (E4-10.2, #69) y devuelve su id. [titulo] ya viene validado. */
    suspend fun crearAlbum(titulo: String): ResultadoRed<String> {
        val resultado = llamarAlServidor { servicio.crearAlbum(NuevoAlbumDto(titulo = titulo)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        val album = (resultado as ResultadoRed.Correcto).datos.firstOrNull()
            ?: return ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        guardarDuenos(listOf(album))
        galeriaDao.guardarAlbum(album.aEntidad())
        return ResultadoRed.Correcto(album.id)
    }

    /**
     * Añade una foto a un álbum propio (E4-10.4, #71):
     * 1. Sube la imagen a `imagenes/{id de la cuenta}/{uuid}.jpg`.
     * 2. Crea su fila en `fotos` y la guarda en Room.
     *
     * Si falla el paso 2 (por ejemplo, el álbum no es tuyo), se borra la imagen recién subida.
     */
    suspend fun subirFoto(albumId: String, imagen: ImagenParaSubir): ResultadoRed<Unit> {
        val id = idActual() ?: return ResultadoRed.SesionCaducada
        val esPng = imagen.fichero.extension.equals("png", ignoreCase = true)
        val ruta = "$id/${UUID.randomUUID()}.${if (esPng) "png" else "jpg"}"
        val cuerpo = imagen.fichero.asRequestBody((if (esPng) "image/png" else "image/jpeg").toMediaType())

        val subida = llamarAlServidor {
            almacenamiento.subir(ConfiguracionSupabase.BUCKET_IMAGENES, ruta, cuerpo).exigirExito()
        }
        if (subida is ResultadoRed.Fallo) return subida

        val fila = llamarAlServidor {
            servicio.anadirFoto(NuevaFotoDto(albumId = albumId, ruta = ruta, ancho = imagen.ancho, alto = imagen.alto))
        }
        val foto = (fila as? ResultadoRed.Correcto)?.datos?.firstOrNull()
        if (foto == null) {
            borrarImagen(ruta)
            return fila as? ResultadoRed.Fallo ?: ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        }
        galeriaDao.guardarFoto(foto.aEntidad(configuracion))
        return ResultadoRed.Correcto(Unit)
    }

    /**
     * Borra una foto propia (E4-10.4, #71): primero su fila del servidor, después el archivo del
     * almacenamiento y por último la copia de Room. Si no se puede borrar el archivo, la foto ya
     * no aparece en ningún sitio y solo queda una imagen sin usar.
     */
    suspend fun borrarFoto(foto: FotoEntity): ResultadoRed<Unit> {
        val resultado = llamarAlServidor { servicio.borrarFoto(igual(foto.id)) }
        if (resultado is ResultadoRed.Fallo) return resultado
        if ((resultado as ResultadoRed.Correcto).datos.isEmpty()) {
            return ResultadoRed.ErrorServidor(403, CODIGO_SIN_PERMISO)
        }
        borrarImagen(foto.ruta)
        galeriaDao.borrarFoto(foto.id)
        return ResultadoRed.Correcto(Unit)
    }

    /** Los álbumes tienen clave foránea a su dueño en Room: su perfil se guarda antes que ellos. */
    private suspend fun guardarDuenos(albumes: List<AlbumDto>) {
        val duenos = albumes.mapNotNull { it.propietario }.distinctBy { it.id }
        if (duenos.isNotEmpty()) usuarioDao.guardarTodos(duenos.map { it.aEntidad(configuracion) })
    }

    private suspend fun borrarImagen(ruta: String) {
        llamarAlServidor { almacenamiento.borrar(ConfiguracionSupabase.BUCKET_IMAGENES, ruta).exigirExito() }
    }

    private companion object {
        /** Código de Postgres para «sin permiso» (lo traduce MensajesRed). */
        const val CODIGO_SIN_PERMISO = "42501"
    }
}
