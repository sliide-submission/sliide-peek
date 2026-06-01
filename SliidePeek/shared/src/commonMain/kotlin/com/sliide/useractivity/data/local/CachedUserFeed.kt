package com.sliide.useractivity.data.local

import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.User

data class CachedUserFeed(
    val page: Page<User>,
    val cachedAtMillis: Long,
    val fetchedAtMillis: Long,
)
