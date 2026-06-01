package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.data.local.CachedUserFeed
import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.Post
import com.sliide.useractivity.domain.model.Todo
import com.sliide.useractivity.domain.model.User
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CreateUserUseCaseTest {
    @Test
    fun `successful create updates cache and returns feed item`() = runTest {
        val cache = FakeUserCacheDataSource()
        val useCase = CreateUserUseCaseImpl(
            userRepository = FakeUserRepository(AppResult.Success(user(99))),
            userCacheDataSource = cache,
            clock = FixedClock(123_000),
        )

        val result = useCase(request())

        val feedItem = assertIs<AppResult.Success<UserFeedItem>>(result).value
        assertEquals(99, feedItem.id)
        assertEquals("just now", feedItem.relativeTimestamp)
        assertEquals(99, cache.insertedUser?.id)
        assertEquals(123_000, cache.createdAtMillis)
    }

    @Test
    fun `failure does not update cache`() = runTest {
        val cache = FakeUserCacheDataSource()
        val useCase = CreateUserUseCaseImpl(
            userRepository = FakeUserRepository(AppResult.Failure(AppError.Unauthorized)),
            userCacheDataSource = cache,
            clock = FixedClock(123_000),
        )

        val result = useCase(request())

        assertEquals(AppError.Unauthorized, assertIs<AppResult.Failure>(result).error)
        assertEquals(null, cache.insertedUser)
    }

    private class FakeUserCacheDataSource : UserCacheDataSource {
        var insertedUser: User? = null
        var createdAtMillis: Long? = null

        override suspend fun replaceLastPage(page: Page<User>, fetchedAtMillis: Long, cachedAtMillis: Long) = Unit
        override suspend fun getLastPageFeed(): CachedUserFeed? = null
        override suspend fun insertCreatedUserAtTop(user: User, createdAtMillis: Long, cachedAtMillis: Long) {
            insertedUser = user
            this.createdAtMillis = createdAtMillis
        }
        override suspend fun deleteUser(id: Long) = Unit
    }

    private class FakeUserRepository(
        private val result: AppResult<User>,
    ) : UserRepository {
        override suspend fun getUsers(page: Int, perPage: Int): AppResult<Page<User>> = error("Not needed")
        override suspend fun createUser(request: CreateUserRequest): AppResult<User> = result
        override suspend fun deleteUser(id: Long): AppResult<Unit> = error("Not needed")
        override suspend fun getUser(id: Long): AppResult<User> = error("Not needed")
        override suspend fun getUserPosts(userId: Long): AppResult<List<Post>> = error("Not needed")
        override suspend fun getUserTodos(userId: Long): AppResult<List<Todo>> = error("Not needed")
    }

    private class FixedClock(private val nowMillis: Long) : AppClock {
        override fun nowMillis(): Long = nowMillis
    }

    private fun request() = CreateUserRequest(
        name = "Maya Reed",
        email = "maya.reed@example.com",
        gender = UserGender.Female,
        status = UserStatus.Active,
    )

    private fun user(id: Long) = User(
        id = id,
        name = "Maya Reed",
        email = "maya.reed@example.com",
        gender = UserGender.Female,
        status = UserStatus.Active,
    )
}
