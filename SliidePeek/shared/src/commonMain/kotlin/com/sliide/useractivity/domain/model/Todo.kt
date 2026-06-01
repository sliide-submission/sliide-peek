package com.sliide.useractivity.domain.model

data class Todo(
    val id: Long,
    val userId: Long,
    val title: String,
    val dueOn: String?,
    val status: TodoStatus,
)

enum class TodoStatus {
    Pending,
    Completed,
    Unknown,
}
