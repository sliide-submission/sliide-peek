package com.sliide.useractivity.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.SignalMotion
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

enum class BannerVariant { Offline, Refreshing, Error }

/** Informational feed banner — offline (neutral), refreshing (accent, spinning), or error (danger). */
@Composable
fun SignalBanner(
    message: String,
    variant: BannerVariant,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    val content: Color = when (variant) {
        BannerVariant.Offline -> MaterialTheme.colorScheme.onSurfaceVariant
        BannerVariant.Refreshing -> MaterialTheme.colorScheme.primary
        BannerVariant.Error -> MaterialTheme.colorScheme.error
    }
    val container: Color = when (variant) {
        BannerVariant.Offline -> MaterialTheme.signal.line2
        BannerVariant.Refreshing -> MaterialTheme.signal.accentSoft
        BannerVariant.Error -> MaterialTheme.signal.dangerBg
    }
    val border = if (variant == BannerVariant.Offline) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    } else {
        null
    }
    val icon = when (variant) {
        BannerVariant.Offline -> SignalIcons.CloudOff
        BannerVariant.Refreshing -> SignalIcons.Refresh
        BannerVariant.Error -> SignalIcons.AlertCircle
    }

    val rotation = if (variant == BannerVariant.Refreshing) {
        val transition = rememberInfiniteTransition(label = "bannerSpin")
        val r by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(SignalMotion.REFRESH_SPIN, easing = LinearEasing)),
            label = "bannerSpinValue",
        )
        r
    } else {
        0f
    }

    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(Radius.md),
        color = container,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(15.dp).rotate(rotation),
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = content,
                style = MaterialTheme.typography.bodySmall,
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    color = if (variant == BannerVariant.Refreshing) content else MaterialTheme.signal.ink3,
                    style = MaterialTheme.signalType.meta,
                )
            }
        }
    }
}
