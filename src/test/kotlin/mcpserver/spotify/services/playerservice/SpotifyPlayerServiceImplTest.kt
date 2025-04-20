package mcpserver.spotify.services.playerservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.playerservice.model.SpotifySearchResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class SpotifyPlayerServiceImplTest {

    // Mock implementation of SpotifyTokenManager for successful token retrieval
    private val successTokenManager = object : SpotifyTokenManager {
        override suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError> {
            return SpotifyResult.Success("mock-token")
        }
    }

    // Mock implementation of SpotifyTokenManager for failed token retrieval
    private val failureTokenManager = object : SpotifyTokenManager {
        override suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError> {
            return SpotifyResult.Failure(
                SpotifyAccountsError(
                    errorDescription = "Token retrieval failed",
                    error = "invalid_client",
                    status = 401
                )
            )
        }
    }


    @Test
    fun `pausePlayback should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])

                // Return a mock response
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.pausePlayback()

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `search should return success with search results when API call succeeds`() {
        runBlocking {
            // Arrange
            val mockSearchResponse = """
                {
                    "tracks": {
                        "items": [
                            {
                                "id": "track1",
                                "name": "Test Track 1"
                            },
                            {
                                "id": "track2",
                                "name": "Test Track 2"
                            }
                        ]
                    }
                }
            """.trimIndent()

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals("test query", request.url.parameters["q"])
                assertEquals("track", request.url.parameters["type"])

                // Return a mock response
                respond(
                    content = mockSearchResponse,
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.search("test query", "track")

            // Assert
            assertIs<SpotifyResult.Success<SpotifySearchResponse>>(result)
        }
    }

    @Test
    fun `skipToNextTrack should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Post, request.method)

                // Return a mock response
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.skipToNextTrack()

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `skipToPreviousTrack should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Post, request.method)

                // Return a mock response
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.skipToPreviousTrack()

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `seekToPosition should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val positionMs = 30000 // 30 seconds

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("$positionMs", request.url.parameters["position_ms"])

                // Return a mock response
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.seekToPosition(positionMs)

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `setVolume should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val volumePercent = 50

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("$volumePercent", request.url.parameters["volume_percent"])

                // Return a mock response
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.setVolume(volumePercent)

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `getQueue should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val mockQueueResponse = """
                {
                    "currently_playing": {
                        "id": "current-track",
                        "name": "Current Track"
                    },
                    "queue": [
                        {
                            "id": "next-track-1",
                            "name": "Next Track 1"
                        },
                        {
                            "id": "next-track-2",
                            "name": "Next Track 2"
                        }
                    ]
                }
            """.trimIndent()

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Get, request.method)

                // Return a mock response
                respond(
                    content = mockQueueResponse,
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.getQueue()

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `setRepeatMode should return success when API call succeeds`() {
        runBlocking {
            // Arrange
            val state = "track" // Options: track, context, off

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Put, request.method)
                assertEquals(state, request.url.parameters["state"])

                // Return a mock response
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
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.setRepeatMode(state)

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
        }
    }

    @Test
    fun `playTrack should return success when API call succeeds with empty track URIs`(): Unit = runBlocking {
        // Arrange
        val mockEngine = MockEngine { request ->
            // Verify request properties
            assertEquals("Bearer mock-token", request.headers["Authorization"])
            assertEquals(HttpMethod.Put, request.method)

            // Verify request body is empty (no track URIs)
            val requestBody = request.body.toByteArray().decodeToString()
            assert(requestBody.isEmpty()) { "Request body should be empty when no track URIs are provided" }

            // Return a mock response
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
            tokenManager = successTokenManager,
            client = mockClient
        )

        // Act
        val result = service.playTrack(listOf())

        // Assert
        assertIs<SpotifyResult.Success<String>>(result)
    }


    @Test
    fun `playTrack should return failure when token retrieval fails`() = runBlocking {
        // Arrange
        val trackUris = listOf("spotify:track:123456")

        val mockEngine = MockEngine { request ->
            // This should not be called because token retrieval fails
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
            tokenManager = failureTokenManager,
            client = mockClient
        )

        // Act
        val result = service.playTrack(trackUris)

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
        assertEquals(401, error.error?.status)
        assertEquals("Token retrieval failed", error.error?.message)
    }

    @Test
    fun `playPlaylist should return success when API call succeeds`(): Unit = runBlocking {
        // Arrange
        val playlistUri = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M"

        val mockEngine = MockEngine { request ->
            // Verify request properties
            assertEquals("Bearer mock-token", request.headers["Authorization"])
            assertEquals(HttpMethod.Put, request.method)

            // Verify request body contains the correct playlist URI
            val requestBody = request.body.toByteArray().decodeToString()
            assert(requestBody.contains("\"context_uri\":\"$playlistUri\"")) { 
                "Request body should contain the playlist URI: $requestBody" 
            }

            // Return a mock response
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
            tokenManager = successTokenManager,
            client = mockClient
        )

        // Act
        val result = service.playPlaylist(playlistUri)

        // Assert
        assertIs<SpotifyResult.Success<String>>(result)
    }

    @Test
    fun `playPlaylist should return failure when token retrieval fails`(): Unit = runBlocking {
        // Arrange
        val playlistUri = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M"

        val mockEngine = MockEngine { request ->
            // This should not be called because token retrieval fails
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
            tokenManager = failureTokenManager,
            client = mockClient
        )

        // Act
        val result = service.playPlaylist(playlistUri)

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
        assertEquals(401, error.error?.status)
        assertEquals("Token retrieval failed", error.error?.message)
    }
}
