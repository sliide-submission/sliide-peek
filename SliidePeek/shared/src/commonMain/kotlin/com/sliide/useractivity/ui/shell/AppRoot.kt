package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.sliide.useractivity.ui.navigation.AppNavigator

@Composable
fun AppRoot() {
    val navigator = remember { AppNavigator() }

    BoxWithConstraints {
        when (appLayoutClassForWidth(maxWidth)) {
            AppLayoutClass.Compact -> CompactAppShell(navigator = navigator)
            AppLayoutClass.Expanded -> ExpandedAppShell(navigator = navigator, availableWidth = maxWidth)
        }
    }
}
