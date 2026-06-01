package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.data.local.CachedUserFeed
import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.RelativeTimeFormatter

interface LoadUserFeedUseCase {
    suspend operator fun invoke(): AppResult<UserFeedResult>
}

data class UserFeedResult(
    val users: List<UserFeedItem>,
    val fromCache: Boolean,
    val lastUpdatedMillis: Long?,
)

class GetSmartUserFeedUseCase(
    private val userRepository: UserRepository,
    private val userCacheDataSource: UserCacheDataSource,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
    private val perPage: Int = 20,
) : LoadUserFeedUseCase {
    override suspend fun invoke(): AppResult<UserFeedResult> {
        val firstPageResult = userRepository.getUsers(page = 1, perPage = perPage)
        val firstPage = when (firstPageResult) {
            is AppResult.Success -> firstPageResult.value
            is AppResult.Failure -> return firstPageResult.withCachedFallback()
        }

        val lastPage = firstPage.totalPages.coerceAtLeast(1)
        val finalPage = if (lastPage == firstPage.page) {
            firstPage
        } else {
            when (val lastPageResult = userRepository.getUsers(page = lastPage, perPage = perPage)) {
                is AppResult.Success -> lastPageResult.value
                is AppResult.Failure -> return lastPageResult.withCachedFallback()
            }
        }

        val fetchedAtMillis = clock.nowMillis()
        val cachedAtMillis = fetchedAtMillis
        userCacheDataSource.replaceLastPage(
            page = finalPage,
            fetchedAtMillis = fetchedAtMillis,
            cachedAtMillis = cachedAtMillis,
        )

        return AppResult.Success(
            UserFeedResult(
                users = finalPage.toFeedItems(fetchedAtMillis),
                fromCache = false,
                lastUpdatedMillis = cachedAtMillis,
            ),
        )
    }

    private suspend fun AppResult.Failure.withCachedFallback(): AppResult<UserFeedResult> {
        if (!error.isOfflineError()) return this
        val cachedFeed = userCacheDataSource.getLastPageFeed() ?: return this
        return AppResult.Success(cachedFeed.toFeedResult())
    }

    private fun CachedUserFeed.toFeedResult(): UserFeedResult = UserFeedResult(
        users = page.toFeedItems(fetchedAtMillis),
        fromCache = true,
        lastUpdatedMillis = cachedAtMillis,
    )

    private fun Page<User>.toFeedItems(fetchedAtMillis: Long): List<UserFeedItem> =
        items.map { user -> user.toFeedItem(fetchedAtMillis) }

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

    private fun AppError.isOfflineError(): Boolean = this is AppError.Network || this is AppError.Timeout
}
