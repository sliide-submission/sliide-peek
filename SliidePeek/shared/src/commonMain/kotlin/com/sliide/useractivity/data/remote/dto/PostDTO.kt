package com.sliide.useractivity.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PostDTO(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    val title: String,
    val body: String,
)
