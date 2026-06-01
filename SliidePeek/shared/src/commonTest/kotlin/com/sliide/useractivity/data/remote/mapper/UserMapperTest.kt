package com.sliide.useractivity.data.remote.mapper

import com.sliide.useractivity.data.remote.dto.UserDTO
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class UserMapperTest {
    @Test
    fun `maps user dto to domain user`() {
        val user = UserDTO(
            id = 1,
            name = "Alice Example",
            email = "alice@example.test",
            gender = "female",
            status = "active",
        ).toDomain()

        assertEquals(1, user.id)
        assertEquals("Alice Example", user.name)
        assertEquals(UserGender.Female, user.gender)
        assertEquals(UserStatus.Active, user.status)
    }

    @Test
    fun `maps unknown user enum values safely`() {
        val user = UserDTO(
            id = 1,
            name = "Alex Example",
            email = "alex@example.test",
            gender = "other",
            status = "archived",
        ).toDomain()

        assertEquals(UserGender.Unknown, user.gender)
        assertEquals(UserStatus.Unknown, user.status)
    }
}
