package com.sliide.useractivity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun createGorestHttpClient(): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json(gorestJson)
    }
}

internal val gorestJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
