package com.sliide.useractivity.ui.navigation

sealed interface AppRoute {
    data object Users : AppRoute
    data class UserDetail(val userId: Long) : AppRoute
    data class UserPosts(val userId: Long) : AppRoute
    data class UserTodos(val userId: Long) : AppRoute
    data class PostDetail(val postId: Long, val userId: Long? = null) : AppRoute
}

val AppRoute.title: String
    get() = when (this) {
        AppRoute.Users -> "Users"
        is AppRoute.UserDetail -> "Profile"
        is AppRoute.UserPosts -> "Posts"
        is AppRoute.UserTodos -> "Todos"
        is AppRoute.PostDetail -> "Post"
    }
