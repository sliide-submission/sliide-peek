package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus

data class UserFeedItem(
    val id: Long,
    val name: String,
    val email: String,
    val gender: UserGender,
    val status: UserStatus,
    val fetchedAtMillis: Long,
    val relativeTimestamp: String,
)
