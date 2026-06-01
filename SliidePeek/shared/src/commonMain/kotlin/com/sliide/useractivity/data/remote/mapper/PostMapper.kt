package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.PostDTO
import com.sliide.useractivity.domain.model.Post

internal fun PostDTO.toDomain(): Post = Post(
    id = id,
    userId = userId,
    title = title,
    body = body,
)
