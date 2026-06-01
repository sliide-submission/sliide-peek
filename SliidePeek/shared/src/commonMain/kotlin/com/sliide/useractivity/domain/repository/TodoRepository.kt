package com.sliide.useractivity.domain.repository

import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.Todo

interface TodoRepository {
    suspend fun getUserTodos(userId: Long): AppResult<List<Todo>>
}
