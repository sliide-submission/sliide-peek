package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sliide.useractivity.presentation.users.UserFeedState
import com.sliide.useractivity.ui.components.AppTopBar
import com.sliide.useractivity.ui.navigation.AppNavigator
import com.sliide.useractivity.ui.navigation.AppRoute
import com.sliide.useractivity.ui.navigation.title
import com.sliide.useractivity.ui.users.UserFeedScreen

@Composable
fun CompactAppShell(
    navigator: AppNavigator,
    userFeedState: UserFeedState,
    onUserFeedRefresh: () -> Unit,
    onUserFeedRetry: () -> Unit,
    onAddUserClick: () -> Unit,
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
            )
        },
    ) {
        when (route) {
            AppRoute.Users -> UserFeedScreen(
                state = userFeedState,
                selectedUserId = navigator.selectedUserId,
                onUserClick = navigator::selectUser,
                onRefresh = onUserFeedRefresh,
                onRetry = onUserFeedRetry,
                onAddUserClick = onAddUserClick,
                modifier = Modifier.fillMaxSize(),
            )

            is AppRoute.UserDetail -> UserDetailPlaceholder(
                userId = route.userId,
                onPostsClick = { userId -> navigator.navigate(AppRoute.UserPosts(userId)) },
                onTodosClick = { userId -> navigator.navigate(AppRoute.UserTodos(userId)) },
                onPostClick = { userId, postId -> navigator.navigate(AppRoute.PostDetail(userId = userId, postId = postId)) },
                modifier = Modifier.fillMaxSize(),
            )

            is AppRoute.UserPosts -> PostsListPlaceholder(
                userId = route.userId,
                onPostClick = { userId, postId -> navigator.navigate(AppRoute.PostDetail(userId = userId, postId = postId)) },
                modifier = Modifier.fillMaxSize(),
            )

            is AppRoute.UserTodos -> TodosListPlaceholder(modifier = Modifier.fillMaxSize())

            is AppRoute.PostDetail -> PostDetailPlaceholder(
                postId = route.postId,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
