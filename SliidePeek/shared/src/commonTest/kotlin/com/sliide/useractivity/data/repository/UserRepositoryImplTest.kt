package com.sliide.useractivity.data.repository

import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.gorestJson
import com.sliide.useractivity.domain.AppError
import com.sliide.useractivity.domain.AppResult
import com.sliide.useractivity.domain.auth.BearerTokenProvider
import com.sliide.useractivity.domain.model.CreateUserRequest
import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus
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
import kotlin.test.assertIs

class UserRepositoryImplTest {
    @Test
    fun `returns domain users on success`() = runTest {
        val repository = UserRepositoryImpl(
            GorestApiClient(mockClient(Fixtures.USERS_PAGE_1), "https://example.test/public/v2"),
            FakeTokenProvider("token"),
        )

        val result = repository.getUsers(page = 1, perPage = 2)

        val page = assertIs<AppResult.Success<*>>(result).value as com.sliide.useractivity.domain.model.Page<*>
        assertEquals(2, page.items.size)
        assertEquals(UserStatus.Active, (page.items.first() as com.sliide.useractivity.domain.model.User).status)
    }

    @Test
    fun `creates user on success`() = runTest {
        val repository = UserRepositoryImpl(
            GorestApiClient(
                mockClient("""{"id":99,"name":"Maya Reed","email":"maya.reed@example.com","gender":"female","status":"active"}""", status = HttpStatusCode.Created),
                "https://example.test/public/v2",
            ),
            FakeTokenProvider("token"),
        )

        val result = repository.createUser(
            CreateUserRequest(
                name = "Maya Reed",
                email = "maya.reed@example.com",
                gender = UserGender.Female,
                status = UserStatus.Active,
            ),
        )

        val user = assertIs<AppResult.Success<*>>(result).value as com.sliide.useractivity.domain.model.User
        assertEquals(99, user.id)
        assertEquals(UserGender.Female, user.gender)
    }

    @Test
    fun `missing token returns unauthorized without network request`() = runTest {
        var requestCount = 0
        val repository = UserRepositoryImpl(
            GorestApiClient(
                mockClient("{}", onRequest = { requestCount++ }),
                "https://example.test/public/v2",
            ),
            FakeTokenProvider(null),
        )

        val result = repository.createUser(
            CreateUserRequest(
                name = "Maya Reed",
                email = "maya.reed@example.com",
                gender = UserGender.Female,
                status = UserStatus.Active,
            ),
        )

        assertEquals(AppError.Unauthorized, assertIs<AppResult.Failure>(result).error)
        assertEquals(0, requestCount)
    }

    @Test
    fun `maps 404 response to not found`() = runTest {
        val repository = UserRepositoryImpl(
            GorestApiClient(mockClient("""{"message":"Not Found"}""", status = HttpStatusCode.NotFound), "https://example.test/public/v2"),
            FakeTokenProvider("token"),
        )

        val result = repository.getUser(99)

        assertEquals(AppError.NotFound, assertIs<AppResult.Failure>(result).error)
    }

    @Test
    fun `maps 422 response to validation`() = runTest {
        val repository = UserRepositoryImpl(
            GorestApiClient(mockClient("""[{"field":"email","message":"is invalid"}]""", status = HttpStatusCode.UnprocessableEntity), "https://example.test/public/v2"),
            FakeTokenProvider("token"),
        )

        val result = repository.createUser(
            CreateUserRequest(
                name = "Maya Reed",
                email = "invalid",
                gender = UserGender.Female,
                status = UserStatus.Active,
            ),
        )

        assertIs<AppError.Validation>(assertIs<AppResult.Failure>(result).error)
    }

    private fun mockClient(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: () -> Unit = {},
    ): HttpClient = HttpClient(MockEngine) {
        expectSuccess = true
        engine {
            addHandler {
                onRequest()
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        install(ContentNegotiation) { json(gorestJson) }
    }

    private class FakeTokenProvider(private val token: String?) : BearerTokenProvider {
        override fun getToken(): String? = token
    }
}
