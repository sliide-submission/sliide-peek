package com.sliide.useractivity.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Signal radii (squared, technical). From `design/design_tokens_and_handoff.html` §04. */
object Radius {
    val xs = 5.dp   // chips, segments
    val sm = 6.dp   // small buttons
    val md = 8.dp   // cards, avatars (small)
    val row = 12.dp // feed rows / list cards
    val lg = 11.dp  // large avatar, icon containers
    val xl = 14.dp  // state-block icon, sheets
    val xxl = 18.dp // FAB
    val dialog = 20.dp
}

/** Signal border strokes. */
object Stroke {
    val hairline = 1.dp   // dividers, card edges (color = line)
    val control = 1.5.dp  // text fields, segmented controls
    val selected = 3.dp   // selected-row left keyline (color = accent)
}

val SignalShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl),
)
