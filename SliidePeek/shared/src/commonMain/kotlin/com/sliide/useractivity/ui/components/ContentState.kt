package com.sliide.useractivity.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed interface ContentState {
    data object Loading : ContentState
    data object Empty : ContentState
    data class Error(val message: String? = null) : ContentState
    data object Content : ContentState
}

@Composable
fun ContentStateContainer(
    state: ContentState,
    modifier: Modifier = Modifier,
    emptyTitle: String = "Nothing here yet",
    emptyBody: String = "Try changing filters or refreshing.",
    emptyActionLabel: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    errorTitle: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
    loadingRows: Int = 5,
    content: @Composable () -> Unit,
) {
    when (state) {
        ContentState.Loading -> Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(loadingRows) { SkeletonRow() }
        }

        ContentState.Empty -> StateMessage(
            modifier = modifier,
            title = emptyTitle,
            body = emptyBody,
            actionLabel = emptyActionLabel,
            onAction = onEmptyAction,
        )

        is ContentState.Error -> StateMessage(
            modifier = modifier,
            title = errorTitle,
            body = state.message ?: "Check your connection and try again. Nothing was lost.",
            symbol = "⚠",
            actionLabel = "Retry",
            onAction = onRetry,
            isError = true,
        )

        ContentState.Content -> content()
    }
}
