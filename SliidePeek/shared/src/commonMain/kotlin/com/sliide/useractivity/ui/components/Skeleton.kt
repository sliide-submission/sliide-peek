package com.sliide.useractivity.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonRow(
    modifier: Modifier = Modifier,
    avatarSize: androidx.compose.ui.unit.Dp = 38.dp,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonBlock(
                modifier = Modifier.size(avatarSize),
                round = true,
                alpha = alpha,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                SkeletonBlock(Modifier.fillMaxWidth(0.62f).height(10.dp), alpha = alpha)
                SkeletonBlock(Modifier.fillMaxWidth(0.9f).height(9.dp), alpha = alpha)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBlock(
                    modifier = Modifier.size(width = 56.dp, height = 21.dp),
                    alpha = alpha,
                )
                SkeletonBlock(
                    modifier = Modifier.size(width = 46.dp, height = 8.dp),
                    alpha = alpha,
                )
            }
        }
    }
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    round: Boolean = false,
    alpha: Float = 0.65f,
) {
    Box(
        modifier = modifier
            .alpha(alpha)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = if (round) CircleShape else RoundedCornerShape(6.dp),
            ),
    )
}
