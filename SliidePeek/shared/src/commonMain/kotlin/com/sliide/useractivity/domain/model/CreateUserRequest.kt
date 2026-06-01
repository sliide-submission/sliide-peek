package com.sliide.useractivity.domain.model

data class CreateUserRequest(
    val name: String,
    val email: String,
    val gender: UserGender,
    val status: UserStatus,
)
