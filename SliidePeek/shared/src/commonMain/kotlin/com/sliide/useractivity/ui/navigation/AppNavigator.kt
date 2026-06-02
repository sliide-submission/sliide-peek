package com.sliide.useractivity.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class AppNavigator(initialRoute: AppRoute = AppRoute.Users) {
    private val routeStack = mutableStateListOf(initialRoute)
    private val selectedUserIdState = mutableStateOf<Long?>(null)

    val currentRoute: AppRoute
        get() = routeStack.last()

    val canGoBack: Boolean
        get() = routeStack.size > 1

    val selectedUserId: Long?
        get() = selectedUserIdState.value

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
    }

    fun selectUser(userId: Long) {
        selectedUserIdState.value = userId
    }

    private fun updateSelection(route: AppRoute) {
        when (route) {
            AppRoute.Users -> Unit
            is AppRoute.UserDetail -> selectUser(route.userId)
        }
    }
}
