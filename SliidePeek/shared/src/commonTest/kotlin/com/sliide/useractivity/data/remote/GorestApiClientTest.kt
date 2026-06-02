package com.sliide.useractivity.data.remote

import com.sliide.useractivity.data.remote.dto.CreateUserRequestDTO
import com.sliide.useractivity.test.Fixtures
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun `sends bearer token when provided for users page`() = runTest {
        var capturedAuthorization: String? = null
        val apiClient = GorestApiClient(
            httpClient = HttpClient(MockEngine) {
                expectSuccess = true
                engine {
                    addHandler { request ->
                        capturedAuthorization = request.headers[HttpHeaders.Authorization]
                        respond(Fixtures.USERS_PAGE_1, HttpStatusCode.OK, paginationHeaders())
                    }
                }
                install(ContentNegotiation) { json(gorestJson) }
            },
            baseUrl = "https://example.test/public/v2",
        )

        apiClient.getUsers(page = 1, perPage = 2, bearerToken = "test-token")

        assertEquals("Bearer test-token", capturedAuthorization)
    }

    @Test
    fun `omits bearer token when not provided for users page`() = runTest {
        var capturedAuthorization: String? = null
        val apiClient = GorestApiClient(
            httpClient = HttpClient(MockEngine) {
                expectSuccess = true
                engine {
                    addHandler { request ->
                        capturedAuthorization = request.headers[HttpHeaders.Authorization]
                        respond(Fixtures.USERS_PAGE_1, HttpStatusCode.OK, paginationHeaders())
                    }
                }
                install(ContentNegotiation) { json(gorestJson) }
            },
            baseUrl = "https://example.test/public/v2",
        )

        apiClient.getUsers(page = 1, perPage = 2)

        assertEquals(null, capturedAuthorization)
    }

    @Test
    fun `parses user detail`() = runTest {
        val apiClient = GorestApiClient(mockClient(Fixtures.USER_DETAIL), "https://example.test/public/v2")

        val user = apiClient.getUser(8484086)

        assertEquals(8484086, user.id)
        assertEquals("Bhargava Adiga", user.name)
    }

    @Test
    fun `creates user with bearer token and parses response`() = runTest {
        var capturedPath: String? = null
        var capturedAuthorization: String? = null
        var capturedBody: String? = null
        val apiClient = GorestApiClient(
            httpClient = HttpClient(MockEngine) {
                expectSuccess = true
                engine {
                    addHandler { request ->
                        capturedPath = request.url.encodedPath
                        capturedAuthorization = request.headers[HttpHeaders.Authorization]
                        capturedBody = request.body.toByteArray().decodeToString()
                        respond(
                            """{"id":99,"name":"Maya Reed","email":"maya.reed@example.com","gender":"female","status":"active"}""",
                            HttpStatusCode.Created,
                            jsonHeaders(),
                        )
                    }
                }
                install(ContentNegotiation) { json(gorestJson) }
            },
            baseUrl = "https://example.test/public/v2",
        )

        val user = apiClient.createUser(
            request = CreateUserRequestDTO(
                name = "Maya Reed",
                email = "maya.reed@example.com",
                gender = "female",
                status = "active",
            ),
            bearerToken = "test-token",
        )

        assertEquals(99, user.id)
        assertEquals("/public/v2/users", capturedPath)
        assertEquals("Bearer test-token", capturedAuthorization)
        assertTrue(capturedBody!!.contains("\"email\":\"maya.reed@example.com\""))
    }

    @Test
    fun `deletes user with bearer token`() = runTest {
        var capturedMethod: String? = null
        var capturedPath: String? = null
        var capturedAuthorization: String? = null
        val apiClient = GorestApiClient(
            httpClient = HttpClient(MockEngine) {
                expectSuccess = true
                engine {
                    addHandler { request ->
                        capturedMethod = request.method.value
                        capturedPath = request.url.encodedPath
                        capturedAuthorization = request.headers[HttpHeaders.Authorization]
                        respond("", HttpStatusCode.NoContent)
                    }
                }
                install(ContentNegotiation) { json(gorestJson) }
            },
            baseUrl = "https://example.test/public/v2",
        )

        apiClient.deleteUser(id = 99, bearerToken = "test-token")

        assertEquals("DELETE", capturedMethod)
        assertEquals("/public/v2/users/99", capturedPath)
        assertEquals("Bearer test-token", capturedAuthorization)
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

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private fun paginationHeaders() = headersOf(
        HttpHeaders.ContentType to listOf("application/json"),
        "x-pagination-page" to listOf("1"),
        "x-pagination-limit" to listOf("2"),
        "x-pagination-pages" to listOf("5"),
    )
}
