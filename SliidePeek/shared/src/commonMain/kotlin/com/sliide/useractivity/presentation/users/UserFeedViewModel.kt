package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.RelativeTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserFeedViewModel(
    private val loadUserFeed: LoadUserFeedUseCase,
    private val scope: CoroutineScope,
    private val clock: AppClock,
    private val relativeTimeFormatter: RelativeTimeFormatter = RelativeTimeFormatter(),
) {
    private val _state = MutableStateFlow(UserFeedState())
    val state: StateFlow<UserFeedState> = _state.asStateFlow()

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

    private fun AppError.toUserMessage(): String = when (this) {
        AppError.Network -> "No internet connection. Try again when you are back online."
        AppError.Timeout -> "The connection timed out. Please try again."
        AppError.Unauthorized -> "This request is not authorised."
        AppError.NotFound -> "Users could not be found."
        is AppError.Validation -> message ?: "The request was not valid."
        is AppError.Server -> "The service is unavailable right now. Please try again."
        is AppError.Unknown -> message ?: "Something went wrong. Please try again."
    }
}
