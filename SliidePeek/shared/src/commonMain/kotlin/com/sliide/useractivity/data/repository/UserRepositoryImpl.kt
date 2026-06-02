package com.sliide.useractivity.data.repository

import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.mapper.toDTO
import com.sliide.useractivity.data.remote.mapper.toDomain
import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.auth.BearerTokenProvider
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.repository.UserRepository

class UserRepositoryImpl internal constructor(
    private val apiClient: GorestApiClient,
    private val bearerTokenProvider: BearerTokenProvider,
) : UserRepository {
    override suspend fun getUsers(page: Int, perPage: Int): AppResult<Page<User>> = repositoryCall {
        val response = apiClient.getUsers(
            page = page,
            perPage = perPage,
            bearerToken = bearerTokenProvider.getToken(),
        )
        Page(
            items = response.items.map { it.toDomain() },
            page = response.page,
            perPage = response.perPage,
            totalPages = response.totalPages,
        )
    }

    override suspend fun createUser(request: CreateUserRequest): AppResult<User> {
        val token = bearerTokenProvider.getToken() ?: return AppResult.Failure(AppError.Unauthorized)
        return repositoryCall {
            apiClient.createUser(request = request.toDTO(), bearerToken = token).toDomain()
        }
    }

    override suspend fun deleteUser(id: Long): AppResult<Unit> {
        val token = bearerTokenProvider.getToken() ?: return AppResult.Failure(AppError.Unauthorized)
        return repositoryCall {
            apiClient.deleteUser(id = id, bearerToken = token)
        }
    }

    override suspend fun getUser(id: Long): AppResult<User> = repositoryCall {
        apiClient.getUser(id).toDomain()
    }
}
