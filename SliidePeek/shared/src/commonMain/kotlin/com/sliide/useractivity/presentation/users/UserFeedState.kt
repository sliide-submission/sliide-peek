package com.sliide.useractivity.presentation.users

data class UserFeedState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val users: List<UserFeedItem> = emptyList(),
    val errorMessage: String? = null,
    val offlineMessage: String? = null,
    val lastUpdatedLabel: String? = null,
    val canRetry: Boolean = false,
) {
    val isEmpty: Boolean = !isLoading && users.isEmpty() && errorMessage == null && offlineMessage == null
    val isOffline: Boolean = offlineMessage != null
}
