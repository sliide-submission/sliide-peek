package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.CreateUserRequestDTO
import com.sliide.useractivity.data.remote.dto.UserDTO
import com.sliide.useractivity.domain.model.CreateUserRequest
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

internal fun CreateUserRequest.toDTO(): CreateUserRequestDTO = CreateUserRequestDTO(
    name = name.trim(),
    email = email.trim(),
    gender = gender.apiValue(),
    status = status.apiValue(),
)

internal fun UserGender.apiValue(): String = when (this) {
    UserGender.Male -> "male"
    UserGender.Female -> "female"
    UserGender.Unknown -> ""
}

internal fun UserStatus.apiValue(): String = when (this) {
    UserStatus.Active -> "active"
    UserStatus.Inactive -> "inactive"
    UserStatus.Unknown -> ""
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
