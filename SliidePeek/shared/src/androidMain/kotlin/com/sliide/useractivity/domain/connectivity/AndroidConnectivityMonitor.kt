package com.sliide.useractivity.domain.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidConnectivityMonitor(
    context: Context,
) : ConnectivityMonitor {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val status: Flow<ConnectivityStatus> = callbackFlow {
        trySend(connectivityManager.currentStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // The default network is changing; query the system state but wait for capabilities
                // before trusting the connection as internet-usable.
                trySend(connectivityManager.currentStatus())
            }

            override fun onLost(network: Network) {
                trySend(connectivityManager.currentStatus())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(networkCapabilities.toConnectivityStatus())
            }

            override fun onUnavailable() {
                trySend(ConnectivityStatus.Offline)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun ConnectivityManager.currentStatus(): ConnectivityStatus {
        val network = activeNetwork ?: return ConnectivityStatus.Offline
        val capabilities = getNetworkCapabilities(network) ?: return ConnectivityStatus.Offline
        return capabilities.toConnectivityStatus()
    }

    private fun NetworkCapabilities.toConnectivityStatus(): ConnectivityStatus =
        if (
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            ConnectivityStatus.Online
        } else {
            ConnectivityStatus.Offline
        }
}
