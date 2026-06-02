package com.sliide.useractivity.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/** Signal motion tokens — Material 3 emphasized easing + target durations (ms). */
object SignalMotion {
    val emphDecel = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // elements entering
    val emphAccel = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f) // elements leaving
    val standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)   // in-place changes

    const val SHIMMER = 1400
    const val CONTENT_FADE = 200
    const val SHEET_ENTER = 280
    const val SHEET_EXIT = 220
    const val ROW_INSERT = 240
    const val HIGHLIGHT = 1200 // amber/accent decay on add/restore
    const val LONG_PRESS = 450
    const val DIALOG_ENTER = 260
    const val ROW_REMOVE = 240
    const val REFLOW = 200
    const val ROW_STAGGER = 30
    const val REFRESH_SPIN = 900
}
