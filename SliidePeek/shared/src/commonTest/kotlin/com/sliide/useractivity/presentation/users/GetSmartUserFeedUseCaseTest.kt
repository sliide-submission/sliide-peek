package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.data.local.CachedUserFeed
import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetSmartUserFeedUseCaseTest {
    @Test
    fun `fetches only the latest page - page 1 - and caches it`() = runTest {
        val cache = FakeUserCacheDataSource()
        val repository = FakeUserRepository(
            pages = mapOf(
                // Page 1 = newest users on GoREST. Later pages must never be requested.
                1 to Page(listOf(user(30), user(31)), page = 1, perPage = 2, totalPages = 3),
            ),
        )
        val useCase = GetSmartUserFeedUseCase(repository, cache, FixedClock(123_000), perPage = 2)

        val result = useCase()

        val feed = assertIs<AppResult.Success<UserFeedResult>>(result).value
        assertEquals(listOf(1), repository.requestedPages)
        assertEquals(listOf(30L, 31L), feed.users.map { it.id })
        assertEquals(123_000, feed.users.first().fetchedAtMillis)
        assertFalse(feed.fromCache)
        assertEquals(123_000, feed.lastUpdatedMillis)
        assertEquals(1, feed.currentPage)
        assertEquals(3, feed.totalPages)
        assertTrue(feed.hasNextPage)
        assertNotNull(cache.savedFeed)
    }

    @Test
    fun `network failure returns cached feed when available`() = runTest {
        val cache = FakeUserCacheDataSource(
            cachedFeed = CachedUserFeed(
                page = Page(listOf(user(99)), page = 4, perPage = 20, totalPages = 4),
                cachedAtMillis = 60_000,
                fetchedAtMillis = 50_000,
            ),
        )
        val useCase = GetSmartUserFeedUseCase(
            userRepository = FakeUserRepository(failure = AppError.Network),
            userCacheDataSource = cache,
            clock = FixedClock(90_000),
        )

        val result = useCase()

        val feed = assertIs<AppResult.Success<UserFeedResult>>(result).value
        assertTrue(feed.fromCache)
        assertEquals(listOf(99L), feed.users.map { it.id })
        assertEquals(60_000, feed.lastUpdatedMillis)
    }

    @Test
    fun `network failure without cache propagates failure`() = runTest {
        val repository = FakeUserRepository(failure = AppError.Network)
        val useCase = GetSmartUserFeedUseCase(repository, FakeUserCacheDataSource(), FixedClock(10_000))

        val result = useCase()

        assertEquals(AppError.Network, assertIs<AppResult.Failure>(result).error)
    }

    @Test
    fun `non-offline failure does not use cache`() = runTest {
        val cache = FakeUserCacheDataSource(
            cachedFeed = CachedUserFeed(
                page = Page(listOf(user(99)), page = 1, perPage = 20, totalPages = 1),
                cachedAtMillis = 1,
                fetchedAtMillis = 1,
            ),
        )
        val useCase = GetSmartUserFeedUseCase(
            userRepository = FakeUserRepository(failure = AppError.Server(500)),
            userCacheDataSource = cache,
            clock = FixedClock(10_000),
        )

        val result = useCase()

        assertEquals(AppError.Server(500), assertIs<AppResult.Failure>(result).error)
    }

    private class FixedClock(private val nowMillis: Long) : AppClock {
        override fun nowMillis(): Long = nowMillis
    }

    private class FakeUserCacheDataSource(
        private val cachedFeed: CachedUserFeed? = null,
    ) : UserCacheDataSource {
        var savedFeed: CachedUserFeed? = null

        override suspend fun replaceCachedFeed(page: Page<User>, fetchedAtMillis: Long, cachedAtMillis: Long) {
            savedFeed = CachedUserFeed(page, cachedAtMillis, fetchedAtMillis)
        }

        override suspend fun appendCachedPage(page: Page<User>, fetchedAtMillis: Long, cachedAtMillis: Long) {
            savedFeed = CachedUserFeed(page, cachedAtMillis, fetchedAtMillis)
        }

        override suspend fun getCachedFeed(): CachedUserFeed? = cachedFeed

        override suspend fun insertCreatedUserAtTop(user: User, createdAtMillis: Long, cachedAtMillis: Long) = Unit
        override suspend fun deleteUser(id: Long) = Unit
    }

    private class FakeUserRepository(
        private val pages: Map<Int, Page<User>> = emptyMap(),
        private val failure: AppError? = null,
    ) : UserRepository {
        val requestedPages = mutableListOf<Int>()

        override suspend fun getUsers(page: Int, perPage: Int): AppResult<Page<User>> {
            requestedPages += page
            failure?.let { return AppResult.Failure(it) }
            return AppResult.Success(pages.getValue(page))
        }

        override suspend fun createUser(request: CreateUserRequest): AppResult<User> = error("Not needed")
        override suspend fun deleteUser(id: Long): AppResult<Unit> = error("Not needed")
        override suspend fun getUser(id: Long): AppResult<User> = error("Not needed")
    }

    private fun user(id: Long) = User(
        id = id,
        name = "User $id",
        email = "user$id@example.com",
        gender = UserGender.Unknown,
        status = UserStatus.Active,
    )
}
