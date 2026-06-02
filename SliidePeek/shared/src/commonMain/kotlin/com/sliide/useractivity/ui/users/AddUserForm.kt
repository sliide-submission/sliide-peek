package com.sliide.useractivity.ui.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.AddUserFormState
import com.sliide.useractivity.ui.components.BannerVariant
import com.sliide.useractivity.ui.components.SignalBanner
import com.sliide.useractivity.ui.components.SignalIcons
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

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
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Add user",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "New people appear at the top of the feed.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )

        if (state.submitErrorMessage != null) {
            SignalBanner(message = state.submitErrorMessage, variant = BannerVariant.Error)
        }

        val nameValid = state.name.isNotBlank() && state.nameError == null
        FormTextField(
            label = "Name",
            value = state.name,
            onValueChange = onNameChanged,
            enabled = !state.isSubmitting,
            placeholder = "Full name",
            error = state.visibleNameError,
            valid = nameValid,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Next) }),
        )
        val emailValid = state.email.isNotBlank() && state.emailError == null
        FormTextField(
            label = "Email",
            value = state.email,
            onValueChange = onEmailChanged,
            enabled = !state.isSubmitting,
            placeholder = "name@example.com",
            error = state.visibleEmailError,
            valid = emailValid,
            mono = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        )

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

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !state.isSubmitting,
                // iPad: secondary stays compact so the primary CTA dominates. Phone: equal split.
                modifier = if (expanded) Modifier else Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSubmit,
                enabled = state.isSubmitEnabled,
                modifier = Modifier.weight(1f),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Adding…")
                } else {
                    Icon(SignalIcons.Plus, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Add user")
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    error: String?,
    valid: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
) {
    val green = MaterialTheme.signal.green
    var fieldValue by remember { mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length))) }

    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { next ->
            fieldValue = next
            if (next.text != value) onValueChange(next.text)
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(Radius.lg),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = if (mono) MaterialTheme.signalType.email.copy(fontSize = 13.5.sp) else MaterialTheme.typography.bodyMedium,
        trailingIcon = {
            when {
                error != null -> Icon(SignalIcons.AlertCircle, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                valid -> Icon(SignalIcons.Check, contentDescription = null, tint = green, modifier = Modifier.size(18.dp))
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valid) green else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (valid) green else MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
        ),
    )
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
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
        )
        Surface(
            shape = RoundedCornerShape(Radius.lg),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (value, label) ->
                    val isSelected = value == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(enabled = enabled) { onSelected(value) }
                            .then(
                                if (isSelected) Modifier.background(MaterialTheme.signal.accentSoft) else Modifier,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    if (index < options.lastIndex) {
                        Box(
                            Modifier
                                .width(1.5.dp)
                                .height(44.dp)
                                .background(MaterialTheme.colorScheme.outline),
                        )
                    }
                }
            }
        }
    }
}
