package com.sliide.useractivity.ui.navigation

sealed interface AppRoute {
    data object Users : AppRoute
    data class UserDetail(val userId: Long) : AppRoute
}

val AppRoute.title: String
    get() = when (this) {
        AppRoute.Users -> "Users"
        is AppRoute.UserDetail -> "Profile"
    }
