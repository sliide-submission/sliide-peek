package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddUserFormValidatorTest {
    private val validator = AddUserFormValidator()

    @Test
    fun `valid form enables submit`() {
        val state = validator.validate(
            AddUserFormState(
                name = "Maya Reed",
                email = "maya.reed@example.com",
                gender = UserGender.Female,
                status = UserStatus.Active,
            ),
        )

        assertTrue(state.isSubmitEnabled)
        assertEquals(null, state.nameError)
        assertEquals(null, state.emailError)
    }

    @Test
    fun `name is required`() {
        val state = validator.validate(AddUserFormState(name = " ", email = "maya@example.com"))

        assertFalse(state.isSubmitEnabled)
        assertEquals("Name is required", state.nameError)
    }

    @Test
    fun `email is required and must look valid`() {
        val missing = validator.validate(AddUserFormState(name = "Maya", email = ""))
        val invalid = validator.validate(AddUserFormState(name = "Maya", email = "maya@"))

        assertEquals("Email is required", missing.emailError)
        assertEquals("Enter a valid email address", invalid.emailError)
    }

    @Test
    fun `gender and status must be accepted gorest values`() {
        val state = validator.validate(
            AddUserFormState(
                name = "Maya Reed",
                email = "maya.reed@example.com",
                gender = UserGender.Unknown,
                status = UserStatus.Unknown,
            ),
        )

        assertFalse(state.isSubmitEnabled)
        assertEquals("Choose male or female", state.genderError)
        assertEquals("Choose active or inactive", state.statusError)
    }
}
