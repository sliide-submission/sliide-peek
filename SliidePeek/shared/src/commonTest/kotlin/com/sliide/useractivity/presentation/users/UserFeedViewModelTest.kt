package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserFeedViewModelTest {
    @Test
    fun `initial state is empty and idle`() = runTest {
        val viewModel = UserFeedViewModel(FakeLoadUserFeedUseCase(), this)

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.users.isEmpty())
    }

    @Test
    fun `load emits loading then success`() = runTest {
        val pending = CompletableDeferred<AppResult<List<UserFeedItem>>>()
        val viewModel = UserFeedViewModel(FakeLoadUserFeedUseCase(pending = pending), this)

        viewModel.load()
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)

        pending.complete(AppResult.Success(listOf(feedUser(1))))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(1L), state.users.map { it.id })
        assertEquals(null, state.errorMessage)
        assertEquals(null, state.offlineMessage)
        assertEquals("just now", state.lastUpdatedLabel)
    }

    @Test
    fun `empty success produces empty non-error state`() = runTest {
        val viewModel = UserFeedViewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Success(emptyList()))),
            this,
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isEmpty)
        assertEquals(null, state.errorMessage)
        assertEquals(null, state.offlineMessage)
    }

    @Test
    fun `network failure with no users produces offline retry state`() = runTest {
        val viewModel = UserFeedViewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Failure(AppError.Network))),
            this,
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isOffline)
        assertTrue(state.canRetry)
        assertTrue(state.users.isEmpty())
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `refresh failure keeps existing users visible and records offline state`() = runTest {
        val refreshResult = CompletableDeferred<AppResult<List<UserFeedItem>>>()
        val viewModel = UserFeedViewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(AppResult.Success(listOf(feedUser(1)))),
                pending = refreshResult,
            ),
            this,
        )

        viewModel.load()
        advanceUntilIdle()
        viewModel.refresh()
        runCurrent()

        assertTrue(viewModel.state.value.isRefreshing)
        assertEquals(listOf(1L), viewModel.state.value.users.map { it.id })

        refreshResult.complete(AppResult.Failure(AppError.Timeout))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isRefreshing)
        assertEquals(listOf(1L), state.users.map { it.id })
        assertEquals("just now", state.lastUpdatedLabel)
        assertTrue(state.isOffline)
        assertTrue(state.canRetry)
    }

    @Test
    fun `retry clears previous error before attempting again`() = runTest {
        val pending = CompletableDeferred<AppResult<List<UserFeedItem>>>()
        val useCase = FakeLoadUserFeedUseCase(
            results = mutableListOf(AppResult.Failure(AppError.Server(500))),
            pending = pending,
        )
        val viewModel = UserFeedViewModel(useCase, this)

        viewModel.load()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.canRetry)

        viewModel.retry()
        runCurrent()

        val retryingState = viewModel.state.value
        assertTrue(retryingState.isLoading)
        assertEquals(null, retryingState.errorMessage)
        assertFalse(retryingState.canRetry)

        pending.complete(AppResult.Success(listOf(feedUser(2))))
        advanceUntilIdle()

        assertEquals(listOf(2L), viewModel.state.value.users.map { it.id })
    }

    private class FakeLoadUserFeedUseCase(
        private val results: MutableList<AppResult<List<UserFeedItem>>> = mutableListOf(),
        private val pending: CompletableDeferred<AppResult<List<UserFeedItem>>>? = null,
    ) : LoadUserFeedUseCase {
        override suspend fun invoke(): AppResult<List<UserFeedItem>> =
            if (results.isNotEmpty()) results.removeAt(0) else pending!!.await()
    }

    private fun feedUser(id: Long) = UserFeedItem(
        id = id,
        name = "User $id",
        email = "user$id@example.com",
        gender = UserGender.Unknown,
        status = UserStatus.Active,
        fetchedAtMillis = 1_000,
        relativeTimestamp = "just now",
    )
}
