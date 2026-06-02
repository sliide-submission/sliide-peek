package com.sliide.useractivity.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.SignalMotion
import com.sliide.useractivity.ui.theme.signal

@Composable
fun SkeletonRow(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 42.dp,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.row),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(Modifier.size(avatarSize), cornerRadius = 11.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                SkeletonBlock(Modifier.fillMaxWidth(0.52f).height(11.dp))
                SkeletonBlock(Modifier.fillMaxWidth(0.84f).height(9.dp))
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SkeletonBlock(Modifier.size(width = 52.dp, height = 18.dp))
                SkeletonBlock(Modifier.size(width = 36.dp, height = 9.dp))
            }
        }
    }
}

/** A single shimmer placeholder with an animated left→right highlight sweep. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 5.dp,
) {
    val skel = MaterialTheme.signal.skel
    val skelHi = MaterialTheme.signal.skelHi
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SignalMotion.SHIMMER, easing = LinearEasing)),
        label = "shimmerSweep",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                drawRect(skel)
                val sweep = size.width
                val start = -sweep + (size.width + sweep) * progress
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(skel, skelHi, skel),
                        startX = start,
                        endX = start + sweep,
                    ),
                )
            },
    )
}
