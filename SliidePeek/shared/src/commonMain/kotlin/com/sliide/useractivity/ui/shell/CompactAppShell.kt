package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.UserFeedState
import com.sliide.useractivity.ui.components.AppTopBar
import com.sliide.useractivity.ui.navigation.AppNavigator
import com.sliide.useractivity.ui.navigation.AppRoute
import com.sliide.useractivity.ui.navigation.title
import com.sliide.useractivity.ui.theme.Radius
import com.sliide.useractivity.ui.users.AddUserForm
import com.sliide.useractivity.ui.users.UserDetailScreen
import com.sliide.useractivity.ui.users.UserFeedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactAppShell(
    navigator: AppNavigator,
    userFeedState: UserFeedState,
    onUserFeedRefresh: () -> Unit,
    onUserFeedRetry: () -> Unit,
    onLoadMoreUsers: () -> Unit,
    onAddUserClick: () -> Unit,
    onDismissAddUser: () -> Unit,
    onAddUserNameChanged: (String) -> Unit,
    onAddUserEmailChanged: (String) -> Unit,
    onAddUserGenderSelected: (UserGender) -> Unit,
    onAddUserStatusSelected: (UserStatus) -> Unit,
    onSubmitAddUser: () -> Unit,
    onUserClick: (Long) -> Unit,
    onUserLongPress: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = navigator.currentRoute
    PlatformBackHandler(enabled = navigator.canGoBack) {
        navigator.goBack()
    }
    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = route.title,
                subtitle = if (route == AppRoute.Users) userFeedState.users.size.toString() else null,
                showBack = navigator.canGoBack,
                onBack = if (navigator.canGoBack) ({ navigator.goBack() }) else null,
                onRefresh = if (route == AppRoute.Users) onUserFeedRefresh else null,
                refreshing = route == AppRoute.Users && userFeedState.isRefreshing,
            )
        },
    ) {
        when (route) {
            AppRoute.Users -> UserFeedScreen(
                state = userFeedState,
                // Compact/mobile pushes a detail screen, so row selection highlighting is not useful here.
                selectedUserId = null,
                onUserClick = onUserClick,
                onRefresh = onUserFeedRefresh,
                onRetry = onUserFeedRetry,
                onAddUserClick = onAddUserClick,
                onLoadMoreUsers = onLoadMoreUsers,
                onUserLongPress = onUserLongPress,
                modifier = Modifier.fillMaxSize(),
            )

            is AppRoute.UserDetail -> UserDetailScreen(
                user = userFeedState.users.firstOrNull { it.id == route.userId },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (userFeedState.isAddUserVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissAddUser,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl),
        ) {
            AddUserForm(
                state = userFeedState.addUserForm,
                onNameChanged = onAddUserNameChanged,
                onEmailChanged = onAddUserEmailChanged,
                onGenderSelected = onAddUserGenderSelected,
                onStatusSelected = onAddUserStatusSelected,
                onSubmit = onSubmitAddUser,
                onCancel = onDismissAddUser,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }
}
