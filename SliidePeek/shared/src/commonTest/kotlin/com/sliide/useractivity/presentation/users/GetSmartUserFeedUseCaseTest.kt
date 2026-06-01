package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
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

class GetSmartUserFeedUseCaseTest {
    @Test
    fun `fetches first page then last page from pagination metadata`() = runTest {
        val repository = FakeUserRepository(
            pages = mapOf(
                1 to Page(listOf(user(1)), page = 1, perPage = 2, totalPages = 3),
                3 to Page(listOf(user(30), user(31)), page = 3, perPage = 2, totalPages = 3),
            ),
        )
        val useCase = GetSmartUserFeedUseCase(repository, FixedClock(123_000), perPage = 2)

        val result = useCase()

        val users = assertIs<AppResult.Success<List<UserFeedItem>>>(result).value
        assertEquals(listOf(1, 3), repository.requestedPages)
        assertEquals(listOf(30L, 31L), users.map { it.id })
        assertEquals(123_000, users.first().fetchedAtMillis)
    }

    @Test
    fun `reuses first page when it is already the last page`() = runTest {
        val repository = FakeUserRepository(
            pages = mapOf(1 to Page(listOf(user(1)), page = 1, perPage = 20, totalPages = 1)),
        )
        val useCase = GetSmartUserFeedUseCase(repository, FixedClock(10_000))

        val result = useCase()

        assertIs<AppResult.Success<List<UserFeedItem>>>(result)
        assertEquals(listOf(1), repository.requestedPages)
    }

    @Test
    fun `propagates repository failure`() = runTest {
        val repository = FakeUserRepository(failure = AppError.Network)
        val useCase = GetSmartUserFeedUseCase(repository, FixedClock(10_000))

        val result = useCase()

        assertEquals(AppError.Network, assertIs<AppResult.Failure>(result).error)
    }

    private class FixedClock(private val nowMillis: Long) : AppClock {
        override fun nowMillis(): Long = nowMillis
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

        override suspend fun getUser(id: Long): AppResult<User> = error("Not needed")
        override suspend fun getUserPosts(userId: Long): AppResult<List<Post>> = error("Not needed")
        override suspend fun getUserTodos(userId: Long): AppResult<List<Todo>> = error("Not needed")
    }

    private fun user(id: Long) = User(
        id = id,
        name = "User $id",
        email = "user$id@example.com",
        gender = UserGender.Unknown,
        status = UserStatus.Active,
    )
}
