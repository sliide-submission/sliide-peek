package com.sliide.useractivity.data.local

import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus

internal fun Cached_user_feed.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    gender = gender.toUserGender(),
    status = status.toUserStatus(),
)

internal fun UserGender.cacheValue(): String = when (this) {
    UserGender.Male -> "male"
    UserGender.Female -> "female"
    UserGender.Unknown -> "unknown"
}

internal fun UserStatus.cacheValue(): String = when (this) {
    UserStatus.Active -> "active"
    UserStatus.Inactive -> "inactive"
    UserStatus.Unknown -> "unknown"
}

private fun String.toUserGender(): UserGender = when (lowercase()) {
    "male" -> UserGender.Male
    "female" -> UserGender.Female
    else -> UserGender.Unknown
}

private fun String.toUserStatus(): UserStatus = when (lowercase()) {
    "active" -> UserStatus.Active
    "inactive" -> UserStatus.Inactive
    else -> UserStatus.Unknown
}
