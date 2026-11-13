package com.retroconver.data.remote

import com.retroconver.BuildConfig

/**
 * Dónde está el servidor Supabase y con qué clave pública se entra (E4-03.1, #114).
 *
 * Los dos valores salen de `local.properties` a través de `BuildConfig`, así no se suben al
 * repositorio. Si faltan, la app funciona igual pero cada llamada al servidor devuelve un
 * error que explica que no está configurado (ver supabase/README.md, «Conectar la app»).
 *
 * @param url URL del proyecto, por ejemplo `https://xxxx.supabase.co`.
 * @param claveAnonima Clave pública (*anon key*). Solo da acceso a lo que permiten las políticas de seguridad.
 */
class ConfiguracionSupabase(
    val url: String,
    val claveAnonima: String,
) {

    /** Hay URL y clave. Sin ellas no se intenta ninguna conexión. */
    val configurada: Boolean get() = url.isNotBlank() && claveAnonima.isNotBlank()

    /**
     * URL base para Retrofit, siempre acabada en «/». Sin configurar es una dirección que no existe,
     * porque Retrofit no admite una URL vacía. De todas formas, el interceptor corta la llamada antes.
     */
    val urlBase: String get() = if (configurada) url.trim().trimEnd('/') + "/" else URL_SIN_CONFIGURAR

    /**
     * URL pública de una imagen del bucket, para que Coil la cargue sin token.
     *
     * @param ruta Ruta dentro del bucket, tal como se guarda en las tablas: `{id de la cuenta}/{uuid}.jpg`.
     */
    fun urlPublica(ruta: String, bucket: String = BUCKET_IMAGENES): String =
        "${urlBase}storage/v1/object/public/$bucket/$ruta"

    // No se escribe la clave en los registros: aunque sea pública, no hace falta repartirla
    override fun toString(): String = "ConfiguracionSupabase(url=$url, configurada=$configurada)"

    companion object {
        /** Bucket de Supabase Storage donde van el avatar, las imágenes del muro y las fotos de los álbumes. */
        const val BUCKET_IMAGENES = "imagenes"

        private const val URL_SIN_CONFIGURAR = "https://supabase-sin-configurar.invalid/"

        /** La configuración de esta compilación, sacada de `local.properties`. */
        fun desdeBuildConfig(): ConfiguracionSupabase =
            ConfiguracionSupabase(url = BuildConfig.SUPABASE_URL, claveAnonima = BuildConfig.SUPABASE_ANON_KEY)
    }
}
