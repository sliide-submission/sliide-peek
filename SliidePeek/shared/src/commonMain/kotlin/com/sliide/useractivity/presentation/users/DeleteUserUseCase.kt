package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.repository.UserRepository

interface DeleteUserUseCase {
    suspend operator fun invoke(id: Long): AppResult<Unit>
}

class DeleteUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val userCacheDataSource: UserCacheDataSource,
) : DeleteUserUseCase {
    override suspend fun invoke(id: Long): AppResult<Unit> = when (val result = userRepository.deleteUser(id)) {
        is AppResult.Success -> {
            userCacheDataSource.deleteUser(id)
            result
        }
        is AppResult.Failure -> result
    }
}
