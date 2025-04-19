package mcpserver.spotify.services.playlistservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistsResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class SpotifyPlaylistServiceImplTest {

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
    fun `getUserPlaylists should return success when API call succeeds`() = runBlocking {
        // Arrange
        val mockEngine = MockEngine { request ->
            // Verify request properties
            assertEquals("Bearer mock-token", request.headers["Authorization"])
            assertEquals("20", request.url.parameters["limit"])
            assertEquals("0", request.url.parameters["offset"])
            assertEquals("/v1/users/smedjan/playlists", request.url.encodedPath)

            // Return a mock response
            respond(
                content = """
                    {
                        "href": "https://api.spotify.com/v1/users/smedjan/playlists?offset=0&limit=20",
                        "items": [
                            {
                                "collaborative": false,
                                "description": "Test playlist",
                                "external_urls": {
                                    "spotify": "https://open.spotify.com/playlist/123456"
                                },
                                "href": "https://api.spotify.com/v1/playlists/123456",
                                "id": "123456",
                                "images": [],
                                "name": "Test Playlist",
                                "owner": {
                                    "display_name": "Test User",
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/user/smedjan"
                                    },
                                    "href": "https://api.spotify.com/v1/users/smedjan",
                                    "id": "smedjan",
                                    "type": "user",
                                    "uri": "spotify:user:smedjan"
                                },
                                "public": true,
                                "snapshot_id": "abcdef",
                                "tracks": {
                                    "href": "https://api.spotify.com/v1/playlists/123456/tracks",
                                    "total": 10
                                },
                                "type": "playlist",
                                "uri": "spotify:playlist:123456"
                            }
                        ],
                        "limit": 20,
                        "next": null,
                        "offset": 0,
                        "previous": null,
                        "total": 1
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val service = SpotifyPlaylistServiceImpl(
            tokenManager = successTokenManager,
            client = mockClient
        )

        // Act
        val result = service.getUserPlaylists(userId = "smedjan")

        // Assert
        assertIs<SpotifyResult.Success<SpotifyPlaylistsResponse>>(result)
        assertEquals(1, result.data.total)
        assertEquals(1, result.data.items.size)
        assertEquals("Test Playlist", result.data.items[0].name)
        assertEquals("smedjan", result.data.items[0].owner.id)
    }

    @Test
    fun `getUserPlaylists should return failure when token retrieval fails`() = runBlocking {
        // Arrange
        val mockEngine = MockEngine { request ->
            // This should not be called
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

        val service = SpotifyPlaylistServiceImpl(
            tokenManager = failureTokenManager,
            client = mockClient
        )

        // Act
        val result = service.getUserPlaylists(userId = "smedjan")

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        val error = result.exception
        assertEquals("Failed to get access token: Token retrieval failed", error.error?.message)
        assertEquals(401, error.error?.status)
    }

    @Test
    fun `getUserPlaylists should return failure when API call fails`() {
        runBlocking {
            // Arrange
            val mockEngine = MockEngine { request ->
                // Return an error response with a 404 Not Found status
                respond(
                    content = """{"error":{"status":404,"message":"User not found"}}""",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            // Configure the client to throw exceptions for non-2xx responses
            val mockClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json()
                }
                expectSuccess = true // This makes the client throw exceptions for non-2xx responses
            }

            val service = SpotifyPlaylistServiceImpl(
                tokenManager = successTokenManager,
                client = mockClient
            )

            // Act
            val result = service.getUserPlaylists(userId = "nonexistentuser")

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        }
    }
}
