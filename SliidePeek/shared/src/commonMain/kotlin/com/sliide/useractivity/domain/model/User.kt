package com.sliide.useractivity.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val gender: UserGender,
    val status: UserStatus,
)

enum class UserGender {
    Male,
    Female,
    Unknown,
}

enum class UserStatus {
    Active,
    Inactive,
    Unknown,
}
