package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.TodoDTO
import com.sliide.useractivity.domain.model.TodoStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class TodoMapperTest {
    @Test
    fun `maps todo dto to domain todo`() {
        val todo = TodoDTO(
            id = 12,
            userId = 2,
            title = "Ship feature",
            dueOn = "2026-06-16T00:00:00.000+05:30",
            status = "completed",
        ).toDomain()

        assertEquals(12, todo.id)
        assertEquals(2, todo.userId)
        assertEquals("Ship feature", todo.title)
        assertEquals("2026-06-16T00:00:00.000+05:30", todo.dueOn)
        assertEquals(TodoStatus.Completed, todo.status)
    }

    @Test
    fun `maps unknown todo status safely`() {
        val todo = TodoDTO(
            id = 12,
            userId = 2,
            title = "Ship feature",
            status = "blocked",
        ).toDomain()

        assertEquals(TodoStatus.Unknown, todo.status)
    }
}
