package com.sliide.useractivity.ui.users

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.presentation.users.UserFeedState
import com.sliide.useractivity.ui.components.ContentState
import com.sliide.useractivity.ui.components.ContentStateContainer
import com.sliide.useractivity.ui.components.StateMessage
import com.sliide.useractivity.ui.shell.appBodyPaddingValues

@Composable
fun UserFeedScreen(
    state: UserFeedState,
    selectedUserId: Long?,
    onUserClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAddUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFab: Boolean = true,
    compactRows: Boolean = false,
    onUserLongPress: (Long) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        UserFeedContent(
            state = state,
            selectedUserId = selectedUserId,
            onUserClick = onUserClick,
            onRefresh = onRefresh,
            onRetry = onRetry,
            onAddUserClick = onAddUserClick,
            compactRows = compactRows,
            onUserLongPress = onUserLongPress,
            modifier = Modifier.fillMaxSize(),
        )
        if (showFab && state.shouldShowFab) {
            FloatingActionButton(
                onClick = onAddUserClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp),
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun UserFeedContent(
    state: UserFeedState,
    selectedUserId: Long?,
    onUserClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAddUserClick: () -> Unit,
    compactRows: Boolean,
    onUserLongPress: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            state.isLoading -> ContentStateContainer(
                state = ContentState.Loading,
                modifier = Modifier.fillMaxSize(),
                loadingRows = 8,
            ) {}

            state.offlineMessage != null && state.users.isEmpty() -> NoInternetState(
                message = state.offlineMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )

            state.errorMessage != null && state.users.isEmpty() -> ApiErrorState(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )

            state.isEmpty -> EmptyFeedState(
                onAddUserClick = onAddUserClick,
                modifier = Modifier.fillMaxSize(),
            )

            else -> {
                if (state.offlineMessage != null) {
                    FeedStatusBanner(
                        message = "Offline — showing cached users",
                        trailing = state.lastUpdatedLabel?.let { "updated $it" },
                    )
                } else if (state.isRefreshing) {
                    FeedStatusBanner(message = "Refreshing…")
                } else if (state.errorMessage != null) {
                    FeedStatusBanner(message = state.errorMessage, isError = true)
                }
                Column(
                    modifier = Modifier.animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.users.forEach { user ->
                        key(user.id) {
                            val transitionState = remember {
                                MutableTransitionState(false).apply { targetState = true }
                            }
                            AnimatedVisibility(
                                visibleState = transitionState,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                UserFeedRow(
                                    user = user,
                                    selected = user.id == selectedUserId,
                                    onClick = { onUserClick(user.id) },
                                    onLongClick = { onUserLongPress(user.id) },
                                    compact = compactRows,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(
    onAddUserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateMessage(
        modifier = modifier,
        title = "No users yet",
        body = "Add the first person to get started. They’ll appear at the top of the feed.",
        symbol = "+",
        actionLabel = "Add user",
        onAction = onAddUserClick,
    )
}

@Composable
private fun ApiErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateMessage(
        modifier = modifier,
        title = "Couldn’t load users",
        body = message.ifBlank { "Something went wrong on our side. Nothing was lost — try again." },
        symbol = "⚠",
        actionLabel = "Retry",
        onAction = onRetry,
        isError = true,
    )
}

@Composable
private fun NoInternetState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateMessage(
        modifier = modifier,
        title = "You’re offline",
        body = message.ifBlank { "No connection and nothing cached yet. Reconnect to load your users." },
        symbol = "⚡",
        actionLabel = "Try again",
        onAction = onRetry,
    )
}

@Composable
private fun FeedStatusBanner(
    message: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    isError: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "●",
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

private val UserFeedState.shouldShowFab: Boolean
    get() = isLoading || isEmpty || users.isNotEmpty()
