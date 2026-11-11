package com.retroconver.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Avisa de si el móvil tiene red (E4-03.2, #115). Con él, la app muestra una franja de
 * «Sin conexión» mientras se ve la copia guardada en Room (RNF-06).
 *
 * Solo dice si hay una red que debería dar Internet. Que el servidor conteste lo sigue
 * diciendo cada llamada con [ResultadoRed].
 */
class MonitorConexion(context: Context) {

    private val gestor = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    /** `true` mientras hay red. Emite el estado actual nada más empezar a observar. */
    val hayConexion: Flow<Boolean> = callbackFlow {
        val aviso = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Si se pierde el Wi-Fi pero hay datos móviles, Android avisa luego con onAvailable
                trySend(hayRedAhora())
            }
        }
        trySend(hayRedAhora())
        gestor.registerDefaultNetworkCallback(aviso)
        awaitClose { gestor.unregisterNetworkCallback(aviso) }
    }.distinctUntilChanged()

    private fun hayRedAhora(): Boolean {
        val red = gestor.activeNetwork ?: return false
        return gestor.getNetworkCapabilities(red)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
