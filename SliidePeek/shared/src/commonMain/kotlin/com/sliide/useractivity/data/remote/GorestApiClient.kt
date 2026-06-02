package com.sliide.useractivity.data.remote

import com.sliide.useractivity.data.remote.dto.CreateUserRequestDTO
import com.sliide.useractivity.data.remote.dto.UserDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class GorestApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun getUsers(page: Int, perPage: Int, bearerToken: String? = null): GorestPage<UserDTO> {
        val response = httpClient.get(baseUrl) {
            url { appendPathSegments("users") }
            parameter("page", page)
            parameter("per_page", perPage)
            bearerToken?.let { token -> header(HttpHeaders.Authorization, "Bearer $token") }
        }
        val items: List<UserDTO> = response.body()
        return GorestPage(
            items = items,
            page = response.paginationHeader("page") ?: page,
            perPage = response.paginationHeader("limit") ?: perPage,
            // When the pagination header is missing, infer "there is more" from a full page so
            // we don't silently disable paging instead of defaulting totalPages to the current page.
            totalPages = response.paginationHeader("pages")
                ?: if (items.size >= perPage) page + 1 else page,
        )
    }

    suspend fun getUser(id: Long): UserDTO = httpClient.get(baseUrl) {
        url { appendPathSegments("users", id.toString()) }
    }.body()

    suspend fun createUser(request: CreateUserRequestDTO, bearerToken: String): UserDTO = httpClient.post(baseUrl) {
        url { appendPathSegments("users") }
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $bearerToken")
        setBody(request)
    }.body()

    suspend fun deleteUser(id: Long, bearerToken: String) {
        httpClient.delete(baseUrl) {
            url { appendPathSegments("users", id.toString()) }
            header(HttpHeaders.Authorization, "Bearer $bearerToken")
        }
    }

    // Ktor headers are case-insensitive, so a single lookup covers both header casings GoREST uses.
    private fun HttpResponse.paginationHeader(name: String): Int? =
        headers["x-pagination-$name"]?.toIntOrNull()

    companion object {
        const val DEFAULT_BASE_URL = "https://gorest.co.in/public/v2"
    }
}

internal data class GorestPage<T>(
    val items: List<T>,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
)
