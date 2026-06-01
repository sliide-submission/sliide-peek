package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.RelativeTimeFormatter

interface CreateUserUseCase {
    suspend operator fun invoke(request: CreateUserRequest): AppResult<UserFeedItem>
}

class CreateUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val userCacheDataSource: UserCacheDataSource,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
) : CreateUserUseCase {
    override suspend fun invoke(request: CreateUserRequest): AppResult<UserFeedItem> = when (val result = userRepository.createUser(request)) {
        is AppResult.Success -> {
            val createdAtMillis = clock.nowMillis()
            userCacheDataSource.insertCreatedUserAtTop(
                user = result.value,
                createdAtMillis = createdAtMillis,
                cachedAtMillis = createdAtMillis,
            )
            AppResult.Success(result.value.toFeedItem(createdAtMillis))
        }
        is AppResult.Failure -> result
    }

    private fun User.toFeedItem(createdAtMillis: Long): UserFeedItem = UserFeedItem(
        id = id,
        name = name,
        email = email,
        gender = gender,
        status = status,
        fetchedAtMillis = createdAtMillis,
        relativeTimestamp = relativeTimeFormatter.format(
            thenMillis = createdAtMillis,
            nowMillis = clock.nowMillis(),
        ),
    )
}
