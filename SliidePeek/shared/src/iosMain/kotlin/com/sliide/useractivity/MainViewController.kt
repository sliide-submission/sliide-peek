package com.sliide.useractivity

import androidx.compose.ui.window.ComposeUIViewController
import com.sliide.useractivity.data.local.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    App(databaseDriverFactory = DatabaseDriverFactory())
}