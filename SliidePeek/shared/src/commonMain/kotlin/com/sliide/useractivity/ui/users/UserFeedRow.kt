package com.sliide.useractivity.ui.users

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.UserFeedItem
import com.sliide.useractivity.ui.components.InitialsAvatar
import com.sliide.useractivity.ui.components.StatusChip
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.SignalMotion
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserFeedRow(
    user: UserFeedItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    highlighted: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "rowPressScale")

    val accent = MaterialTheme.colorScheme.primary
    val targetContainer = when {
        highlighted -> MaterialTheme.signal.accentSoft
        selected -> MaterialTheme.signal.accentSoft
        else -> MaterialTheme.colorScheme.surface
    }
    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(SignalMotion.HIGHLIGHT),
        label = "rowContainer",
    )
    val emphasised = selected || highlighted

    val deleteAction = onLongClick?.let {
        listOf(CustomAccessibilityAction("Delete ${user.name}") { it(); true })
    } ?: emptyList()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .semantics { customActions = deleteAction }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick,
                onLongClick = onLongClick?.let {
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
            ),
        shape = RoundedCornerShape(Radius.row),
        color = container,
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasised) accent else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Box(
            modifier = Modifier.drawBehind {
                if (emphasised) {
                    drawRect(color = accent, size = Size(3.dp.toPx(), size.height))
                }
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InitialsAvatar(
                    initials = userInitials(user.name),
                    size = if (compact) 34.dp else 42.dp,
                    accent = highlighted,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = user.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = user.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.signalType.email,
                    )
                }
                if (compact) {
                    Text(
                        text = shortRelativeTime(user.relativeTimestamp),
                        color = MaterialTheme.signal.ink3,
                        maxLines = 1,
                        style = MaterialTheme.signalType.meta,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        StatusChip(
                            label = user.status.label,
                            active = user.status == UserStatus.Active,
                        )
                        Text(
                            text = user.relativeTimestamp,
                            color = MaterialTheme.signal.ink3,
                            maxLines = 1,
                            style = MaterialTheme.signalType.meta,
                        )
                    }
                }
            }
        }
    }
}

private val UserStatus.label: String
    get() = when (this) {
        UserStatus.Active -> "Active"
        UserStatus.Inactive -> "Inactive"
        UserStatus.Unknown -> "Unknown"
    }
