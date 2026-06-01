package com.sliide.useractivity.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = SliidePeekDatabase.Schema,
        name = "sliidepeek.db",
    )
}
