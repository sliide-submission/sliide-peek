package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.RelativeTimeFormatter

data class UserFeedItem(
    val id: Long,
    val name: String,
    val email: String,
    val gender: UserGender,
    val status: UserStatus,
    val fetchedAtMillis: Long,
    val relativeTimestamp: String,
)

/** Single source of truth for projecting a domain [User] into a feed item with a shared relative timestamp. */
internal fun User.toFeedItem(
    fetchedAtMillis: Long,
    relativeTimeFormatter: RelativeTimeFormatter,
    clock: AppClock,
): UserFeedItem = UserFeedItem(
    id = id,
    name = name,
    email = email,
    gender = gender,
    status = status,
    fetchedAtMillis = fetchedAtMillis,
    relativeTimestamp = relativeTimeFormatter.format(
        thenMillis = fetchedAtMillis,
        nowMillis = clock.nowMillis(),
    ),
)
