package com.sliide.useractivity.data.repository

import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.mapper.toDomain
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.Todo
import com.sliide.useractivity.domain.repository.TodoRepository

class TodoRepositoryImpl internal constructor(
    private val apiClient: GorestApiClient,
) : TodoRepository {
    override suspend fun getUserTodos(userId: Long): AppResult<List<Todo>> = repositoryCall {
        apiClient.getUserTodos(userId).map { it.toDomain() }
    }
}
