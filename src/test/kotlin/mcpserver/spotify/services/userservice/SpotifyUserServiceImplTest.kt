package mcpserver.spotify.services.userservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.userservice.model.SpotifyUserProfile
import mcpserver.spotify.services.userservice.storage.UserProfileStorage
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class SpotifyUserServiceImplTest {

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

    // Mock implementation of UserProfileStorage
    private class MockUserProfileStorage : UserProfileStorage {
        var storedProfile: SpotifyUserProfile? = null

        override fun saveUserProfile(profile: SpotifyUserProfile) {
            storedProfile = profile
        }

        override fun getUserProfile(): SpotifyUserProfile? {
            return storedProfile
        }
    }

    private lateinit var mockStorage: MockUserProfileStorage

    @BeforeEach
    fun setup() {
        mockStorage = MockUserProfileStorage()
    }

    @Test
    fun `getCurrentUserProfile should return cached profile when available`() = runBlocking {
        // Arrange
        val cachedProfile = SpotifyUserProfile(
            displayName = "Test User",
            userId = "test-user-id"
        )
        mockStorage.saveUserProfile(cachedProfile)

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

        val service = SpotifyUserServiceImpl(
            tokenManager = successTokenManager,
            client = mockClient,
            storage = mockStorage
        )

        // Act
        val result = service.getCurrentUserProfile()

        // Assert
        assertIs<SpotifyResult.Success<SpotifyUserProfile>>(result)
        assertEquals(cachedProfile, result.data)
    }

    @Test
    fun `getCurrentUserProfile should fetch from API when cache is empty`() = runBlocking {
        // Arrange
        val mockEngine = MockEngine { request ->
            // Verify request properties
            assertEquals("Bearer mock-token", request.headers["Authorization"])

            // Return a mock response
            respond(
                content = """
                    {
                        "display_name": "API User",
                        "id": "api-user-id",
                        "external_urls": {"spotify": "https://open.spotify.com/user/api-user-id"},
                        "followers": {"href": null, "total": 7},
                        "href": "https://api.spotify.com/v1/users/api-user-id",
                        "images": [],
                        "type": "user",
                        "uri": "spotify:user:api-user-id"
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

        val service = SpotifyUserServiceImpl(
            tokenManager = successTokenManager,
            client = mockClient,
            storage = mockStorage
        )

        // Act
        val result = service.getCurrentUserProfile()

        // Assert
        assertIs<SpotifyResult.Success<SpotifyUserProfile>>(result)
        assertEquals("API User", result.data.displayName)
        assertEquals("api-user-id", result.data.userId)
        
        // Verify the profile was stored
        assertEquals("API User", mockStorage.storedProfile?.displayName)
        assertEquals("api-user-id", mockStorage.storedProfile?.userId)
    }

    @Test
    fun `refreshUserProfile should always fetch from API even when cache is available`() = runBlocking {
        // Arrange
        val cachedProfile = SpotifyUserProfile(
            displayName = "Cached User",
            userId = "cached-user-id"
        )
        mockStorage.saveUserProfile(cachedProfile)

        val mockEngine = MockEngine { request ->
            // Verify request properties
            assertEquals("Bearer mock-token", request.headers["Authorization"])

            // Return a mock response
            respond(
                content = """
                    {
                        "display_name": "Refreshed User",
                        "id": "refreshed-user-id",
                        "external_urls": {"spotify": "https://open.spotify.com/user/refreshed-user-id"},
                        "followers": {"href": null, "total": 7},
                        "href": "https://api.spotify.com/v1/users/refreshed-user-id",
                        "images": [],
                        "type": "user",
                        "uri": "spotify:user:refreshed-user-id"
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

        val service = SpotifyUserServiceImpl(
            tokenManager = successTokenManager,
            client = mockClient,
            storage = mockStorage
        )

        // Act
        val result = service.refreshUserProfile()

        // Assert
        assertIs<SpotifyResult.Success<SpotifyUserProfile>>(result)
        assertEquals("Refreshed User", result.data.displayName)
        assertEquals("refreshed-user-id", result.data.userId)
        
        // Verify the profile was updated in storage
        assertEquals("Refreshed User", mockStorage.storedProfile?.displayName)
        assertEquals("refreshed-user-id", mockStorage.storedProfile?.userId)
    }

    @Test
    fun `getCurrentUserProfile should return failure when token retrieval fails`() = runBlocking {
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

        val service = SpotifyUserServiceImpl(
            tokenManager = failureTokenManager,
            client = mockClient,
            storage = mockStorage
        )

        // Act
        val result = service.getCurrentUserProfile()

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        val error = result.exception
        assertEquals("Failed to get access token: Token retrieval failed", error.error?.message)
        assertEquals(401, error.error?.status)
        
        // Verify the profile was not stored
        assertNull(mockStorage.storedProfile)
    }

    @Test
    fun `refreshUserProfile should return failure when API call fails`() = runBlocking {
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

        val service = SpotifyUserServiceImpl(
            tokenManager = successTokenManager,
            client = mockClient,
            storage = mockStorage
        )

        // Act
        val result = service.refreshUserProfile()

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        
        // Verify the profile was not stored
        assertNull(mockStorage.storedProfile)
    }
}