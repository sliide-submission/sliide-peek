package com.sliide.useractivity.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.UserFeedItem
import com.sliide.useractivity.ui.components.ContentState
import com.sliide.useractivity.ui.components.ContentStateContainer
import com.sliide.useractivity.ui.components.InitialsAvatar
import com.sliide.useractivity.ui.components.SectionHeader
import com.sliide.useractivity.ui.components.StatusChip
import com.sliide.useractivity.ui.shell.appBodyPaddingValues
import com.sliide.useractivity.ui.theme.signal
import com.sliide.useractivity.ui.theme.signalType

@Composable
fun UserDetailScreen(
    user: UserFeedItem?,
    modifier: Modifier = Modifier,
) {
    if (user == null) {
        ContentStateContainer(
            modifier = modifier,
            state = ContentState.Empty,
            emptyTitle = "User not found",
            emptyBody = "This user is no longer in the current feed. Go back and select another user.",
        ) {}
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            InitialsAvatar(initials = userInitials(user.name), size = 66.dp, accent = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = user.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = user.email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.signalType.email,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatusChip(label = user.status.label, active = user.status == UserStatus.Active)
                    StatusChip(label = user.gender.label, neutral = true)
                }
            }
        }

        SectionHeader(title = "Details")
        DetailGrid(user)
    }
}

@Composable
private fun DetailGrid(user: UserFeedItem) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
            DetailItem("Last active", user.relativeTimestamp, Modifier.weight(1f))
            DetailItem("User ID", "#${user.id}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
            DetailItem("Status", user.status.label, Modifier.weight(1f))
            DetailItem("Gender", user.gender.label, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.signal.ink3,
            style = MaterialTheme.signalType.eyebrow,
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
