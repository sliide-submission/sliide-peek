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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.SignalMotion
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    kicker: String? = "// directory",
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    refreshing: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)) {
            if (kicker != null && !showBack) {
                Text(
                    text = kicker,
                    color = MaterialTheme.signal.ink3,
                    style = MaterialTheme.signalType.eyebrow,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBack && onBack != null) {
                    TopIconButton(SignalIcons.ChevronLeft, "Back", onBack)
                }
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (subtitle != null) {
                    CountPill(subtitle)
                }
                Spacer(Modifier.weight(1f))
                if (onAdd != null) {
                    TopIconButton(SignalIcons.Plus, "Add user", onAdd)
                }
                if (onRefresh != null) {
                    TopIconButton(SignalIcons.Refresh, "Refresh", onRefresh, spinning = refreshing)
                }
            }
        }
    }
}

@Composable
private fun CountPill(text: String) {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = MaterialTheme.signal.accentSoft,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.signalType.meta,
        )
    }
}

@Composable
private fun TopIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    active: Boolean = false,
    spinning: Boolean = false,
) {
    val rotation = if (spinning) {
        val transition = rememberInfiniteTransition(label = "spin")
        val r by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(SignalMotion.REFRESH_SPIN, easing = LinearEasing)),
            label = "refreshSpin",
        )
        r
    } else {
        0f
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(Radius.md),
        color = if (active) MaterialTheme.signal.accentSoft else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).rotate(rotation),
            )
        }
    }
}
