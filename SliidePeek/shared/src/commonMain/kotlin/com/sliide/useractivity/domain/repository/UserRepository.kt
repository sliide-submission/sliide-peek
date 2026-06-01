package com.sliide.useractivity.domain.repository

import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.Post
import com.sliide.useractivity.domain.model.Todo
import com.sliide.useractivity.domain.model.User

interface UserRepository {
    suspend fun getUsers(page: Int = 1, perPage: Int = 20): AppResult<Page<User>>
    suspend fun createUser(request: CreateUserRequest): AppResult<User>
    suspend fun deleteUser(id: Long): AppResult<Unit>
    suspend fun getUser(id: Long): AppResult<User>
    suspend fun getUserPosts(userId: Long): AppResult<List<Post>>
    suspend fun getUserTodos(userId: Long): AppResult<List<Todo>>
}
