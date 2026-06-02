package com.sliide.useractivity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sliide.useractivity.ui.theme.signalType

/**
 * Signal avatar — rounded-square tile with JetBrains Mono initials.
 * [accent] = true fills with the accent colour + white initials (newly added / restored / tablet hero).
 */
@Composable
fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    accent: Boolean = false,
) {
    val corner = (size.value * 0.26f).dp.coerceAtMost(16.dp)
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(corner),
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        border = if (accent) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                color = if (accent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.signalType.email.copy(fontSize = (size.value * 0.31f).sp),
            )
        }
    }
}
