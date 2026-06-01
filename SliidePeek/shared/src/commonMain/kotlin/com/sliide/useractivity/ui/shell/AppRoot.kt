package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.BoxWithConstraints
import app.cash.sqldelight.db.SqlDriver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.di.appModule
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

    LaunchedEffect(userFeedViewModel) {
        userFeedViewModel.load()
    }
    DisposableEffect(koinApplication) {
        onDispose {
            koin.get<HttpClient>().close()
            koin.get<SqlDriver>().close()
            koinApplication.close()
        }
    }

    BoxWithConstraints {
        when (appLayoutClassForWidth(maxWidth)) {
            AppLayoutClass.Compact -> CompactAppShell(
                navigator = navigator,
                userFeedState = userFeedState,
                onUserFeedRefresh = userFeedViewModel::refresh,
                onUserFeedRetry = userFeedViewModel::retry,
                onAddUserClick = {},
            )
            AppLayoutClass.Expanded -> ExpandedAppShell(
                navigator = navigator,
                availableWidth = maxWidth,
                userFeedState = userFeedState,
                onUserFeedRefresh = userFeedViewModel::refresh,
                onUserFeedRetry = userFeedViewModel::retry,
                onAddUserClick = {},
            )
        }
    }
}
