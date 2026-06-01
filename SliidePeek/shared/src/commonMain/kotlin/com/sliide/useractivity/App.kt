package com.sliide.useractivity

import androidx.compose.runtime.Composable
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.ui.shell.AppRoot
import com.sliide.useractivity.ui.theme.AppTheme

@Composable
fun App(databaseDriverFactory: DatabaseDriverFactory) {
    AppTheme {
        AppRoot(databaseDriverFactory = databaseDriverFactory)
    }
}
