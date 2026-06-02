package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.presentation.users.UserFeedState
import com.sliide.useractivity.ui.components.AppTopBar
import com.sliide.useractivity.ui.navigation.AppNavigator
import com.sliide.useractivity.ui.users.UserActionPanel
import com.sliide.useractivity.ui.users.UserFeedScreen

@Composable
fun ExpandedAppShell(
    navigator: AppNavigator,
    availableWidth: Dp,
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
    onDeleteUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(modifier = modifier) {
        Row(Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .width(masterPaneWidthFor(availableWidth))
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(Modifier.fillMaxSize()) {
                    AppTopBar(
                        title = "Users",
                        subtitle = userFeedState.users.size.toString(),
                        onAdd = onAddUserClick,
                        onRefresh = onUserFeedRefresh,
                        refreshing = userFeedState.isRefreshing,
                    )
                    UserFeedScreen(
                        state = userFeedState,
                        selectedUserId = navigator.selectedUserId,
                        onUserClick = onUserClick,
                        onRefresh = onUserFeedRefresh,
                        onRetry = onUserFeedRetry,
                        onAddUserClick = onAddUserClick,
                        onLoadMoreUsers = onLoadMoreUsers,
                        showFab = false,
                        compactRows = true,
                        enableLongPress = false,
                        onUserLongPress = onUserLongPress,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                val selectedUser = userFeedState.users.firstOrNull { it.id == navigator.selectedUserId }
                UserActionPanel(
                    user = selectedUser,
                    addUserForm = userFeedState.addUserForm,
                    isAddUserVisible = userFeedState.isAddUserVisible,
                    onAddUserClick = onAddUserClick,
                    onDismissAddUser = onDismissAddUser,
                    onAddUserNameChanged = onAddUserNameChanged,
                    onAddUserEmailChanged = onAddUserEmailChanged,
                    onAddUserGenderSelected = onAddUserGenderSelected,
                    onAddUserStatusSelected = onAddUserStatusSelected,
                    onSubmitAddUser = onSubmitAddUser,
                    onDeleteUserClick = onDeleteUserClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
