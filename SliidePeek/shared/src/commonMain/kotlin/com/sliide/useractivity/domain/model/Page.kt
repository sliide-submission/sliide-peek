package com.sliide.useractivity.domain.model

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
) {
    val hasNextPage: Boolean = page < totalPages
}
