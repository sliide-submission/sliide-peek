package com.sliide.useractivity.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CommentDTO(
    val id: Long,
    @SerialName("post_id") val postId: Long,
    val name: String,
    val email: String,
    val body: String,
)
