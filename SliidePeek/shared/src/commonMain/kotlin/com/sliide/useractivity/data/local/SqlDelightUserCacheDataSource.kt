package com.sliide.useractivity.data.local

import com.sliide.useractivity.domain.model.Page
import com.sliide.useractivity.domain.model.User

class SqlDelightUserCacheDataSource(
    private val database: SliidePeekDatabase,
) : UserCacheDataSource {
    private val queries = database.userCacheQueries

    override suspend fun replaceCachedFeed(
        page: Page<User>,
        fetchedAtMillis: Long,
        cachedAtMillis: Long,
    ) {
        queries.transaction {
            queries.clearFeed()
            page.items.forEachIndexed { index, user ->
                queries.insertUser(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    gender = user.gender.cacheValue(),
                    status = user.status.cacheValue(),
                    fetched_at_millis = fetchedAtMillis,
                    cached_at_millis = cachedAtMillis,
                    position = index.toLong(),
                )
            }
            queries.replaceMetadata(
                cache_key = FEED_CACHE_KEY,
                page = page.page.toLong(),
                per_page = page.perPage.toLong(),
                total_pages = page.totalPages.toLong(),
                cached_at_millis = cachedAtMillis,
            )
        }
    }

    override suspend fun appendCachedPage(
        page: Page<User>,
        fetchedAtMillis: Long,
        cachedAtMillis: Long,
    ) {
        queries.transaction {
            val existingRows = queries.selectCachedFeed().executeAsList()
            val existingIds = existingRows.map { it.id }.toSet()
            var nextPosition = existingRows.size.toLong()
            page.items.forEach { user ->
                if (user.id !in existingIds) {
                    queries.insertUser(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        gender = user.gender.cacheValue(),
                        status = user.status.cacheValue(),
                        fetched_at_millis = fetchedAtMillis,
                        cached_at_millis = cachedAtMillis,
                        position = nextPosition,
                    )
                    nextPosition += 1
                }
            }
            val metadata = queries.selectMetadata(FEED_CACHE_KEY).executeAsOneOrNull()
            queries.replaceMetadata(
                cache_key = FEED_CACHE_KEY,
                page = page.page.toLong(),
                per_page = metadata?.per_page ?: page.perPage.toLong(),
                total_pages = page.totalPages.toLong(),
                cached_at_millis = cachedAtMillis,
            )
        }
    }

    override suspend fun getCachedFeed(): CachedUserFeed? {
        val metadata = queries.selectMetadata(FEED_CACHE_KEY).executeAsOneOrNull() ?: return null
        val rows = queries.selectCachedFeed().executeAsList()
        if (rows.isEmpty()) return null

        return CachedUserFeed(
            page = Page(
                items = rows.map(Cached_user_feed::toDomain),
                page = metadata.page.toInt(),
                perPage = metadata.per_page.toInt(),
                totalPages = metadata.total_pages.toInt(),
            ),
            cachedAtMillis = metadata.cached_at_millis,
            fetchedAtMillis = rows.first().fetched_at_millis,
        )
    }

    override suspend fun insertCreatedUserAtTop(
        user: User,
        createdAtMillis: Long,
        cachedAtMillis: Long,
    ) {
        queries.transaction {
            val metadata = queries.selectMetadata(FEED_CACHE_KEY).executeAsOneOrNull()
            queries.deleteUserById(user.id)
            queries.incrementFeedPositions()
            queries.insertUser(
                id = user.id,
                name = user.name,
                email = user.email,
                gender = user.gender.cacheValue(),
                status = user.status.cacheValue(),
                fetched_at_millis = createdAtMillis,
                cached_at_millis = cachedAtMillis,
                position = 0,
            )
            val perPage = metadata?.per_page ?: DEFAULT_LOCAL_PER_PAGE
            queries.trimFeedToLimit(perPage)
            queries.replaceMetadata(
                cache_key = FEED_CACHE_KEY,
                page = metadata?.page ?: 1,
                per_page = perPage,
                total_pages = metadata?.total_pages ?: 1,
                cached_at_millis = cachedAtMillis,
            )
        }
    }

    override suspend fun deleteUser(id: Long) {
        queries.deleteUserById(id)
    }

    private companion object {
        const val FEED_CACHE_KEY = "users:latest-feed"
        const val DEFAULT_LOCAL_PER_PAGE = 20L
    }
}
