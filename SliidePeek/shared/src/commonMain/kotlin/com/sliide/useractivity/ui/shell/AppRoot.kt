package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import app.cash.sqldelight.db.SqlDriver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.di.appModule
import com.sliide.useractivity.presentation.users.UserFeedEvent
import com.sliide.useractivity.presentation.users.UserFeedViewModel
import com.sliide.useractivity.ui.navigation.AppNavigator
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
                        message = event.message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        userFeedViewModel.undoDelete(event.userId)
                    } else {
                        userFeedViewModel.commitDeletion(event.userId)
                    }
                }

                is UserFeedEvent.ShowDeleteFailed -> scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Long,
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
                    onAddUserClick = userFeedViewModel::openAddUser,
                    onDismissAddUser = userFeedViewModel::dismissAddUser,
                    onAddUserNameChanged = userFeedViewModel::onAddUserNameChanged,
                    onAddUserEmailChanged = userFeedViewModel::onAddUserEmailChanged,
                    onAddUserGenderSelected = userFeedViewModel::onAddUserGenderSelected,
                    onAddUserStatusSelected = userFeedViewModel::onAddUserStatusSelected,
                    onSubmitAddUser = userFeedViewModel::submitAddUser,
                    onUserLongPress = userFeedViewModel::requestDeleteUser,
                )
                AppLayoutClass.Expanded -> ExpandedAppShell(
                    navigator = navigator,
                    availableWidth = maxWidth,
                    userFeedState = userFeedState,
                    onUserFeedRefresh = userFeedViewModel::refresh,
                    onUserFeedRetry = userFeedViewModel::retry,
                    onAddUserClick = userFeedViewModel::openAddUser,
                    onDismissAddUser = userFeedViewModel::dismissAddUser,
                    onAddUserNameChanged = userFeedViewModel::onAddUserNameChanged,
                    onAddUserEmailChanged = userFeedViewModel::onAddUserEmailChanged,
                    onAddUserGenderSelected = userFeedViewModel::onAddUserGenderSelected,
                    onAddUserStatusSelected = userFeedViewModel::onAddUserStatusSelected,
                    onSubmitAddUser = userFeedViewModel::submitAddUser,
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
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
        title = { Text("Delete $userName?") },
        text = { Text("This removes them from your feed. You can undo right after.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
