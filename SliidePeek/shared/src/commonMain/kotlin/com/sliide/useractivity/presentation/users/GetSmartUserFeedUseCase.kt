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

interface LoadOlderUsersUseCase {
    suspend operator fun invoke(page: Int): AppResult<UserFeedResult>
}

data class UserFeedResult(
    val users: List<UserFeedItem>,
    val fromCache: Boolean,
    val lastUpdatedMillis: Long?,
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
) {
    val nextPage: Int?
        get() = if (hasNextPage) currentPage + 1 else null
}

class GetSmartUserFeedUseCase(
    private val userRepository: UserRepository,
    private val userCacheDataSource: UserCacheDataSource,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
    private val perPage: Int = 20,
) : LoadUserFeedUseCase {
    override suspend fun invoke(): AppResult<UserFeedResult> {
        // The newest users live on page 1 of GoREST (newest-first), so the "latest" feed starts at page 1.
        val pageResult = userRepository.getUsers(page = 1, perPage = perPage)
        val page = when (pageResult) {
            is AppResult.Success -> pageResult.value
            is AppResult.Failure -> return pageResult.withCachedFallback()
        }

        val fetchedAtMillis = clock.nowMillis()
        val cachedAtMillis = fetchedAtMillis
        // A cache-write failure must not fail an otherwise-successful network load.
        runCatching {
            userCacheDataSource.replaceCachedFeed(
                page = page,
                fetchedAtMillis = fetchedAtMillis,
                cachedAtMillis = cachedAtMillis,
            )
        }

        return AppResult.Success(page.toFeedResult(fetchedAtMillis, cachedAtMillis, fromCache = false))
    }

    private suspend fun AppResult.Failure.withCachedFallback(): AppResult<UserFeedResult> {
        if (!error.isOfflineError()) return this
        val cachedFeed = userCacheDataSource.getCachedFeed() ?: return this
        return AppResult.Success(cachedFeed.toFeedResult())
    }

    private fun CachedUserFeed.toFeedResult(): UserFeedResult = page.toFeedResult(
        fetchedAtMillis = fetchedAtMillis,
        lastUpdatedMillis = cachedAtMillis,
        fromCache = true,
    )

    private fun Page<User>.toFeedResult(
        fetchedAtMillis: Long,
        lastUpdatedMillis: Long?,
        fromCache: Boolean,
    ): UserFeedResult = UserFeedResult(
        users = toFeedItems(fetchedAtMillis),
        fromCache = fromCache,
        lastUpdatedMillis = lastUpdatedMillis,
        currentPage = page,
        totalPages = totalPages,
        hasNextPage = hasNextPage,
    )

    private fun Page<User>.toFeedItems(fetchedAtMillis: Long): List<UserFeedItem> =
        items.map { user -> user.toFeedItem(fetchedAtMillis, relativeTimeFormatter, clock) }

    private fun AppError.isOfflineError(): Boolean = this is AppError.Network || this is AppError.Timeout
}

class LoadOlderUsersUseCaseImpl(
    private val userRepository: UserRepository,
    private val userCacheDataSource: UserCacheDataSource,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
    private val perPage: Int = 20,
) : LoadOlderUsersUseCase {
    override suspend fun invoke(page: Int): AppResult<UserFeedResult> {
        if (page <= 1) return AppResult.Failure(AppError.Validation("Older users start at page 2."))
        val pageResult = userRepository.getUsers(page = page, perPage = perPage)
        val loadedPage = when (pageResult) {
            is AppResult.Success -> pageResult.value
            is AppResult.Failure -> return pageResult
        }
        val fetchedAtMillis = clock.nowMillis()
        val cachedAtMillis = fetchedAtMillis
        runCatching {
            userCacheDataSource.appendCachedPage(
                page = loadedPage,
                fetchedAtMillis = fetchedAtMillis,
                cachedAtMillis = cachedAtMillis,
            )
        }
        return AppResult.Success(
            UserFeedResult(
                users = loadedPage.items.map { it.toFeedItem(fetchedAtMillis, relativeTimeFormatter, clock) },
                fromCache = false,
                lastUpdatedMillis = cachedAtMillis,
                currentPage = loadedPage.page,
                totalPages = loadedPage.totalPages,
                hasNextPage = loadedPage.hasNextPage,
            ),
        )
    }
}
