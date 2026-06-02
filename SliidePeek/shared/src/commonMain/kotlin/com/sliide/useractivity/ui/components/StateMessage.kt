package com.sliide.useractivity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

enum class StateTone { Neutral, Accent, Error }

@Composable
fun StateMessage(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: StateTone = StateTone.Neutral,
    code: String? = null,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    ghostAction: Boolean = false,
    onAction: (() -> Unit)? = null,
) {
    val iconColor = when (tone) {
        StateTone.Accent -> MaterialTheme.colorScheme.primary
        StateTone.Error -> MaterialTheme.colorScheme.error
        StateTone.Neutral -> MaterialTheme.signal.ink3
    }
    val iconBorder = when (tone) {
        StateTone.Accent -> MaterialTheme.colorScheme.primary
        StateTone.Error -> MaterialTheme.colorScheme.error
        StateTone.Neutral -> MaterialTheme.colorScheme.outline
    }
    val iconBg = if (tone == StateTone.Accent) MaterialTheme.signal.accentSoft else MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(Radius.xl),
            color = iconBg,
            border = BorderStroke(1.5.dp, iconBorder),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            modifier = Modifier.padding(top = 14.dp),
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            modifier = Modifier.padding(top = 6.dp).widthIn(max = 240.dp),
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
        if (code != null) {
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = code,
                color = MaterialTheme.signal.ink3,
                style = MaterialTheme.signalType.code,
            )
        }
        if (actionLabel != null && onAction != null) {
            val content: @Composable () -> Unit = {
                if (actionIcon != null) {
                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(17.dp))
                    androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                }
                Text(actionLabel)
            }
            if (ghostAction) {
                OutlinedButton(modifier = Modifier.padding(top = 16.dp), onClick = onAction) {
                    content()
                }
            } else {
                Button(modifier = Modifier.padding(top = 16.dp), onClick = onAction) {
                    content()
                }
            }
        }
    }
}
