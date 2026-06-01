package com.sliide.useractivity.ui.shell

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Compose is hosted in a single UIViewController, so there is no native iOS navigation stack
    // to intercept here. In-app back affordances call the same navigator directly.
}
