package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.connectivity.ConnectivityStatus

data class UserFeedState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val users: List<UserFeedItem> = emptyList(),
    val errorMessage: String? = null,
    val offlineMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
    val lastUpdatedLabel: String? = null,
    val canRetry: Boolean = false,
    val nextPage: Int? = null,
    val hasMoreUsers: Boolean = false,
    val connectivityStatus: ConnectivityStatus = ConnectivityStatus.Unknown,
    val isAddUserVisible: Boolean = false,
    val addUserForm: AddUserFormState = AddUserFormState(),
    val highlightedUserId: Long? = null,
    val deleteConfirmation: UserFeedItem? = null,
) {
    val isDeviceOffline: Boolean = connectivityStatus == ConnectivityStatus.Offline
    val isEmpty: Boolean = !isLoading && users.isEmpty() && errorMessage == null && offlineMessage == null
    val isOffline: Boolean = offlineMessage != null
}
