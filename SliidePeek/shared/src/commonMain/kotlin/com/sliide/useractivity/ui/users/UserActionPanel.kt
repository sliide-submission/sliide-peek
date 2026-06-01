package com.sliide.useractivity.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.presentation.users.AddUserFormState
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.UserFeedItem
import com.sliide.useractivity.ui.components.ContentState
import com.sliide.useractivity.ui.components.ContentStateContainer
import com.sliide.useractivity.ui.components.InitialsAvatar
import com.sliide.useractivity.ui.components.SectionHeader
import com.sliide.useractivity.ui.components.StatusChip
import com.sliide.useractivity.ui.shell.appBodyPaddingValues

@Composable
fun UserActionPanel(
    user: UserFeedItem?,
    addUserForm: AddUserFormState,
    isAddUserVisible: Boolean,
    onAddUserClick: () -> Unit,
    onDismissAddUser: () -> Unit,
    onAddUserNameChanged: (String) -> Unit,
    onAddUserEmailChanged: (String) -> Unit,
    onAddUserGenderSelected: (UserGender) -> Unit,
    onAddUserStatusSelected: (UserStatus) -> Unit,
    onSubmitAddUser: () -> Unit,
    onDeleteUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isAddUserVisible) {
        AddUserForm(
            state = addUserForm,
            onNameChanged = onAddUserNameChanged,
            onEmailChanged = onAddUserEmailChanged,
            onGenderSelected = onAddUserGenderSelected,
            onStatusSelected = onAddUserStatusSelected,
            onSubmit = onSubmitAddUser,
            onCancel = onDismissAddUser,
            expanded = true,
            modifier = modifier
                .fillMaxSize()
                .padding(appBodyPaddingValues()),
        )
        return
    }

    if (user == null) {
        ContentStateContainer(
            modifier = modifier,
            state = ContentState.Empty,
            emptyTitle = "Select a user",
            emptyBody = "Choose a user from the feed to manage them, or add someone new.",
            emptyActionLabel = "Add user",
            onEmptyAction = onAddUserClick,
        ) {}
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InitialsAvatar(initials = user.name.initials(), size = 54.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = user.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = user.email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatusChip(label = user.status.label, active = user.status == UserStatus.Active)
                    StatusChip(label = user.gender.label)
                }
            }
        }

        SectionHeader(title = "Details")
        DetailGrid(user)

        SectionHeader(title = "Actions")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                enabled = false,
                onClick = { onDeleteUserClick(user.id) },
            ) {
                Text("Delete user")
            }
            OutlinedButton(onClick = onAddUserClick) {
                Text("Add user")
            }
        }
        Text(
            text = "Add and delete actions are visible seams for the next roadmap items.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DetailGrid(user: UserFeedItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailRow("Last active", user.relativeTimestamp)
        DetailRow("User ID", "#${user.id}")
        DetailRow("Status", user.status.label)
        DetailRow("Gender", user.gender.label)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private val UserStatus.label: String
    get() = when (this) {
        UserStatus.Active -> "Active"
        UserStatus.Inactive -> "Inactive"
        UserStatus.Unknown -> "Unknown"
    }

private val UserGender.label: String
    get() = when (this) {
        UserGender.Male -> "Male"
        UserGender.Female -> "Female"
        UserGender.Unknown -> "Unknown"
    }

private fun String.initials(): String = trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "?" }
