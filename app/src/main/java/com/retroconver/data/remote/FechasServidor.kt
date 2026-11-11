package com.retroconver.data.remote

import java.time.Instant
import java.time.OffsetDateTime

/**
 * Convierte una fecha del servidor en [Instant], que es como la guarda Room.
 *
 * Postgres la envía en ISO 8601 con la zona y hasta seis decimales:
 * `2026-11-11T09:30:00.123456+00:00`. `Instant.parse` no admite «+00:00» en Android 8,
 * por eso se lee como [OffsetDateTime].
 */
fun instanteDelServidor(texto: String): Instant = OffsetDateTime.parse(texto).toInstant()
