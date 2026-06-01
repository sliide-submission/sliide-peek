package com.sliide.useractivity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.theme.selectionContainerColor

enum class UserFilter(val label: String) {
    All("All"),
    Active("Active"),
    Inactive("Inactive"),
}

@Composable
fun SegmentedFilter(
    selected: UserFilter,
    onSelected: (UserFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        UserFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 3.dp)
                    .clickable { onSelected(filter) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) selectionContainerColor() else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = filter.label,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
