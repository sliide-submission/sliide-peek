package com.sliide.useractivity.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TodoDTO(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    val title: String,
    @SerialName("due_on") val dueOn: String? = null,
    val status: String,
)
