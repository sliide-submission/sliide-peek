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
import com.sliide.useractivity.ui.components.AppTopBar
import com.sliide.useractivity.ui.navigation.AppNavigator
import com.sliide.useractivity.ui.navigation.AppRoute

@Composable
fun ExpandedAppShell(
    navigator: AppNavigator,
    availableWidth: Dp,
    modifier: Modifier = Modifier,
) {
    PlatformBackHandler(enabled = navigator.canGoBack || navigator.selectedPostId != null) {
        val selectedUserId = navigator.selectedUserId
        if (navigator.selectedPostId != null && selectedUserId != null) {
            navigator.selectUser(selectedUserId)
        } else {
            navigator.goBack()
        }
    }

    AppScaffold(modifier = modifier) {
        Row(Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .width(masterPaneWidthFor(availableWidth))
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.fillMaxSize()) {
                    AppTopBar(
                        title = "Users",
                        subtitle = "24",
                        onRefresh = {},
                    )
                    UserListPlaceholder(
                        selectedUserId = navigator.selectedUserId,
                        onUserClick = navigator::selectUser,
                        compactRows = availableWidth < 840.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                val selectedUserId = navigator.selectedUserId
                val selectedPostId = navigator.selectedPostId
                val route = navigator.currentRoute
                when {
                    selectedUserId == null -> NoSelectionPlaceholder(Modifier.fillMaxSize())
                    route is AppRoute.UserPosts -> Column(Modifier.fillMaxSize()) {
                        AppTopBar(title = "Posts", showBack = true, onBack = { navigator.goBack() })
                        PostsListPlaceholder(
                            userId = route.userId,
                            onPostClick = { _, postId -> navigator.selectPost(postId) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    route is AppRoute.UserTodos -> Column(Modifier.fillMaxSize()) {
                        AppTopBar(title = "Todos", showBack = true, onBack = { navigator.goBack() })
                        TodosListPlaceholder(modifier = Modifier.fillMaxSize())
                    }
                    selectedPostId != null -> Column(Modifier.fillMaxSize()) {
                        AppTopBar(
                            title = "Post",
                            showBack = true,
                            onBack = { navigator.selectUser(selectedUserId) },
                        )
                        PostDetailPlaceholder(postId = selectedPostId, modifier = Modifier.fillMaxSize())
                    }
                    else -> UserDetailPlaceholder(
                        userId = selectedUserId,
                        onPostsClick = { navigator.navigate(AppRoute.UserPosts(it)) },
                        onTodosClick = { navigator.navigate(AppRoute.UserTodos(it)) },
                        onPostClick = { _, postId -> navigator.selectPost(postId) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
