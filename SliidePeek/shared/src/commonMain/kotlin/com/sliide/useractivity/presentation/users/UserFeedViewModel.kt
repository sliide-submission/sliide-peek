package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.RelativeTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserFeedViewModel(
    private val loadUserFeed: LoadUserFeedUseCase,
    private val createUser: CreateUserUseCase,
    private val deleteUser: DeleteUserUseCase,
    private val scope: CoroutineScope,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
    private val addUserFormValidator: AddUserFormValidator = AddUserFormValidator(),
) {
    private val _state = MutableStateFlow(UserFeedState())
    val state: StateFlow<UserFeedState> = _state.asStateFlow()

    private val eventsChannel = Channel<UserFeedEvent>(capacity = Channel.BUFFERED)
    val events: Flow<UserFeedEvent> = eventsChannel.receiveAsFlow()

    private val pendingDeletions = mutableMapOf<Long, PendingDeletion>()

    fun load() {
        val current = _state.value
        if (current.isLoading || current.users.isNotEmpty()) return
        loadInternal(isRefresh = false)
    }

    fun refresh() {
        val current = _state.value
        if (current.isLoading || current.isRefreshing) return
        loadInternal(isRefresh = current.users.isNotEmpty())
    }

    fun retry() {
        val current = _state.value
        if (current.isLoading || current.isRefreshing) return
        loadInternal(isRefresh = current.users.isNotEmpty())
    }

    fun openAddUser() {
        _state.update { current ->
            current.copy(
                isAddUserVisible = true,
                addUserForm = addUserFormValidator.validate(current.addUserForm.copy(submitErrorMessage = null)),
            )
        }
    }

    fun dismissAddUser() {
        if (_state.value.addUserForm.isSubmitting) return
        _state.update { current ->
            current.copy(
                isAddUserVisible = false,
                addUserForm = AddUserFormState(),
            )
        }
    }

    fun onAddUserNameChanged(value: String) {
        updateAddUserForm { it.copy(name = value, nameTouched = true, submitErrorMessage = null) }
    }

    fun onAddUserEmailChanged(value: String) {
        updateAddUserForm { it.copy(email = value, emailTouched = true, submitErrorMessage = null) }
    }

    fun onAddUserGenderSelected(gender: UserGender) {
        updateAddUserForm { it.copy(gender = gender, submitErrorMessage = null) }
    }

    fun onAddUserStatusSelected(status: UserStatus) {
        updateAddUserForm { it.copy(status = status, submitErrorMessage = null) }
    }

    fun submitAddUser() {
        val form = addUserFormValidator.validate(
            _state.value.addUserForm.copy(nameTouched = true, emailTouched = true),
        )
        if (!form.isSubmitEnabled) {
            _state.update { it.copy(addUserForm = form) }
            return
        }

        scope.launch {
            _state.update { current ->
                current.copy(addUserForm = form.copy(isSubmitting = true, submitErrorMessage = null))
            }

            val request = CreateUserRequest(
                name = form.name.trim(),
                email = form.email.trim(),
                gender = form.gender,
                status = form.status,
            )
            when (val result = createUser(request)) {
                is AppResult.Success -> handleCreateUserSuccess(result.value)
                is AppResult.Failure -> _state.update { current ->
                    current.copy(
                        addUserForm = current.addUserForm.copy(
                            isSubmitting = false,
                            submitErrorMessage = result.error.toCreateUserMessage(),
                        ),
                    )
                }
            }
        }
    }

    fun requestDeleteUser(id: Long) {
        val user = _state.value.users.firstOrNull { it.id == id } ?: return
        _state.update { it.copy(deleteConfirmation = user) }
    }

    fun clearHighlight() {
        _state.update { it.copy(highlightedUserId = null) }
    }

    fun cancelDeleteUser() {
        _state.update { it.copy(deleteConfirmation = null) }
    }

    fun confirmDeleteUser() {
        val current = _state.value
        val user = current.deleteConfirmation ?: return
        _state.update { it.copy(deleteConfirmation = null) }
        if (current.isOffline) {
            scope.launch {
                eventsChannel.send(UserFeedEvent.ShowMessage("You’re offline. Reconnect before deleting users."))
            }
            return
        }
        beginOptimisticDelete(user)
    }

    fun undoDelete(id: Long) {
        val pending = pendingDeletions.remove(id) ?: return
        _state.update { current ->
            val index = pending.index.coerceIn(0, current.users.size)
            current.copy(
                users = current.users.toMutableList().apply { add(index, pending.item) },
                highlightedUserId = pending.item.id,
            )
        }
    }

    fun commitDeletion(id: Long) {
        val pending = pendingDeletions[id] ?: return
        scope.launch {
            val result = deleteUser(id)
            pendingDeletions.remove(id)
            // A 404 means the user is already gone server-side, so the delete goal is met:
            // treat it as committed rather than restoring a user that no longer exists.
            if (result is AppResult.Failure && result.error != AppError.NotFound) {
                restorePendingUser(pending)
                eventsChannel.send(
                    UserFeedEvent.ShowDeleteFailed(
                        userId = id,
                        message = result.error.toDeleteUserMessage(),
                    ),
                )
            }
        }
    }

    fun retryDelete(id: Long) {
        val user = _state.value.users.firstOrNull { it.id == id } ?: return
        beginOptimisticDelete(user)
    }

    private fun beginOptimisticDelete(user: UserFeedItem) {
        val index = _state.value.users.indexOfFirst { it.id == user.id }
        if (index < 0) return
        pendingDeletions[user.id] = PendingDeletion(item = user, index = index)
        _state.update { current ->
            current.copy(
                users = current.users.filterNot { it.id == user.id },
                highlightedUserId = if (current.highlightedUserId == user.id) null else current.highlightedUserId,
            )
        }
        scope.launch {
            eventsChannel.send(UserFeedEvent.ShowUndoDelete(userId = user.id, message = "${user.name} deleted"))
        }
    }

    private fun restorePendingUser(pending: PendingDeletion) {
        _state.update { current ->
            if (current.users.any { it.id == pending.item.id }) return@update current
            val index = pending.index.coerceIn(0, current.users.size)
            current.copy(
                users = current.users.toMutableList().apply { add(index, pending.item) },
                highlightedUserId = pending.item.id,
            )
        }
    }

    private fun updateAddUserForm(transform: (AddUserFormState) -> AddUserFormState) {
        _state.update { current ->
            current.copy(addUserForm = addUserFormValidator.validate(transform(current.addUserForm)))
        }
    }

    private suspend fun handleCreateUserSuccess(user: UserFeedItem) {
        _state.update { current ->
            current.copy(
                users = listOf(user) + current.users.filterNot { it.id == user.id },
                isAddUserVisible = false,
                addUserForm = AddUserFormState(),
                highlightedUserId = user.id,
                errorMessage = null,
                offlineMessage = null,
            )
        }
        eventsChannel.send(UserFeedEvent.ShowMessage("${user.name} added"))
    }

    private fun loadInternal(isRefresh: Boolean) {
        scope.launch {
            _state.update { current ->
                current.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null,
                    offlineMessage = null,
                    canRetry = false,
                )
            }

            when (val result = loadUserFeed()) {
                is AppResult.Success -> _state.update { current ->
                    val feed = result.value
                    current.copy(
                        isLoading = false,
                        isRefreshing = false,
                        users = feed.users,
                        highlightedUserId = null,
                        errorMessage = null,
                        offlineMessage = if (feed.fromCache) "Offline — showing cached users" else null,
                        lastUpdatedLabel = feed.lastUpdatedMillis?.let(::formatLastUpdated),
                        canRetry = feed.fromCache,
                    )
                }

                is AppResult.Failure -> _state.update { current ->
                    val message = result.error.toUserMessage()
                    val offline = result.error.isOfflineError()
                    current.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = if (offline) null else message,
                        offlineMessage = if (offline) message else null,
                        canRetry = true,
                    )
                }
            }
        }
    }

    private fun formatLastUpdated(lastUpdatedMillis: Long): String = relativeTimeFormatter.format(
        thenMillis = lastUpdatedMillis,
        nowMillis = clock.nowMillis(),
    )

    private fun AppError.isOfflineError(): Boolean = this is AppError.Network || this is AppError.Timeout

    private fun AppError.toCreateUserMessage(): String = when (this) {
        AppError.Network -> "No internet connection. Try again when you are back online."
        AppError.Timeout -> "The connection timed out. Please try again."
        AppError.Unauthorized -> "GoREST API token required to add users. Add it locally and rebuild."
        AppError.NotFound -> "Users could not be found."
        is AppError.Validation -> message ?: "Check the form details and try again."
        is AppError.Server -> "The service is unavailable right now. Please try again."
        is AppError.Unknown -> message ?: "Couldn’t add user. Please try again."
    }

    private fun AppError.toDeleteUserMessage(): String = when (this) {
        AppError.Network -> "Couldn’t delete — you’re offline. The user was restored."
        AppError.Timeout -> "Couldn’t delete — the connection timed out. The user was restored."
        AppError.Unauthorized -> "GoREST API token required to delete users. The user was restored."
        AppError.NotFound -> "That user no longer exists."
        is AppError.Validation -> message ?: "Couldn’t delete — the user was restored."
        is AppError.Server -> "Couldn’t delete — the service is unavailable. The user was restored."
        is AppError.Unknown -> message ?: "Couldn’t delete — the user was restored."
    }

    private fun AppError.toUserMessage(): String = when (this) {
        AppError.Network -> "No internet connection. Try again when you are back online."
        AppError.Timeout -> "The connection timed out. Please try again."
        AppError.Unauthorized -> "This request is not authorised."
        AppError.NotFound -> "Users could not be found."
        is AppError.Validation -> message ?: "The request was not valid."
        is AppError.Server -> "The service is unavailable right now. Please try again."
        is AppError.Unknown -> message ?: "Something went wrong. Please try again."
    }

    private data class PendingDeletion(
        val item: UserFeedItem,
        val index: Int,
    )
}
