package com.sliide.useractivity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun createGorestHttpClient(): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json(gorestJson)
    }
    // Without an explicit timeout a stalled connection hangs forever; this also makes the
    // AppError.Timeout path reachable so slow networks surface a clear "try again" message.
    install(HttpTimeout) {
        requestTimeoutMillis = 20_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 20_000
    }
}

internal val gorestJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
