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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.di.appModule
import com.sliide.useractivity.presentation.users.UserFeedEvent
import com.sliide.useractivity.presentation.users.UserFeedViewModel
import com.sliide.useractivity.ui.navigation.AppNavigator
import io.ktor.client.HttpClient
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
            when (event) {
                is UserFeedEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
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
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
