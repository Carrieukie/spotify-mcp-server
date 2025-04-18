package mcpserver.spotify.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun getHttpClient(): HttpClient = HttpClient(CIO) {
    expectSuccess = true
    addDefaultResponseValidation()

    defaultRequest {
        contentType(ContentType.Application.Json)
        url {
            url { protocol = URLProtocol.HTTPS }
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }

    install(Logging) {
        level = LogLevel.ALL
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
