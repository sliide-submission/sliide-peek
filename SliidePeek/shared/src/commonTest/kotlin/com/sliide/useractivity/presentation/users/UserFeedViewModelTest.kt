package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.connectivity.ConnectivityMonitor
import com.sliide.useractivity.domain.connectivity.ConnectivityStatus
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.domain.time.AppClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    fun `load success records next page when more users are available`() = runTest {
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(
                    AppResult.Success(feedResult(listOf(feedUser(1)), currentPage = 1, totalPages = 3)),
                ),
            ),
        )

        viewModel.load()
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.nextPage)
        assertTrue(viewModel.state.value.hasMoreUsers)
    }

    @Test
    fun `load more appends older users and de-dupes ids`() = runTest {
        val older = FakeLoadOlderUsersUseCase(
            mutableMapOf(
                2 to AppResult.Success(feedResult(listOf(feedUser(2), feedUser(3)), currentPage = 2, totalPages = 3)),
                3 to AppResult.Success(feedResult(listOf(feedUser(3), feedUser(4)), currentPage = 3, totalPages = 3)),
            ),
        )
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(AppResult.Success(feedResult(listOf(feedUser(1), feedUser(2)), currentPage = 1, totalPages = 3))),
            ),
            loadOlderUsers = older,
        )

        viewModel.load()
        advanceUntilIdle()
        viewModel.loadMoreUsers()
        advanceUntilIdle()
        viewModel.loadMoreUsers()
        advanceUntilIdle()

        assertEquals(listOf(2, 3), older.requestedPages)
        assertEquals(listOf(1L, 2L, 3L, 4L), viewModel.state.value.users.map { it.id })
        assertFalse(viewModel.state.value.hasMoreUsers)
        assertEquals(null, viewModel.state.value.nextPage)
    }

    @Test
    fun `load more failure preserves existing users`() = runTest {
        val older = FakeLoadOlderUsersUseCase(mutableMapOf(2 to AppResult.Failure(AppError.Server(500))))
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(AppResult.Success(feedResult(listOf(feedUser(1)), currentPage = 1, totalPages = 2))),
            ),
            loadOlderUsers = older,
        )

        viewModel.load()
        advanceUntilIdle()
        viewModel.loadMoreUsers()
        advanceUntilIdle()

        assertEquals(listOf(1L), viewModel.state.value.users.map { it.id })
        assertFalse(viewModel.state.value.isLoadingMore)
        assertEquals("The service is unavailable right now. Please try again.", viewModel.state.value.loadMoreErrorMessage)
        assertEquals(2, viewModel.state.value.nextPage)
    }

    @Test
    fun `refresh after pagination resets pagination to latest page`() = runTest {
        val older = FakeLoadOlderUsersUseCase(
            mutableMapOf(2 to AppResult.Success(feedResult(listOf(feedUser(2)), currentPage = 2, totalPages = 2))),
        )
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(
                    AppResult.Success(feedResult(listOf(feedUser(1)), currentPage = 1, totalPages = 2)),
                    AppResult.Success(feedResult(listOf(feedUser(9)), currentPage = 1, totalPages = 1)),
                ),
            ),
            loadOlderUsers = older,
        )

        viewModel.load()
        advanceUntilIdle()
        viewModel.loadMoreUsers()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(listOf(9L), viewModel.state.value.users.map { it.id })
        assertFalse(viewModel.state.value.hasMoreUsers)
        assertEquals(null, viewModel.state.value.nextPage)
    }

    @Test
    fun `connectivity offline with loaded users shows cached banner state`() = runTest {
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Success(feedResult(listOf(feedUser(1))))),),
        )

        viewModel.load()
        advanceUntilIdle()
        viewModel.onConnectivityChanged(ConnectivityStatus.Offline)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isDeviceOffline)
        assertEquals("Offline — showing cached users", viewModel.state.value.offlineMessage)
        assertFalse(viewModel.state.value.hasMoreUsers)
    }

    @Test
    fun `connectivity offline with no users shows no-cache offline state`() = runTest {
        val viewModel = viewModel(FakeLoadUserFeedUseCase())
        viewModel.onConnectivityChanged(ConnectivityStatus.Offline)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isOffline)
        assertTrue(viewModel.state.value.users.isEmpty())
        assertEquals("No internet connection. Try again when you are back online.", viewModel.state.value.offlineMessage)
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

    @Test
    fun `confirming delete while offline state still starts undo flow`() = runTest {
        val events = RecordedEvents()
        val delete = FakeDeleteUserUseCase()
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(
                results = mutableListOf(
                    AppResult.Success(feedResult(listOf(feedUser(1)), fromCache = true)),
                ),
            ),
            deleteUser = delete,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect(events.events::add)
        }
        viewModel.load()
        advanceUntilIdle()

        viewModel.requestDeleteUser(1)
        assertEquals(1L, viewModel.state.value.deleteConfirmation?.id)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.deleteConfirmation)
        assertTrue(viewModel.state.value.users.isEmpty())
        assertTrue(delete.deletedIds.isEmpty())
        assertEquals(
            listOf<UserFeedEvent>(UserFeedEvent.ShowUndoDelete(userId = 1, message = "User 1 deleted")),
            events.events.toList(),
        )
    }

    @Test
    fun `clear highlight removes added user highlight`() = runTest {
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
        pending.complete(AppResult.Success(feedUser(99)))
        advanceUntilIdle()

        viewModel.clearHighlight()

        assertEquals(null, viewModel.state.value.highlightedUserId)
    }

    @Test
    fun `requesting then cancelling delete leaves the feed untouched`() = runTest {
        val viewModel = loadedViewModel(listOf(1, 2, 3))

        viewModel.requestDeleteUser(2)
        assertEquals(2L, viewModel.state.value.deleteConfirmation?.id)

        viewModel.cancelDeleteUser()
        assertEquals(null, viewModel.state.value.deleteConfirmation)
        assertEquals(listOf(1L, 2L, 3L), viewModel.state.value.users.map { it.id })
    }

    @Test
    fun `confirming delete removes the row and emits an undo snackbar`() = runTest {
        val events = RecordedEvents()
        val viewModel = loadedViewModel(listOf(1, 2, 3), events = events)

        viewModel.requestDeleteUser(2)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(null, state.deleteConfirmation)
        assertEquals(listOf(1L, 3L), state.users.map { it.id })
        assertEquals(
            listOf<UserFeedEvent>(UserFeedEvent.ShowUndoDelete(userId = 2, message = "User 2 deleted")),
            events.events.toList(),
        )
    }

    @Test
    fun `undo restores the row at its original index without calling delete`() = runTest {
        val delete = FakeDeleteUserUseCase()
        val viewModel = loadedViewModel(listOf(1, 2, 3), deleteUser = delete)

        viewModel.requestDeleteUser(2)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()
        assertEquals(listOf(1L, 3L), viewModel.state.value.users.map { it.id })

        viewModel.undoDelete(2)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(1L, 2L, 3L), state.users.map { it.id })
        assertEquals(2L, state.highlightedUserId)
        assertTrue(delete.deletedIds.isEmpty())
    }

    @Test
    fun `committing a deletion calls delete once and keeps the row gone`() = runTest {
        val delete = FakeDeleteUserUseCase(AppResult.Success(Unit))
        val viewModel = loadedViewModel(listOf(1, 2, 3), deleteUser = delete)

        viewModel.requestDeleteUser(2)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()

        viewModel.commitDeletion(2)
        advanceUntilIdle()

        assertEquals(listOf(1L, 3L), viewModel.state.value.users.map { it.id })
        assertEquals(listOf(2L), delete.deletedIds)
    }

    @Test
    fun `commit failure restores the row at its index and emits a failure snackbar`() = runTest {
        val events = RecordedEvents()
        val delete = FakeDeleteUserUseCase(AppResult.Failure(AppError.Server(500)))
        val viewModel = loadedViewModel(listOf(1, 2, 3), deleteUser = delete, events = events)

        viewModel.requestDeleteUser(2)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()

        viewModel.commitDeletion(2)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(1L, 2L, 3L), state.users.map { it.id })
        assertEquals(2L, state.highlightedUserId)
        assertTrue(events.events.any { it is UserFeedEvent.ShowDeleteFailed && it.userId == 2L })
    }

    @Test
    fun `commit of a not-found user is treated as deleted and does not restore`() = runTest {
        val events = RecordedEvents()
        val delete = FakeDeleteUserUseCase(AppResult.Failure(AppError.NotFound))
        val viewModel = loadedViewModel(listOf(1, 2, 3), deleteUser = delete, events = events)

        viewModel.requestDeleteUser(2)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()

        viewModel.commitDeletion(2)
        advanceUntilIdle()

        assertEquals(listOf(1L, 3L), viewModel.state.value.users.map { it.id })
        assertFalse(events.events.any { it is UserFeedEvent.ShowDeleteFailed })
    }

    @Test
    fun `undo and commit after the window closed are no-ops`() = runTest {
        val delete = FakeDeleteUserUseCase(AppResult.Success(Unit))
        val viewModel = loadedViewModel(listOf(1, 2, 3), deleteUser = delete)

        viewModel.requestDeleteUser(2)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()
        viewModel.commitDeletion(2)
        advanceUntilIdle()
        assertEquals(listOf(2L), delete.deletedIds)

        // A late Undo tap after commit must not resurrect the row, and a second
        // commit must not call delete again.
        viewModel.undoDelete(2)
        viewModel.commitDeletion(2)
        advanceUntilIdle()

        assertEquals(listOf(1L, 3L), viewModel.state.value.users.map { it.id })
        assertEquals(listOf(2L), delete.deletedIds)
    }

    @Test
    fun `deleting the only user empties the feed and undo restores it`() = runTest {
        val viewModel = loadedViewModel(listOf(1))

        viewModel.requestDeleteUser(1)
        viewModel.confirmDeleteUser()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isEmpty)

        viewModel.undoDelete(1)
        advanceUntilIdle()
        assertEquals(listOf(1L), viewModel.state.value.users.map { it.id })
        assertFalse(viewModel.state.value.isEmpty)
    }

    private fun TestScope.loadedViewModel(
        ids: List<Long>,
        deleteUser: DeleteUserUseCase = FakeDeleteUserUseCase(),
        events: RecordedEvents? = null,
    ): UserFeedViewModel {
        val viewModel = viewModel(
            FakeLoadUserFeedUseCase(results = mutableListOf(AppResult.Success(feedResult(ids.map(::feedUser))))),
            deleteUser = deleteUser,
        )
        if (events != null) {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.collect(events.events::add)
            }
        }
        viewModel.load()
        advanceUntilIdle()
        return viewModel
    }

    private class RecordedEvents {
        val events = mutableListOf<UserFeedEvent>()
    }

    private fun TestScope.viewModel(
        loadUserFeedUseCase: LoadUserFeedUseCase,
        loadOlderUsers: LoadOlderUsersUseCase = FakeLoadOlderUsersUseCase(),
        createUser: CreateUserUseCase = FakeCreateUserUseCase(AppResult.Failure(AppError.Unauthorized)),
        deleteUser: DeleteUserUseCase = FakeDeleteUserUseCase(),
        nowMillis: Long = 1_000,
        connectivityMonitor: ConnectivityMonitor? = null,
    ): UserFeedViewModel = UserFeedViewModel(
        loadUserFeed = loadUserFeedUseCase,
        loadOlderUsers = loadOlderUsers,
        createUser = createUser,
        deleteUser = deleteUser,
        scope = if (connectivityMonitor == null) this else backgroundScope,
        clock = FixedClock(nowMillis),
        connectivityMonitor = connectivityMonitor,
    )

    private class FakeLoadUserFeedUseCase(
        private val results: MutableList<AppResult<UserFeedResult>> = mutableListOf(),
        private val pending: CompletableDeferred<AppResult<UserFeedResult>>? = null,
    ) : LoadUserFeedUseCase {
        override suspend fun invoke(): AppResult<UserFeedResult> =
            if (results.isNotEmpty()) results.removeAt(0) else pending!!.await()
    }

    private class FakeLoadOlderUsersUseCase(
        private val results: MutableMap<Int, AppResult<UserFeedResult>> = mutableMapOf(),
    ) : LoadOlderUsersUseCase {
        val requestedPages = mutableListOf<Int>()

        override suspend fun invoke(page: Int): AppResult<UserFeedResult> {
            requestedPages += page
            return results[page] ?: AppResult.Failure(AppError.NotFound)
        }
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

    private class FakeDeleteUserUseCase(
        private val result: AppResult<Unit> = AppResult.Success(Unit),
    ) : DeleteUserUseCase {
        val deletedIds = mutableListOf<Long>()

        override suspend fun invoke(id: Long): AppResult<Unit> {
            deletedIds += id
            return result
        }
    }

    private class FixedClock(private val nowMillis: Long) : AppClock {
        override fun nowMillis(): Long = nowMillis
    }

    private fun feedResult(
        users: List<UserFeedItem>,
        fromCache: Boolean = false,
        lastUpdatedMillis: Long? = users.firstOrNull()?.fetchedAtMillis,
        currentPage: Int = 1,
        totalPages: Int = 1,
        hasNextPage: Boolean = currentPage < totalPages,
    ) = UserFeedResult(
        users = users,
        fromCache = fromCache,
        lastUpdatedMillis = lastUpdatedMillis,
        currentPage = currentPage,
        totalPages = totalPages,
        hasNextPage = hasNextPage,
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
