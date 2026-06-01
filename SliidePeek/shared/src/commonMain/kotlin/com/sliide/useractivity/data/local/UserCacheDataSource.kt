package com.sliide.useractivity.data.local

import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.User

interface UserCacheDataSource {
    suspend fun replaceLastPage(
        page: Page<User>,
        fetchedAtMillis: Long,
        cachedAtMillis: Long,
    )

    suspend fun getLastPageFeed(): CachedUserFeed?

    suspend fun insertCreatedUserAtTop(
        user: User,
        createdAtMillis: Long,
        cachedAtMillis: Long,
    )

    suspend fun deleteUser(id: Long)
}
