package com.sliide.useractivity.domain

sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data class Validation(val message: String? = null) : AppError
    data class Server(val statusCode: Int) : AppError
    data class Unknown(val message: String? = null) : AppError
}
