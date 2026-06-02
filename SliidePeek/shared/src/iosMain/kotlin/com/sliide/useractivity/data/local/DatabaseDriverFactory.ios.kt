package com.sliide.useractivity.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sliide.useractivity.domain.connectivity.ConnectivityMonitor
import com.sliide.useractivity.domain.connectivity.IosConnectivityMonitor

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = SliidePeekDatabase.Schema,
        name = "sliidepeek.db",
    )

    actual fun createConnectivityMonitor(): ConnectivityMonitor = IosConnectivityMonitor()
}
