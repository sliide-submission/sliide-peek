package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.UserDTO
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus

internal fun UserDTO.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    gender = gender.toUserGender(),
    status = status.toUserStatus(),
)

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
