package com.sliide.useractivity.presentation.users

sealed interface UserFeedEvent {
    data class ShowMessage(val message: String) : UserFeedEvent
    data class ShowUndoDelete(val userId: Long, val message: String) : UserFeedEvent
    data class ShowDeleteFailed(val userId: Long, val message: String) : UserFeedEvent
}
