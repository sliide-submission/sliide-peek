package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import app.cash.sqldelight.db.SqlDriver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.components.SignalIcons
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.di.appModule
import com.sliide.useractivity.presentation.users.UserFeedEvent
import com.sliide.useractivity.presentation.users.UserFeedViewModel
import com.sliide.useractivity.ui.navigation.AppNavigator
import com.sliide.useractivity.ui.navigation.AppRoute
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication

@Composable
fun AppRoot(databaseDriverFactory: DatabaseDriverFactory) {
    val navigator = remember { AppNavigator() }
    val scope = rememberCoroutineScope()
    val koinApplication = remember(databaseDriverFactory) {
        koinApplication {
            modules(appModule(databaseDriverFactory))
        }
    }
    val koin = koinApplication.koin
    val userFeedViewModel = remember(scope, koinApplication) {
        koin.get<UserFeedViewModel> { parametersOf(scope) }
    }
    val userFeedState by userFeedViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userFeedViewModel) {
        userFeedViewModel.load()
    }
    LaunchedEffect(userFeedViewModel, snackbarHostState) {
        userFeedViewModel.events.collect { event ->
            // Launch each snackbar so a suspending Undo window never blocks later events.
            when (event) {
                is UserFeedEvent.ShowMessage -> scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }

                is UserFeedEvent.ShowUndoDelete -> scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        SignalSnackbarVisuals(message = event.message, actionLabel = "Undo"),
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        userFeedViewModel.undoDelete(event.userId)
                    } else {
                        userFeedViewModel.commitDeletion(event.userId)
                    }
                }

                is UserFeedEvent.ShowDeleteFailed -> scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        SignalSnackbarVisuals(message = event.message, actionLabel = "Retry", isError = true),
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        userFeedViewModel.retryDelete(event.userId)
                    }
                }
            }
        }
    }
    DisposableEffect(koinApplication) {
        onDispose {
            koin.get<HttpClient>().close()
            koin.get<SqlDriver>().close()
            koinApplication.close()
        }
    }

    Box(Modifier.fillMaxSize()) {
        BoxWithConstraints {
            when (appLayoutClassForWidth(maxWidth)) {
                AppLayoutClass.Compact -> CompactAppShell(
                    navigator = navigator,
                    userFeedState = userFeedState,
                    onUserFeedRefresh = userFeedViewModel::refresh,
                    onUserFeedRetry = userFeedViewModel::retry,
                    onLoadMoreUsers = userFeedViewModel::loadMoreUsers,
                    onAddUserClick = userFeedViewModel::openAddUser,
                    onDismissAddUser = userFeedViewModel::dismissAddUser,
                    onAddUserNameChanged = userFeedViewModel::onAddUserNameChanged,
                    onAddUserEmailChanged = userFeedViewModel::onAddUserEmailChanged,
                    onAddUserGenderSelected = userFeedViewModel::onAddUserGenderSelected,
                    onAddUserStatusSelected = userFeedViewModel::onAddUserStatusSelected,
                    onSubmitAddUser = userFeedViewModel::submitAddUser,
                    onUserClick = { userId ->
                        userFeedViewModel.clearHighlight()
                        navigator.navigate(AppRoute.UserDetail(userId))
                    },
                    onUserLongPress = userFeedViewModel::requestDeleteUser,
                )
                AppLayoutClass.Expanded -> ExpandedAppShell(
                    navigator = navigator,
                    availableWidth = maxWidth,
                    userFeedState = userFeedState,
                    onUserFeedRefresh = userFeedViewModel::refresh,
                    onUserFeedRetry = userFeedViewModel::retry,
                    onLoadMoreUsers = userFeedViewModel::loadMoreUsers,
                    onAddUserClick = userFeedViewModel::openAddUser,
                    onDismissAddUser = userFeedViewModel::dismissAddUser,
                    onAddUserNameChanged = userFeedViewModel::onAddUserNameChanged,
                    onAddUserEmailChanged = userFeedViewModel::onAddUserEmailChanged,
                    onAddUserGenderSelected = userFeedViewModel::onAddUserGenderSelected,
                    onAddUserStatusSelected = userFeedViewModel::onAddUserStatusSelected,
                    onSubmitAddUser = userFeedViewModel::submitAddUser,
                    onUserClick = { userId ->
                        userFeedViewModel.clearHighlight()
                        navigator.selectUser(userId)
                    },
                    onUserLongPress = userFeedViewModel::requestDeleteUser,
                    onDeleteUserClick = userFeedViewModel::requestDeleteUser,
                )
            }
        }

        userFeedState.deleteConfirmation?.let { user ->
            DeleteUserConfirmationDialog(
                userName = user.name,
                onConfirm = userFeedViewModel::confirmDeleteUser,
                onDismiss = userFeedViewModel::cancelDeleteUser,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
        ) { data ->
            SignalSnackbar(data)
        }
    }
}

/** Snackbar visuals carrying an explicit error flag so styling never depends on matching label text. */
private class SignalSnackbarVisuals(
    override val message: String,
    override val actionLabel: String?,
    val isError: Boolean = false,
) : SnackbarVisuals {
    override val duration: SnackbarDuration = SnackbarDuration.Long
    override val withDismissAction: Boolean = false
}

@Composable
private fun SignalSnackbar(data: SnackbarData) {
    val isError = (data.visuals as? SignalSnackbarVisuals)?.isError == true
    val container = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inverseSurface
    val content = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.inverseOnSurface
    val action = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = container,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = data.visuals.message,
                modifier = Modifier.weight(1f),
                color = content,
                style = MaterialTheme.typography.bodySmall,
            )
            data.visuals.actionLabel?.let { label ->
                TextButton(onClick = data::performAction) {
                    Text(
                        text = label.uppercase(),
                        color = action,
                        textDecoration = if (isError) TextDecoration.Underline else null,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteUserConfirmationDialog(
    userName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Radius.dialog),
        icon = {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(Radius.lg),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        SignalIcons.Trash,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        },
        title = { Text("Delete $userName?") },
        text = { Text("This removes them from your feed. You can undo right after.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
