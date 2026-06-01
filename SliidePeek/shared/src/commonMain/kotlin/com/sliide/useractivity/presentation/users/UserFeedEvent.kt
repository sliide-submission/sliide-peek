package com.sliide.useractivity.presentation.users

sealed interface UserFeedEvent {
    data class ShowMessage(val message: String) : UserFeedEvent
}
