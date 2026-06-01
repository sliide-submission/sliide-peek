package com.sliide.useractivity.data.repository

import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.coroutines.CancellationException

internal suspend fun <T> repositoryCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: ResponseException) {
    AppResult.Failure(error.toAppError())
} catch (error: HttpRequestTimeoutException) {
    AppResult.Failure(AppError.Timeout)
} catch (error: IOException) {
    AppResult.Failure(AppError.Network)
} catch (error: Throwable) {
    AppResult.Failure(AppError.Unknown(error.message))
}

private fun ResponseException.toAppError(): AppError = when (val status = response.status) {
    HttpStatusCode.Unauthorized,
    HttpStatusCode.Forbidden,
    -> AppError.Unauthorized

    HttpStatusCode.NotFound -> AppError.NotFound
    HttpStatusCode.UnprocessableEntity -> AppError.Validation(message)
    else -> if (status.value >= 500) {
        AppError.Server(status.value)
    } else {
        AppError.Unknown("HTTP ${status.value}")
    }
}
