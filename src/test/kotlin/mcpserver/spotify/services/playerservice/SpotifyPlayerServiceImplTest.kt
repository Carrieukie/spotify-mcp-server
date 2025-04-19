package mcpserver.spotify.services.playerservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

class SpotifyPlayerServiceImplTest {

    @Test
    fun `playTrack should return success when API call succeeds`() = runBlocking {
        // Arrange
        val mockTokenManager = object : SpotifyTokenManager {
            override suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError> {
                return SpotifyResult.Success("mock-token")
            }
        }

        val mockEngine = MockEngine { request ->
            // Verify request
            assertEquals("Bearer mock-token", request.headers["Authorization"])
            // Content-Type is only set when there's a body, so we don't check it here

            // Return success response
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val service = SpotifyPlayerServiceImpl(
            tokenManager = mockTokenManager,
            client = mockClient
        )

        // Act
        val result = service.playTrack(listOf("spotify:track:123456"))

        // Assert
        assertIs<SpotifyResult.Success<String>>(result)
        println("[DEBUG_LOG] Test passed: playTrack returns success when API call succeeds")
    }

    @Test
    fun `playTrack should return failure when token manager fails`() = runBlocking {
        // Arrange
        val mockTokenManager = object : SpotifyTokenManager {
            override suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError> {
                return SpotifyResult.Failure(
                    SpotifyAccountsError(
                        errorDescription = "Invalid token",
                        error = "invalid_token",
                        status = 401
                    )
                )
            }
        }

        // We don't expect the mock engine to be called in this test
        // since the token manager should fail first
        val mockEngine = MockEngine { request ->
            // If this is called, the test will fail in the assertions
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }

        val mockClient = HttpClient(mockEngine)
        val service = SpotifyPlayerServiceImpl(
            tokenManager = mockTokenManager,
            client = mockClient
        )

        // Act
        val result = service.playTrack(listOf("spotify:track:123456"))

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        assertEquals("Invalid token", result.exception.error?.message)
        println("[DEBUG_LOG] Test passed: playTrack returns failure when token manager fails")
    }
}
