package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.RelativeTimeFormatter

interface LoadUserFeedUseCase {
    suspend operator fun invoke(): AppResult<List<UserFeedItem>>
}

class GetSmartUserFeedUseCase(
    private val userRepository: UserRepository,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
    private val perPage: Int = 20,
) : LoadUserFeedUseCase {
    override suspend fun invoke(): AppResult<List<UserFeedItem>> {
        val firstPageResult = userRepository.getUsers(page = 1, perPage = perPage)
        val firstPage = when (firstPageResult) {
            is AppResult.Success -> firstPageResult.value
            is AppResult.Failure -> return firstPageResult
        }

        val lastPage = firstPage.totalPages.coerceAtLeast(1)
        val finalPage = if (lastPage == firstPage.page) {
            firstPage
        } else {
            when (val lastPageResult = userRepository.getUsers(page = lastPage, perPage = perPage)) {
                is AppResult.Success -> lastPageResult.value
                is AppResult.Failure -> return lastPageResult
            }
        }

        val fetchedAtMillis = clock.nowMillis()
        val users = finalPage.items.map { user -> user.toFeedItem(fetchedAtMillis) }
        return AppResult.Success(users)
    }

    private fun User.toFeedItem(fetchedAtMillis: Long): UserFeedItem = UserFeedItem(
        id = id,
        name = name,
        email = email,
        gender = gender,
        status = status,
        fetchedAtMillis = fetchedAtMillis,
        relativeTimestamp = relativeTimeFormatter.format(
            thenMillis = fetchedAtMillis,
            nowMillis = clock.nowMillis(),
        ),
    )
}
