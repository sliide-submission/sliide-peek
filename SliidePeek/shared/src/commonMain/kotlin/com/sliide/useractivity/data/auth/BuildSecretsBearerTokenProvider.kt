package com.sliide.useractivity.data.auth

import com.sliide.useractivity.config.BuildSecrets
import com.sliide.useractivity.domain.auth.BearerTokenProvider

class BuildSecretsBearerTokenProvider : BearerTokenProvider {
    override fun getToken(): String? = BuildSecrets.gorestToken.trim().ifBlank { null }
}
