package com.sliide.useractivity.domain.repository

import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.Comment
import com.sliide.useractivity.domain.model.Post

interface PostRepository {
    suspend fun getPost(id: Long): AppResult<Post>
    suspend fun getPostComments(postId: Long): AppResult<List<Comment>>
}
