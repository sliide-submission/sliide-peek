package com.sliide.useractivity.domain.auth

interface BearerTokenProvider {
    fun getToken(): String?
}
