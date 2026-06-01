package com.sliide.useractivity.data.repository

import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.gorestJson
import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.test.Fixtures
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PostRepositoryImplTest {
    @Test
    fun `returns comments on success`() = runTest {
        val repository = PostRepositoryImpl(
            GorestApiClient(mockClient(Fixtures.POST_COMMENTS), "https://example.test/public/v2"),
        )

        val result = repository.getPostComments(282051)

        val comments = assertIs<AppResult.Success<*>>(result).value as List<*>
        assertEquals(1, comments.size)
    }

    @Test
    fun `maps network exception to network error`() = runTest {
        val httpClient = HttpClient(MockEngine) {
            expectSuccess = true
            engine { addHandler { throw IOException("offline") } }
            install(ContentNegotiation) { json(gorestJson) }
        }
        val repository = PostRepositoryImpl(GorestApiClient(httpClient, "https://example.test/public/v2"))

        val result = repository.getPost(282051)

        assertEquals(AppError.Network, assertIs<AppResult.Failure>(result).error)
    }

    private fun mockClient(body: String): HttpClient = HttpClient(MockEngine) {
        expectSuccess = true
        engine {
            addHandler {
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        install(ContentNegotiation) { json(gorestJson) }
    }
}
