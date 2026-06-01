package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.PostDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class PostMapperTest {
    @Test
    fun `maps post dto to domain post`() {
        val post = PostDTO(
            id = 10,
            userId = 2,
            title = "Post title",
            body = "Post body",
        ).toDomain()

        assertEquals(10, post.id)
        assertEquals(2, post.userId)
        assertEquals("Post title", post.title)
        assertEquals("Post body", post.body)
    }
}
