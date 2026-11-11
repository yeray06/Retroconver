package com.retroconver.data.sesion

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.retroconver.data.remote.AuthService
import com.retroconver.data.remote.CredencialesDto
import com.retroconver.data.remote.DatosRegistroDto
import com.retroconver.data.remote.FuenteDeToken
import com.retroconver.data.remote.RegistroDto
import com.retroconver.data.remote.RenovarSesionDto
import com.retroconver.data.remote.ResultadoRed
import com.retroconver.data.remote.SesionDto
import com.retroconver.data.remote.exigirExito
import com.retroconver.data.remote.llamarAlServidor
import com.retroconver.data.remote.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.time.Duration
import java.time.Instant

/** Sesión abierta en el móvil. La contraseña no está: solo los tokens que da Supabase Auth. */
data class Sesion(
    /** UUID de la cuenta, que es también el id de su perfil. */
    val idCuenta: String,
    val tokenAcceso: String,
    val tokenRenovacion: String,
    /** Cuándo deja de valer [tokenAcceso] (una hora después de pedirlo). */
    val caducaEn: Instant,
) {
    fun caducaAntesDe(momento: Instant): Boolean = !caducaEn.isAfter(momento)
}

/** Si hay alguien dentro. Decide si la app abre el inicio de sesión o el muro. */
sealed interface EstadoSesion {
    /** Todavía no se ha leído DataStore (solo dura un instante al abrir la app). */
    data object Cargando : EstadoSesion

    data object SinSesion : EstadoSesion

    data class Iniciada(val idCuenta: String) : EstadoSesion
}

/** Cómo ha ido un registro correcto. */
enum class ResultadoRegistro {
    /** La cuenta está creada y la sesión abierta. */
    DENTRO,

    /** El proyecto pide confirmar el email: hay que pulsar el enlace del correo y luego entrar. */
    CONFIRMAR_EMAIL,
}

/**
 * Sesión de la persona usuaria con Supabase Auth (E4-04.1, #117): registrarse, entrar,
 * renovar el token y salir.
 *
 * DataStore guarda el token de acceso, el de renovación, el id de la cuenta y cuándo caduca
 * el token. **La contraseña no se guarda nunca**: solo viaja al servidor al entrar o registrarse.
 *
 * También es la [FuenteDeToken] del cliente de red: cada petición pide aquí el token, y si
 * está a punto de caducar se renueva antes de enviarla. Si aun así el servidor responde 401,
 * [RenovadorDeSesion] lo renueva y repite la petición. Si el servidor ya no acepta el token
 * de renovación, la sesión se cierra y la app vuelve al inicio de sesión.
 *
 * @param alTerminarSesion Se llama al cerrar la sesión (o al caducar del todo) para borrar
 *   la copia local de los datos de esa persona: Room y las imágenes.
 * @param reloj Solo se cambia en las pruebas.
 */
