package mcpserver.spotify.services.playlistservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.playlistservice.model.PlaylistItem
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyCreatePlaylistRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistResponse
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
    fun `getCurrentUserPlaylists should return success with playlist results when API call succeeds`() {
        runBlocking {
            // Arrange
            val mockPlaylistsResponse = """
                {
                    "href": "https://api.spotify.com/v1/me/playlists?offset=0&limit=20",
                    "limit": 20,
                    "next": null,
                    "offset": 0,
                    "previous": null,
                    "total": 2,
                    "items": [
                        {
                            "collaborative": false,
                            "description": "Test Playlist 1 Description",
                            "external_urls": {
                                "spotify": "https://open.spotify.com/playlist/1234567890"
                            },
                            "href": "https://api.spotify.com/v1/playlists/1234567890",
                            "id": "1234567890",
                            "images": [
                                {
                                    "url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228",
                                    "height": 300,
                                    "width": 300
                                }
                            ],
                            "name": "Test Playlist 1",
                            "owner": {
                                "external_urls": {
                                    "spotify": "https://open.spotify.com/user/testuser"
                                },
                                "href": "https://api.spotify.com/v1/users/testuser",
                                "id": "testuser",
                                "type": "user",
                                "uri": "spotify:user:testuser",
                                "display_name": "Test User"
                            },
                            "public": true,
                            "snapshot_id": "snapshot123",
                            "tracks": {
                                "href": "https://api.spotify.com/v1/playlists/1234567890/tracks",
                                "total": 10
                            },
                            "type": "playlist",
                            "uri": "spotify:playlist:1234567890"
                        },
                        {
                            "collaborative": true,
                            "description": "Test Playlist 2 Description",
                            "external_urls": {
                                "spotify": "https://open.spotify.com/playlist/0987654321"
                            },
                            "href": "https://api.spotify.com/v1/playlists/0987654321",
                            "id": "0987654321",
                            "images": [
                                {
                                    "url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228",
                                    "height": 300,
                                    "width": 300
                                }
                            ],
                            "name": "Test Playlist 2",
                            "owner": {
                                "external_urls": {
                                    "spotify": "https://open.spotify.com/user/testuser"
                                },
                                "href": "https://api.spotify.com/v1/users/testuser",
                                "id": "testuser",
                                "type": "user",
                                "uri": "spotify:user:testuser",
                                "display_name": "Test User"
                            },
                            "public": false,
                            "snapshot_id": "snapshot456",
                            "tracks": {
                                "href": "https://api.spotify.com/v1/playlists/0987654321/tracks",
                                "total": 5
                            },
                            "type": "playlist",
                            "uri": "spotify:playlist:0987654321"
                        }
                    ]
                }
            """.trimIndent()

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Get, request.method)
                assertEquals("20", request.url.parameters["limit"])
                assertEquals("0", request.url.parameters["offset"])

                // Return a mock response
                respond(
                    content = mockPlaylistsResponse,
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
            val result = service.getCurrentUserPlaylists(limit = 20, offset = 0)

            // Assert
            assertIs<SpotifyResult.Success<SpotifyPlaylistResponse>>(result)
            val playlists = (result as SpotifyResult.Success<SpotifyPlaylistResponse>).data
            assertEquals(2, playlists.total)
            assertEquals(20, playlists.limit)
            assertEquals(2, playlists.items?.size)
            assertEquals("Test Playlist 1", playlists.items?.get(0)?.name)
            assertEquals("Test Playlist 2", playlists.items?.get(1)?.name)
        }
    }

    @Test
    fun `getCurrentUserPlaylists should return failure when token retrieval fails`() {
        runBlocking {
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
            val result = service.getCurrentUserPlaylists()

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
            val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
            assertEquals("Token retrieval failed", error.error?.message)
            assertEquals(401, error.error?.status)
        }
    }

    @Test
    fun `getCurrentUserPlaylists should return failure when API call fails`() {
        runBlocking {
            // Arrange
            val mockEngine = MockEngine { request ->
                // Return an error response with a 400 Bad Request status
                respond(
                    content = """{"error":{"status":400,"message":"Bad request"}}""",
                    status = HttpStatusCode.BadRequest,
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
            val result = service.getCurrentUserPlaylists()

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
            val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
            assertEquals(400, error.error?.status)
        }
    }

    @Test
    fun `addPlaylistTracks should return success with snapshot ID when API call succeeds`() {
        runBlocking {
            // Arrange
            val playlistId = "3cEYpjA9oz9GiPac4AsH4n"
            val trackUris = listOf("spotify:track:4iV5W9uYEdYUVa79Axb7Rh", "spotify:track:1301WleyT98MSxVHPZCA6M")
            val position = 0
            val request = SpotifyAddTracksRequest(uris = trackUris, position = position)

            val mockResponse = """
                {
                    "snapshot_id": "JbtmHBDBAYu3/bt8BOXKjzKx3i0b6LCa/wVjyl6qQ2Yf6nFXkbmzuEa+ZI/U1yF+"
                }
            """.trimIndent()

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("https://api.spotify.com/v1/playlists/$playlistId/tracks", request.url.toString())

                // Return a mock response
                respond(
                    content = mockResponse,
                    status = HttpStatusCode.Created,
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
            val result = service.addPlaylistTracks(playlistId, request)

            // Assert
            assertIs<SpotifyResult.Success<SpotifyAddTracksResponse>>(result)
            val response = (result as SpotifyResult.Success<SpotifyAddTracksResponse>).data
            assertEquals("JbtmHBDBAYu3/bt8BOXKjzKx3i0b6LCa/wVjyl6qQ2Yf6nFXkbmzuEa+ZI/U1yF+", response.snapshotId)
        }
    }

    @Test
    fun `addPlaylistTracks should return failure when token retrieval fails`() {
        runBlocking {
            // Arrange
            val playlistId = "3cEYpjA9oz9GiPac4AsH4n"
            val trackUris = listOf("spotify:track:4iV5W9uYEdYUVa79Axb7Rh")
            val request = SpotifyAddTracksRequest(uris = trackUris)

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
            val result = service.addPlaylistTracks(playlistId, request)

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
            val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
            assertEquals("Token retrieval failed", error.error?.message)
            assertEquals(401, error.error?.status)
        }
    }

    @Test
    fun `addPlaylistTracks should return failure when API call fails`() {
        runBlocking {
            // Arrange
            val playlistId = "3cEYpjA9oz9GiPac4AsH4n"
            val trackUris = listOf("spotify:track:4iV5W9uYEdYUVa79Axb7Rh")
            val request = SpotifyAddTracksRequest(uris = trackUris)

            val mockEngine = MockEngine { request ->
                // Return an error response with a 403 Forbidden status
                respond(
                    content = """{"error":{"status":403,"message":"Forbidden"}}""",
                    status = HttpStatusCode.Forbidden,
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
            val result = service.addPlaylistTracks(playlistId, request)

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
            val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
            assertEquals(403, error.error?.status)
        }
    }

    @Test
    fun `createPlaylist should return success with playlist details when API call succeeds`() {
        runBlocking {
            // Arrange
            val userId = "smedjan"
            val request = SpotifyCreatePlaylistRequest(
                name = "New Playlist",
                description = "New playlist description",
                public = false
            )

            val mockResponse = """
                {
                    "collaborative": false,
                    "description": "New playlist description",
                    "external_urls": {
                        "spotify": "https://open.spotify.com/playlist/5678901234"
                    },
                    "href": "https://api.spotify.com/v1/playlists/5678901234",
                    "id": "5678901234",
                    "images": [
                        {
                            "url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228",
                            "height": 300,
                            "width": 300
                        }
                    ],
                    "name": "New Playlist",
                    "owner": {
                        "external_urls": {
                            "spotify": "https://open.spotify.com/user/smedjan"
                        },
                        "href": "https://api.spotify.com/v1/users/smedjan",
                        "id": "smedjan",
                        "type": "user",
                        "uri": "spotify:user:smedjan",
                        "display_name": "Smedjan"
                    },
                    "public": false,
                    "snapshot_id": "snapshot789",
                    "tracks": {
                        "href": "https://api.spotify.com/v1/playlists/5678901234/tracks",
                        "total": 0
                    },
                    "type": "playlist",
                    "uri": "spotify:playlist:5678901234"
                }
            """.trimIndent()

            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer mock-token", request.headers["Authorization"])
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("https://api.spotify.com/v1/users/$userId/playlists", request.url.toString())

                // Return a mock response
                respond(
                    content = mockResponse,
                    status = HttpStatusCode.Created,
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
            val result = service.createPlaylist(userId, request)

            // Assert
            assertIs<SpotifyResult.Success<PlaylistItem>>(result)
            val playlist = (result as SpotifyResult.Success<PlaylistItem>).data
            assertEquals("New Playlist", playlist.name)
            assertEquals("New playlist description", playlist.description)
            assertEquals(false, playlist.public)
            assertEquals("5678901234", playlist.id)
            assertEquals("smedjan", playlist.owner?.id)
        }
    }

    @Test
    fun `createPlaylist should return failure when token retrieval fails`() {
        runBlocking {
            // Arrange
            val userId = "smedjan"
            val request = SpotifyCreatePlaylistRequest(
                name = "New Playlist",
                description = "New playlist description",
                public = false
            )

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
            val result = service.createPlaylist(userId, request)

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
            val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
            assertEquals("Token retrieval failed", error.error?.message)
            assertEquals(401, error.error?.status)
        }
    }

    @Test
    fun `createPlaylist should return failure when API call fails`() {
        runBlocking {
            // Arrange
            val userId = "smedjan"
            val request = SpotifyCreatePlaylistRequest(
                name = "New Playlist",
                description = "New playlist description",
                public = false
            )

            val mockEngine = MockEngine { request ->
                // Return an error response with a 403 Forbidden status
                respond(
                    content = """{"error":{"status":403,"message":"Forbidden"}}""",
                    status = HttpStatusCode.Forbidden,
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
            val result = service.createPlaylist(userId, request)

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
            val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
            assertEquals(403, error.error?.status)
        }
    }
}
