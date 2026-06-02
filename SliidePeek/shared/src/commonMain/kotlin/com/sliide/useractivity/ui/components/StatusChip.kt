package com.sliide.useractivity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

/**
 * Signal status chip — squared, mono uppercase label, colour always paired with the label.
 *
 * - [active] = true → green "Active" (status dot)
 * - [active] = false & [neutral] = false → muted "Inactive" (status dot)
 * - [neutral] = true → informational chip (e.g. gender), no dot
 */
@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    neutral: Boolean = false,
) {
    val content = when {
        active -> MaterialTheme.signal.green
        neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.signal.ink3
    }
    val container = when {
        active -> MaterialTheme.signal.greenBg
        else -> MaterialTheme.signal.line2
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.xs),
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!neutral) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(content, RoundedCornerShape(1.5.dp)),
                )
            }
            Text(
                text = label.uppercase(),
                color = content,
                style = MaterialTheme.signalType.eyebrow,
            )
        }
    }
}
