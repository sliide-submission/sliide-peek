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
        // The newest users live on page 1 of GoREST (newest-first), so the "latest" feed is page 1.
        val pageResult = userRepository.getUsers(page = 1, perPage = perPage)
        val page = when (pageResult) {
            is AppResult.Success -> pageResult.value
            is AppResult.Failure -> return pageResult.withCachedFallback()
        }

        val fetchedAtMillis = clock.nowMillis()
        val cachedAtMillis = fetchedAtMillis
        userCacheDataSource.replaceCachedFeed(
            page = page,
            fetchedAtMillis = fetchedAtMillis,
            cachedAtMillis = cachedAtMillis,
        )

        return AppResult.Success(
            UserFeedResult(
                users = page.toFeedItems(fetchedAtMillis),
                fromCache = false,
                lastUpdatedMillis = cachedAtMillis,
            ),
        )
    }

    private suspend fun AppResult.Failure.withCachedFallback(): AppResult<UserFeedResult> {
        if (!error.isOfflineError()) return this
        val cachedFeed = userCacheDataSource.getCachedFeed() ?: return this
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
