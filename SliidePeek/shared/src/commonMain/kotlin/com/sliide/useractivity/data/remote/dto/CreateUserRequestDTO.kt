package com.sliide.useractivity.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CreateUserRequestDTO(
    val name: String,
    val email: String,
    val gender: String,
    val status: String,
)
