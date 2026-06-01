package com.sliide.useractivity.ui.shell

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppLayoutClass {
    Compact,
    Expanded,
}

fun appLayoutClassForWidth(width: Dp): AppLayoutClass =
    if (width < CompactWidthThreshold) AppLayoutClass.Compact else AppLayoutClass.Expanded

fun masterPaneWidthFor(width: Dp): Dp = when {
    width < 840.dp -> 260.dp
    else -> 320.dp
}

val CompactWidthThreshold = 700.dp
