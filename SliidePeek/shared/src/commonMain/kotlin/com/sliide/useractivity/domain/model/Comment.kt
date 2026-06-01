package com.sliide.useractivity.domain.model

data class Comment(
    val id: Long,
    val postId: Long,
    val name: String,
    val email: String,
    val body: String,
)
