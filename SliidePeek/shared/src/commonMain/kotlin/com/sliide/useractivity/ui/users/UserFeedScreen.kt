package com.sliide.useractivity.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.presentation.users.UserFeedState
import com.sliide.useractivity.ui.components.BannerVariant
import com.sliide.useractivity.ui.components.ContentState
import com.sliide.useractivity.ui.components.ContentStateContainer
import com.sliide.useractivity.ui.components.SignalBanner
import com.sliide.useractivity.ui.components.SignalIcons
import com.sliide.useractivity.ui.components.StateMessage
import com.sliide.useractivity.ui.components.StateTone
import com.sliide.useractivity.ui.shell.appBodyPaddingValues
import com.sliide.useractivity.ui.theme.Radius

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
            onRetry = onRetry,
            onAddUserClick = onAddUserClick,
            compactRows = compactRows,
            onUserLongPress = onUserLongPress,
            fabClearance = showFab && state.shouldShowFab,
            modifier = Modifier.fillMaxSize(),
        )
        if (showFab && state.shouldShowFab) {
            FloatingActionButton(
                onClick = onAddUserClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(Radius.xxl),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(SignalIcons.Plus, contentDescription = "Add user")
            }
        }
    }
}

@Composable
private fun UserFeedContent(
    state: UserFeedState,
    selectedUserId: Long?,
    onUserClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onAddUserClick: () -> Unit,
    compactRows: Boolean,
    onUserLongPress: (Long) -> Unit,
    fabClearance: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            state.isLoading -> ContentStateContainer(
                state = ContentState.Loading,
                modifier = Modifier.fillMaxSize(),
                loadingRows = 6,
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
                val listState = rememberLazyListState()
                LaunchedEffect(state.users.firstOrNull()?.id, state.lastUpdatedLabel) {
                    if (state.users.isNotEmpty()) {
                        listState.animateScrollToItem(0)
                    }
                }

                if (state.offlineMessage != null) {
                    SignalBanner(
                        message = "Offline — showing cached users",
                        variant = BannerVariant.Offline,
                        trailing = state.lastUpdatedLabel?.let { "updated $it" },
                    )
                } else if (state.isRefreshing) {
                    SignalBanner(message = "Refreshing…", variant = BannerVariant.Refreshing)
                } else if (state.errorMessage != null) {
                    SignalBanner(message = state.errorMessage, variant = BannerVariant.Error)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = if (fabClearance) 84.dp else 8.dp),
                ) {
                    items(state.users, key = { it.id }) { user ->
                        UserFeedRow(
                            user = user,
                            selected = user.id == selectedUserId,
                            highlighted = user.id == state.highlightedUserId,
                            onClick = { onUserClick(user.id) },
                            onLongClick = { onUserLongPress(user.id) },
                            compact = compactRows,
                            modifier = Modifier,
                        )
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
        body = "Add the first person to get started — they’ll appear right at the top of the feed.",
        icon = SignalIcons.Users,
        tone = StateTone.Accent,
        actionLabel = "Add user",
        actionIcon = SignalIcons.Plus,
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
        body = message.ifBlank { "Something went wrong on our side. Nothing was lost — give it another try." },
        icon = SignalIcons.AlertTriangle,
        tone = StateTone.Error,
        code = "ERR · /v2/users",
        actionLabel = "Retry",
        actionIcon = SignalIcons.Refresh,
        onAction = onRetry,
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
        body = message.ifBlank { "No connection, and nothing’s been cached yet. Reconnect to load your users." },
        icon = SignalIcons.WifiOff,
        tone = StateTone.Neutral,
        code = "NO_NETWORK · cache empty",
        actionLabel = "Try again",
        actionIcon = SignalIcons.Refresh,
        ghostAction = true,
        onAction = onRetry,
    )
}

private val UserFeedState.shouldShowFab: Boolean
    get() = isLoading || isEmpty || users.isNotEmpty()
