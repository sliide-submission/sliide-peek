package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.sliide.useractivity.data.AppDataContainer
import com.sliide.useractivity.domain.time.SystemAppClock
import com.sliide.useractivity.presentation.users.GetSmartUserFeedUseCase
import com.sliide.useractivity.presentation.users.UserFeedViewModel
import com.sliide.useractivity.ui.navigation.AppNavigator

@Composable
fun AppRoot() {
    val navigator = remember { AppNavigator() }
    val scope = rememberCoroutineScope()
    val dataContainer = remember { AppDataContainer() }
    val userFeedViewModel = remember {
        UserFeedViewModel(
            loadUserFeed = GetSmartUserFeedUseCase(
                userRepository = dataContainer.userRepository,
                clock = SystemAppClock(),
            ),
            scope = scope,
        )
    }
    val userFeedState by userFeedViewModel.state.collectAsState()

    LaunchedEffect(userFeedViewModel) {
        userFeedViewModel.load()
    }
    DisposableEffect(dataContainer) {
        onDispose { dataContainer.close() }
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
