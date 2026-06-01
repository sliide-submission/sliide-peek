package com.sliide.useractivity.data.remote

import com.sliide.useractivity.test.Fixtures
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GorestApiClientTest {
    @Test
    fun `parses users page and pagination headers`() = runTest {
        val apiClient = GorestApiClient(
            httpClient = mockClient(Fixtures.USERS_PAGE_1, paginationHeaders()),
            baseUrl = "https://example.test/public/v2",
        )

        val page = apiClient.getUsers(page = 1, perPage = 2)

        assertEquals(2, page.items.size)
        assertEquals(8484086, page.items.first().id)
        assertEquals(1, page.page)
        assertEquals(2, page.perPage)
        assertEquals(5, page.totalPages)
    }

    @Test
    fun `parses user detail`() = runTest {
        val apiClient = GorestApiClient(mockClient(Fixtures.USER_DETAIL), "https://example.test/public/v2")

        val user = apiClient.getUser(8484086)

        assertEquals(8484086, user.id)
        assertEquals("Bhargava Adiga", user.name)
    }

    @Test
    fun `parses posts comments and todos`() = runTest {
        val apiClient = GorestApiClient(
            httpClient = routedMockClient(
                "/public/v2/users/8484086/posts" to Fixtures.USER_POSTS,
                "/public/v2/posts/282051" to Fixtures.POST_DETAIL,
                "/public/v2/posts/282051/comments" to Fixtures.POST_COMMENTS,
                "/public/v2/users/8484086/todos" to Fixtures.USER_TODOS,
            ),
            baseUrl = "https://example.test/public/v2",
        )

        assertEquals(282051, apiClient.getUserPosts(8484086).single().id)
        assertEquals(282051, apiClient.getPost(282051).id)
        assertEquals(189787, apiClient.getPostComments(282051).single().id)
        assertEquals(104820, apiClient.getUserTodos(8484086).single().id)
    }

    private fun mockClient(
        body: String,
        headers: io.ktor.http.Headers = jsonHeaders(),
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient = HttpClient(MockEngine) {
        expectSuccess = true
        engine {
            addHandler { respond(body, status, headers) }
        }
        install(ContentNegotiation) { json(gorestJson) }
    }

    private fun routedMockClient(vararg routes: Pair<String, String>): HttpClient = HttpClient(MockEngine) {
        expectSuccess = true
        engine {
            addHandler { request ->
                val body = routes.first { it.first == request.url.encodedPath }.second
                respond(body, HttpStatusCode.OK, jsonHeaders())
            }
        }
        install(ContentNegotiation) { json(gorestJson) }
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private fun paginationHeaders() = headersOf(
        HttpHeaders.ContentType to listOf("application/json"),
        "x-pagination-page" to listOf("1"),
        "x-pagination-limit" to listOf("2"),
        "x-pagination-pages" to listOf("5"),
    )
}