class SesionRepository(
    private val almacen: DataStore<Preferences>,
    private val auth: AuthService,
    private val alTerminarSesion: suspend () -> Unit = {},
    private val reloj: () -> Instant = Instant::now,
) : FuenteDeToken {

    /** Lo guardado en DataStore. Si el fichero se ha estropeado, se trata como «sin sesión». */
    private val datos: Flow<Preferences> = almacen.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    /** Solo un hilo renueva el token a la vez; los demás esperan y usan el nuevo. */
    private val cerrojo = Any()

    /** Si hay sesión. Emite otra vez al entrar o salir, así la app cambia de pantalla sola. */
    val estado: Flow<EstadoSesion> = datos
        .map { preferencias -> preferencias.aSesion()?.let { EstadoSesion.Iniciada(it.idCuenta) } ?: EstadoSesion.SinSesion }
        .distinctUntilChanged()

    /** Id de la cuenta con la sesión abierta, o `null` si no hay sesión. */
    val idCuenta: Flow<String?> = datos.map { it[ID_CUENTA] }.distinctUntilChanged()

    /** La sesión guardada ahora mismo, o `null`. */
    suspend fun sesionActual(): Sesion? = datos.first().aSesion()

    /** Entra con email y contraseña. Si va bien, guarda la sesión y la app pasa al muro. */
    suspend fun iniciarSesion(email: String, contrasena: String): ResultadoRed<Unit> =
        llamarAlServidor { auth.iniciarSesion(CredencialesDto(email.trim(), contrasena)) }
            .map { guardar(it.aSesion(reloj())) }

    /**
     * Crea una cuenta. El servidor crea también su perfil con [nombre].
     * Si el proyecto no pide confirmar el email, la sesión queda abierta ([ResultadoRegistro.DENTRO]).
     */
    suspend fun registrar(nombre: String, email: String, contrasena: String): ResultadoRed<ResultadoRegistro> =
        llamarAlServidor { auth.registrar(RegistroDto(email.trim(), contrasena, DatosRegistroDto(nombre.trim()))) }
            .map { respuesta ->
                val sesion = respuesta.sesion()
                if (sesion == null) {
                    ResultadoRegistro.CONFIRMAR_EMAIL
                } else {
                    guardar(sesion.aSesion(reloj()))
                    ResultadoRegistro.DENTRO
                }
            }

    /**
     * Cierra la sesión (E4-04.3, #119). Avisa al servidor si hay red, y pase lo que pase borra la
     * copia local y los tokens: sin conexión también se puede salir.
     */
    suspend fun cerrarSesion() {
        val sesion = sesionActual() ?: return
        llamarAlServidor { auth.cerrarSesion("Bearer ${sesion.tokenAcceso}").exigirExito() }
        terminar()
    }

    // ---------- Para el cliente de red (hilos de OkHttp, nunca el principal) ----------

    override fun tokenDeAcceso(): String? {
        val sesion = leerSincrono() ?: return null
        if (!sesion.caducaAntesDe(reloj().plus(MARGEN_RENOVACION))) return sesion.tokenAcceso
        // A punto de caducar: se renueva antes de enviar la petición. Sin red se prueba con el que hay.
        return renovar(sesion.tokenAcceso)?.tokenAcceso ?: leerSincrono()?.tokenAcceso
    }

    /**
     * El servidor ha rechazado [tokenRechazado] (401). Devuelve la sesión renovada para repetir
     * la petición, o `null` si no se puede (sin red, o el token de renovación ya no vale).
     */
    fun renovarTrasRechazo(tokenRechazado: String?): Sesion? = renovar(tokenRechazado)

    private fun renovar(tokenQueFalla: String?): Sesion? = synchronized(cerrojo) {
        val actual = leerSincrono() ?: return null
        // Otro hilo la ha renovado mientras este esperaba: se usa esa
        if (actual.tokenAcceso != tokenQueFalla && !actual.caducaAntesDe(reloj().plus(MARGEN_RENOVACION))) {
            return actual
        }
        val respuesta = try {
            auth.renovarSesion(RenovarSesionDto(actual.tokenRenovacion)).execute()
        } catch (e: IOException) {
            return null // sin red: la sesión se mantiene y se volverá a intentar
        } catch (e: RuntimeException) {
            return null // respuesta que no se entiende: igual que sin red
        }
        val cuerpo = respuesta.body()
        when {
            respuesta.isSuccessful && cuerpo != null -> {
                val nueva = cuerpo.aSesion(reloj())
                runBlocking { guardar(nueva) }
                nueva
            }
            // 400 o 401: el token de renovación ya no vale (sesión cerrada en otro sitio o muy antigua)
            respuesta.code() == 400 || respuesta.code() == 401 -> {
                runBlocking { terminar() }
                null
            }
            else -> null // error del servidor: se mantiene la sesión
        }
    }

    private fun leerSincrono(): Sesion? = runBlocking { sesionActual() }

    // ---------- DataStore ----------

    private suspend fun guardar(sesion: Sesion) {
        almacen.edit { preferencias ->
            preferencias[ID_CUENTA] = sesion.idCuenta
            preferencias[TOKEN_ACCESO] = sesion.tokenAcceso
            preferencias[TOKEN_RENOVACION] = sesion.tokenRenovacion
            preferencias[CADUCA_EN] = sesion.caducaEn.epochSecond
        }
    }

    /** Primero se borra la copia local y después los tokens, que es lo que lleva la app al inicio de sesión. */
    private suspend fun terminar() {
        alTerminarSesion()
        almacen.edit { it.clear() }
    }

    private fun Preferences.aSesion(): Sesion? {
        return Sesion(
            idCuenta = this[ID_CUENTA] ?: return null,
            tokenAcceso = this[TOKEN_ACCESO] ?: return null,
            tokenRenovacion = this[TOKEN_RENOVACION] ?: return null,
            caducaEn = Instant.ofEpochSecond(this[CADUCA_EN] ?: return null),
        )
    }

    companion object {
        /** Se renueva el token si le queda menos que esto, para que no caduque a mitad de una subida. */
        val MARGEN_RENOVACION: Duration = Duration.ofSeconds(60)

        private val ID_CUENTA = stringPreferencesKey("id_cuenta")
        private val TOKEN_ACCESO = stringPreferencesKey("token_acceso")
        private val TOKEN_RENOVACION = stringPreferencesKey("token_renovacion")
        private val CADUCA_EN = longPreferencesKey("caduca_en")
    }
}

/** Convierte la respuesta de Supabase Auth en la sesión que se guarda. */
fun SesionDto.aSesion(ahora: Instant): Sesion = Sesion(
    idCuenta = user.id,
    tokenAcceso = tokenAcceso,
    tokenRenovacion = tokenRenovacion,
    caducaEn = ahora.plusSeconds(duracionSegundos),
)
