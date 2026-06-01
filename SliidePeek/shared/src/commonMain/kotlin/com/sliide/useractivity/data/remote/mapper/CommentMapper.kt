package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.CommentDTO
import com.sliide.useractivity.domain.model.Comment

internal fun CommentDTO.toDomain(): Comment = Comment(
    id = id,
    postId = postId,
    name = name,
    email = email,
    body = body,
)
