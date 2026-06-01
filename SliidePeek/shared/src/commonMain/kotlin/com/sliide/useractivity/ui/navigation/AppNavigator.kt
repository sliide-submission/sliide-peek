package com.sliide.useractivity.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class AppNavigator(initialRoute: AppRoute = AppRoute.Users) {
    private val routeStack = mutableStateListOf(initialRoute)
    private val selectedUserIdState = mutableStateOf<Long?>(null)
    private val selectedPostIdState = mutableStateOf<Long?>(null)

    val currentRoute: AppRoute
        get() = routeStack.last()

    val canGoBack: Boolean
        get() = routeStack.size > 1

    val selectedUserId: Long?
        get() = selectedUserIdState.value

    val selectedPostId: Long?
        get() = selectedPostIdState.value

    fun navigate(route: AppRoute) {
        routeStack += route
        updateSelection(route)
    }

    fun goBack(): Boolean {
        if (!canGoBack) return false
        routeStack.removeAt(routeStack.lastIndex)
        updateSelection(currentRoute)
        return true
    }

    fun resetToUsers() {
        routeStack.clear()
        routeStack += AppRoute.Users
        selectedUserIdState.value = null
        selectedPostIdState.value = null
    }

    fun selectUser(userId: Long) {
        selectedUserIdState.value = userId
        selectedPostIdState.value = null
    }

    fun selectPost(postId: Long) {
        selectedPostIdState.value = postId
    }

    private fun updateSelection(route: AppRoute) {
        when (route) {
            AppRoute.Users -> Unit
            is AppRoute.UserDetail -> selectUser(route.userId)
            is AppRoute.UserPosts -> selectUser(route.userId)
            is AppRoute.UserTodos -> selectUser(route.userId)
            is AppRoute.PostDetail -> {
                route.userId?.let(::selectUser)
                selectPost(route.postId)
            }
        }
    }
}
