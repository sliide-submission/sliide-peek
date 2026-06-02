package com.sliide.useractivity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * "Signal" design tokens that Material 3's [androidx.compose.material3.ColorScheme] has no slot for
 * (semantic status colours, shimmer, soft accent tint, inverse surface, etc.).
 *
 * Core tokens (bg, surface, ink, ink2, line, accent, danger) live on the [MaterialTheme.colorScheme];
 * everything else lives here and is reached via [MaterialTheme.signal]. Values transcribed from
 * `design/design_tokens_and_handoff.html` (light → dark pairs). Dark soft/background tints carry alpha.
 */
data class SignalColors(
    val accentSoft: Color,
    val green: Color,
    val greenBg: Color,
    val amber: Color,
    val amberBg: Color,
    val danger: Color,
    val dangerBg: Color,
    val ink3: Color,
    val line2: Color,
    val skel: Color,
    val skelHi: Color,
    val bodyText: Color,
    val inverse: Color,
    val inverseInk: Color,
)

val SignalLightColors = SignalColors(
    accentSoft = Color(0xFFE9EEFF),
    green = Color(0xFF17935A),
    greenBg = Color(0xFFE6F4EC),
    amber = Color(0xFFC07A12),
    amberBg = Color(0xFFF7EEDD),
    danger = Color(0xFFD24B4B),
    dangerBg = Color(0xFFFBECEC),
    ink3 = Color(0xFF9A9CA3),
    line2 = Color(0xFFEFF0F2),
    skel = Color(0xFFE9EAED),
    skelHi = Color(0xFFF4F5F6),
    bodyText = Color(0xFF2C2E34),
    inverse = Color(0xFF1C1D22),
    inverseInk = Color(0xFFF4F5F6),
)

val SignalDarkColors = SignalColors(
    accentSoft = Color(0x295B82FF), // ~16% alpha
    green = Color(0xFF34C77B),
    greenBg = Color(0x2634C77B),
    amber = Color(0xFFE0A33A),
    amberBg = Color(0x26E0A33A),
    danger = Color(0xFFF0716E),
    dangerBg = Color(0x24F0716E),
    ink3 = Color(0xFF62656E),
    line2 = Color(0xFF1D2027),
    skel = Color(0xFF23262F),
    skelHi = Color(0xFF2C303A),
    bodyText = Color(0xFFD3D5DA),
    inverse = Color(0xFFE7E8EC),
    inverseInk = Color(0xFF16171C),
)

val LocalSignalColors = staticCompositionLocalOf { SignalLightColors }

/** Signal extended colours for the current theme. Pairs with [MaterialTheme.colorScheme]. */
val MaterialTheme.signal: SignalColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSignalColors.current
