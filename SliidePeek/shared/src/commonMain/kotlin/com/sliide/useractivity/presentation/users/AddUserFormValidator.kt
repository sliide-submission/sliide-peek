package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus

class AddUserFormValidator {
    fun validate(state: AddUserFormState): AddUserFormState = state.copy(
        nameError = validateName(state.name),
        emailError = validateEmail(state.email),
        genderError = validateGender(state.gender),
        statusError = validateStatus(state.status),
    )

    private fun validateName(name: String): String? =
        if (name.trim().isEmpty()) "Name is required" else null

    private fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email is required"
            !EmailRegex.matches(trimmed) -> "Enter a valid email address"
            else -> null
        }
    }

    private fun validateGender(gender: UserGender): String? = when (gender) {
        UserGender.Female,
        UserGender.Male,
        -> null
        UserGender.Unknown -> "Choose male or female"
    }

    private fun validateStatus(status: UserStatus): String? = when (status) {
        UserStatus.Active,
        UserStatus.Inactive,
        -> null
        UserStatus.Unknown -> "Choose active or inactive"
    }

    private companion object {
        val EmailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
