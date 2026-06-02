package com.sliide.useractivity.domain.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ConnectivityMonitor {
    val status: Flow<ConnectivityStatus>
}

enum class ConnectivityStatus {
    Online,
    Offline,
    Unknown,
}

class StaticConnectivityMonitor(
    connectivityStatus: ConnectivityStatus = ConnectivityStatus.Unknown,
) : ConnectivityMonitor {
    override val status: Flow<ConnectivityStatus> = flowOf(connectivityStatus)
}
