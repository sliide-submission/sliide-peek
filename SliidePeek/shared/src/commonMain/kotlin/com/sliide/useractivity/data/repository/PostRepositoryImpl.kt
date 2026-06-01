package com.sliide.useractivity.data.repository

import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.mapper.toDomain
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.Comment
import com.sliide.useractivity.domain.model.Post
import com.sliide.useractivity.domain.repository.PostRepository

class PostRepositoryImpl internal constructor(
    private val apiClient: GorestApiClient,
) : PostRepository {
    override suspend fun getPost(id: Long): AppResult<Post> = repositoryCall {
        apiClient.getPost(id).toDomain()
    }

    override suspend fun getPostComments(postId: Long): AppResult<List<Comment>> = repositoryCall {
        apiClient.getPostComments(postId).map { it.toDomain() }
    }
}
