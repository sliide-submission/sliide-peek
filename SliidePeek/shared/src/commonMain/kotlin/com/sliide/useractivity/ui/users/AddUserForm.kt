package com.sliide.useractivity.ui.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.AddUserFormState
import com.sliide.useractivity.ui.theme.selectionContainerColor

@Composable
fun AddUserForm(
    state: AddUserFormState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onGenderSelected: (UserGender) -> Unit,
    onStatusSelected: (UserStatus) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Add user",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "New people appear at the top of the feed.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )

        if (state.submitErrorMessage != null) {
            FormErrorBanner(message = state.submitErrorMessage)
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            label = { Text("Name") },
            placeholder = { Text("Full name") },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            label = { Text("Email") },
            placeholder = { Text("name@example.com") },
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it) } },
            singleLine = true,
        )

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                FormSegmentGroup(
                    title = "Gender",
                    options = listOf(UserGender.Female to "Female", UserGender.Male to "Male"),
                    selected = state.gender,
                    enabled = !state.isSubmitting,
                    onSelected = onGenderSelected,
                    modifier = Modifier.weight(1f),
                )
                FormSegmentGroup(
                    title = "Status",
                    options = listOf(UserStatus.Active to "Active", UserStatus.Inactive to "Inactive"),
                    selected = state.status,
                    enabled = !state.isSubmitting,
                    onSelected = onStatusSelected,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            FormSegmentGroup(
                title = "Gender",
                options = listOf(UserGender.Female to "Female", UserGender.Male to "Male"),
                selected = state.gender,
                enabled = !state.isSubmitting,
                onSelected = onGenderSelected,
            )
            FormSegmentGroup(
                title = "Status",
                options = listOf(UserStatus.Active to "Active", UserStatus.Inactive to "Inactive"),
                selected = state.status,
                enabled = !state.isSubmitting,
                onSelected = onStatusSelected,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSubmit,
                enabled = state.isSubmitEnabled,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (state.isSubmitting) "Adding…" else "Add user")
            }
        }
    }
}

@Composable
private fun FormErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun <T> FormSegmentGroup(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    enabled: Boolean,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = enabled) { onSelected(value) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) selectionContainerColor() else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    ),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 9.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
