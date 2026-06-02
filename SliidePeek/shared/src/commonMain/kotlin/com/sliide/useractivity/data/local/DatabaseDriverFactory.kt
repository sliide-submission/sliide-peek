package com.sliide.useractivity.data.local

import app.cash.sqldelight.db.SqlDriver
import com.sliide.useractivity.domain.connectivity.ConnectivityMonitor

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
    fun createConnectivityMonitor(): ConnectivityMonitor
}
