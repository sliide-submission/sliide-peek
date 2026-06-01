package com.sliide.useractivity.data.remote

import com.sliide.useractivity.data.remote.dto.CommentDTO
import com.sliide.useractivity.data.remote.dto.CreateUserRequestDTO
import com.sliide.useractivity.data.remote.dto.PostDTO
import com.sliide.useractivity.data.remote.dto.TodoDTO
import com.sliide.useractivity.data.remote.dto.UserDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
    suspend fun getUsers(page: Int, perPage: Int): GorestPage<UserDTO> {
        val response = httpClient.get(baseUrl) {
            url { appendPathSegments("users") }
            parameter("page", page)
            parameter("per_page", perPage)
        }
        return GorestPage(
            items = response.body(),
            page = response.paginationHeader("page") ?: page,
            perPage = response.paginationHeader("limit") ?: perPage,
            totalPages = response.paginationHeader("pages") ?: page,
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

    suspend fun getUserPosts(userId: Long): List<PostDTO> = httpClient.get(baseUrl) {
        url { appendPathSegments("users", userId.toString(), "posts") }
    }.body()

    suspend fun getUserTodos(userId: Long): List<TodoDTO> = httpClient.get(baseUrl) {
        url { appendPathSegments("users", userId.toString(), "todos") }
    }.body()

    suspend fun getPost(id: Long): PostDTO = httpClient.get(baseUrl) {
        url { appendPathSegments("posts", id.toString()) }
    }.body()

    suspend fun getPostComments(postId: Long): List<CommentDTO> = httpClient.get(baseUrl) {
        url { appendPathSegments("posts", postId.toString(), "comments") }
    }.body()

    private fun HttpResponse.paginationHeader(name: String): Int? =
        headers["x-pagination-$name"]?.toIntOrNull()
            ?: headers["X-Pagination-${name.replaceFirstChar { it.uppercase() }}"]?.toIntOrNull()

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
