package com.sliide.useractivity.presentation.users

import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus

data class AddUserFormState(
    val name: String = "",
    val email: String = "",
    val gender: UserGender = UserGender.Female,
    val status: UserStatus = UserStatus.Active,
    val nameError: String? = null,
    val emailError: String? = null,
    val genderError: String? = null,
    val statusError: String? = null,
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null,
) {
    val isSubmitEnabled: Boolean = !isSubmitting &&
        nameError == null &&
        emailError == null &&
        genderError == null &&
        statusError == null &&
        name.isNotBlank() &&
        email.isNotBlank()
}
