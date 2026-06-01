package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.TodoDTO
import com.sliide.useractivity.domain.model.Todo
import com.sliide.useractivity.domain.model.TodoStatus

internal fun TodoDTO.toDomain(): Todo = Todo(
    id = id,
    userId = userId,
    title = title,
    dueOn = dueOn,
    status = status.toTodoStatus(),
)

private fun String.toTodoStatus(): TodoStatus = when (lowercase()) {
    "pending" -> TodoStatus.Pending
    "completed" -> TodoStatus.Completed
    else -> TodoStatus.Unknown
}
