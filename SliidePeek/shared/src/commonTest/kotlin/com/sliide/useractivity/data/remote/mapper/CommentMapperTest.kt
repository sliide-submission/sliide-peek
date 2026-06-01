package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.CommentDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentMapperTest {
    @Test
    fun `maps comment dto to domain comment`() {
        val comment = CommentDTO(
            id = 11,
            postId = 10,
            name = "Reader",
            email = "reader@example.test",
            body = "Nice post",
        ).toDomain()

        assertEquals(11, comment.id)
        assertEquals(10, comment.postId)
        assertEquals("Reader", comment.name)
        assertEquals("reader@example.test", comment.email)
        assertEquals("Nice post", comment.body)
    }
}
