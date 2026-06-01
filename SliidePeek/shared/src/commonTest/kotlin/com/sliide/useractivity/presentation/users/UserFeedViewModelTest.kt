package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.domain.time.AppClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserFeedViewModelTest {
    @Test
    fun `initial state is empty and idle`() = runTest {
        val viewModel = viewModel(FakeLoadUserFeedUseCase())

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.users.isEmpty())
    }

    @Test
    fun `load emits loading then success`() = runTest {
        val pending = CompletableDeferred<AppResult<UserFeedResult>>()
        val viewModel = viewModel(FakeLoadUserFeedUseCase(pending = pending))

        viewModel.load()
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)

        pending.complete(AppResult.Success(feedResult(listOf(feedUser(1)), lastUpdatedMillis = 1_000)))
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
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Success(feedResult(emptyList())))),
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isEmpty)
        assertEquals(null, state.errorMessage)
        assertEquals(null, state.offlineMessage)
    }

    @Test
    fun `cached success produces offline cached state`() = runTest {
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(
                    AppResult.Success(
                        feedResult(
                            users = listOf(feedUser(1)),
                            fromCache = true,
                            lastUpdatedMillis = 30_000,
                        ),
                    ),
                ),
            ),
            nowMillis = 90_000,
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(1L), state.users.map { it.id })
        assertTrue(state.isOffline)
        assertTrue(state.canRetry)
        assertEquals("1 min ago", state.lastUpdatedLabel)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `network failure with no users produces offline retry state`() = runTest {
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Failure(AppError.Network))),
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
        val refreshResult = CompletableDeferred<AppResult<UserFeedResult>>()
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(AppResult.Success(feedResult(listOf(feedUser(1)), lastUpdatedMillis = 1_000))),
                pending = refreshResult,
            ),
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
        val pending = CompletableDeferred<AppResult<UserFeedResult>>()
        val useCase = FakeLoadUserFeedUseCase(
            results = mutableListOf(AppResult.Failure(AppError.Server(500))),
            pending = pending,
        )
        val viewModel = viewModel(useCase)

        viewModel.load()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.canRetry)

        viewModel.retry()
        runCurrent()

        val retryingState = viewModel.state.value
        assertTrue(retryingState.isLoading)
        assertEquals(null, retryingState.errorMessage)
        assertFalse(retryingState.canRetry)

        pending.complete(AppResult.Success(feedResult(listOf(feedUser(2)))))
        advanceUntilIdle()

        assertEquals(listOf(2L), viewModel.state.value.users.map { it.id })
    }

    @Test
    fun `opening and dismissing add user toggles form visibility`() = runTest {
        val viewModel = viewModel(FakeLoadUserFeedUseCase())

        viewModel.openAddUser()
        assertTrue(viewModel.state.value.isAddUserVisible)

        viewModel.dismissAddUser()
        assertFalse(viewModel.state.value.isAddUserVisible)
    }

    @Test
    fun `add user validation updates as fields change`() = runTest {
        val viewModel = viewModel(FakeLoadUserFeedUseCase())

        viewModel.openAddUser()
        viewModel.onAddUserNameChanged("Maya Reed")
        viewModel.onAddUserEmailChanged("maya.reed@example.com")

        val form = viewModel.state.value.addUserForm
        assertEquals(null, form.nameError)
        assertEquals(null, form.emailError)
        assertTrue(form.isSubmitEnabled)
    }

    @Test
    fun `invalid add user form blocks submit`() = runTest {
        val createUser = FakeCreateUserUseCase(AppResult.Success(feedUser(99)))
        val viewModel = viewModel(FakeLoadUserFeedUseCase(), createUser = createUser)

        viewModel.openAddUser()
        viewModel.submitAddUser()
        advanceUntilIdle()

        assertEquals(0, createUser.requests.size)
        assertEquals("Name is required", viewModel.state.value.addUserForm.nameError)
    }

    @Test
    fun `submit add user shows loading then inserts created user at top`() = runTest {
        val pending = CompletableDeferred<AppResult<UserFeedItem>>()
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Success(feedResult(listOf(feedUser(1)))))),
            createUser = FakeCreateUserUseCase(pending = pending),
        )
        viewModel.load()
        advanceUntilIdle()
        viewModel.openAddUser()
        viewModel.onAddUserNameChanged("Maya Reed")
        viewModel.onAddUserEmailChanged("maya.reed@example.com")

        viewModel.submitAddUser()
        runCurrent()

        assertTrue(viewModel.state.value.addUserForm.isSubmitting)

        pending.complete(AppResult.Success(feedUser(99)))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isAddUserVisible)
        assertEquals(listOf(99L, 1L), state.users.map { it.id })
        assertEquals(99L, state.highlightedUserId)
    }

    @Test
    fun `submit add user failure keeps input and shows submit error`() = runTest {
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(),
            createUser = FakeCreateUserUseCase(AppResult.Failure(AppError.Unauthorized)),
        )
        viewModel.openAddUser()
        viewModel.onAddUserNameChanged("Maya Reed")
        viewModel.onAddUserEmailChanged("maya.reed@example.com")

        viewModel.submitAddUser()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isAddUserVisible)
        assertEquals("Maya Reed", state.addUserForm.name)
        assertFalse(state.addUserForm.isSubmitting)
        assertEquals("GoREST API token required to add users. Add it locally and rebuild.", state.addUserForm.submitErrorMessage)
    }

    private fun TestScope.viewModel(
        loadUserFeedUseCase: LoadUserFeedUseCase,
        createUser: CreateUserUseCase = FakeCreateUserUseCase(AppResult.Failure(AppError.Unauthorized)),
        nowMillis: Long = 1_000,
    ): UserFeedViewModel = UserFeedViewModel(
        loadUserFeed = loadUserFeedUseCase,
        createUser = createUser,
        scope = this,
        clock = FixedClock(nowMillis),
    )

    private class FakeLoadUserFeedUseCase(
        private val results: MutableList<AppResult<UserFeedResult>> = mutableListOf(),
        private val pending: CompletableDeferred<AppResult<UserFeedResult>>? = null,
    ) : LoadUserFeedUseCase {
        override suspend fun invoke(): AppResult<UserFeedResult> =
            if (results.isNotEmpty()) results.removeAt(0) else pending!!.await()
    }

    private class FakeCreateUserUseCase(
        private val result: AppResult<UserFeedItem>? = null,
        private val pending: CompletableDeferred<AppResult<UserFeedItem>>? = null,
    ) : CreateUserUseCase {
        val requests = mutableListOf<CreateUserRequest>()

        override suspend fun invoke(request: CreateUserRequest): AppResult<UserFeedItem> {
            requests += request
            return result ?: pending!!.await()
        }
    }

    private class FixedClock(private val nowMillis: Long) : AppClock {
        override fun nowMillis(): Long = nowMillis
    }

    private fun feedResult(
        users: List<UserFeedItem>,
        fromCache: Boolean = false,
        lastUpdatedMillis: Long? = users.firstOrNull()?.fetchedAtMillis,
    ) = UserFeedResult(
        users = users,
        fromCache = fromCache,
        lastUpdatedMillis = lastUpdatedMillis,
    )

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
