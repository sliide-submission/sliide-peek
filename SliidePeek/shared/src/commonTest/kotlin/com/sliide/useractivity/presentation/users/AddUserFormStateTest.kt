package com.sliide.useractivity.presentation.users

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AddUserFormStateTest {

    @Test
    fun field_errors_are_hidden_until_the_field_is_touched() {
        val state = AddUserFormState(
            nameError = "Name is required",
            emailError = "Email is required",
            nameTouched = false,
            emailTouched = false,
        )
        assertNull(state.visibleNameError, "name error must not show before interaction")
        assertNull(state.visibleEmailError, "email error must not show before interaction")
    }

    @Test
    fun field_error_shows_once_that_field_is_touched() {
        val state = AddUserFormState(
            nameError = "Name is required",
            emailError = "Email is required",
            nameTouched = true,
            emailTouched = false,
        )
        assertEquals("Name is required", state.visibleNameError)
        assertNull(state.visibleEmailError, "email stays hidden until its own first keystroke")
    }

    @Test
    fun blank_required_fields_keep_submit_disabled() {
        assertFalse(AddUserFormState().isSubmitEnabled)
    }
}
