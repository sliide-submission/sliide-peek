package com.sliide.useractivity.data

import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.createGorestHttpClient
import com.sliide.useractivity.data.repository.PostRepositoryImpl
import com.sliide.useractivity.data.repository.TodoRepositoryImpl
import com.sliide.useractivity.data.repository.UserRepositoryImpl
import com.sliide.useractivity.domain.repository.PostRepository
import com.sliide.useractivity.domain.repository.TodoRepository
import com.sliide.useractivity.domain.repository.UserRepository
import io.ktor.client.HttpClient

class AppDataContainer(
    private val httpClient: HttpClient = createGorestHttpClient(),
) {
    private val apiClient = GorestApiClient(httpClient)

    val userRepository: UserRepository = UserRepositoryImpl(apiClient)
    val postRepository: PostRepository = PostRepositoryImpl(apiClient)
    val todoRepository: TodoRepository = TodoRepositoryImpl(apiClient)

    fun close() {
        httpClient.close()
    }
}
