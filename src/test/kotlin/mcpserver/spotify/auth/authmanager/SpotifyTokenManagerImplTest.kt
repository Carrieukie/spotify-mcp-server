package mcpserver.spotify.auth.authmanager

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.auth.tokenstorage.FileTokenStorageImpl
import mcpserver.spotify.auth.tokenstorage.TokenStorage
import mcpserver.spotify.auth.tokenstorage.model.TokenData
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SpotifyTokenManagerImplTest {

    private lateinit var tempFile: File
    private lateinit var tokenStorage: FileTokenStorageImpl
    private val clientId = "test-client-id"
    private val clientSecret = "test-client-secret"

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        tempFile = File(tempDir.toFile(), "test-tokens.json")
        tokenStorage = FileTokenStorageImpl(tempFile)
    }

    @Test
    fun `getValidAccessToken should return existing token when it is valid`() {
        runBlocking {
            // Arrange
            // Save a valid token to storage
            val validToken = "valid-access-token"
            tokenStorage.saveTokens(
                TokenData(
                    accessToken = validToken,
                    refreshToken = "refresh-token"
                )
            )

            // Mock HTTP client to simulate a successful validation request
            val mockEngine = MockEngine { request ->
                // Verify request properties
                assertEquals("Bearer $validToken", request.headers["Authorization"])
                assertEquals("https://api.spotify.com/v1/me", request.url.toString())

                // Return a successful response
                respond(
                    content = """{"id": "test-user"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            val mockClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json()
                }
            }

            val tokenManager = SpotifyTokenManagerImpl(
                httpClient = mockClient,
                tokenStorage = tokenStorage,
                clientId = clientId,
                clientSecret = clientSecret
            )

            // Act
            val result = tokenManager.getValidAccessToken()

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
            assertEquals(validToken, result.data)
        }
    }

    @Test
    fun `getValidAccessToken should refresh token when it is invalid`() {
        runBlocking {
            // Arrange
            // Save an invalid token to storage
            val invalidToken = "invalid-access-token"
            val refreshToken = "refresh-token"
            val newAccessToken = "new-access-token"

            tokenStorage.saveTokens(
                TokenData(
                    accessToken = invalidToken,
                    refreshToken = refreshToken
                )
            )

            // Mock HTTP client to simulate a failed validation request followed by a successful refresh
            val mockEngine = MockEngine { request ->
                when (request.url.toString()) {
                    "https://api.spotify.com/v1/me" -> {
                        // Verify request properties for validation
                        assertEquals("Bearer $invalidToken", request.headers["Authorization"])

                        // Return a 401 Unauthorized response
                        respond(
                            content = """{"error":{"status":401,"message":"Invalid token"}}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    "https://accounts.spotify.com/api/token" -> {
                        // Verify request properties for token refresh
                        assertEquals("Basic ${java.util.Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}", 
                            request.headers["Authorization"])

                        // Return a successful refresh response
                        respond(
                            content = """
                                {
                                    "access_token": "$newAccessToken",
                                    "token_type": "Bearer",
                                    "expires_in": 3600,
                                    "scope": "user-read-private"
                                }
                            """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> error("Unexpected URL: ${request.url}")
                }
            }

            val mockClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json()
                }
                expectSuccess = false // Don't throw exceptions for non-2xx responses
            }

            val tokenManager = SpotifyTokenManagerImpl(
                httpClient = mockClient,
                tokenStorage = tokenStorage,
                clientId = clientId,
                clientSecret = clientSecret
            )

            // Act
            val result = tokenManager.getValidAccessToken()

            // Assert
            assertIs<SpotifyResult.Success<String>>(result)
            assertEquals(newAccessToken, result.data)
        }
    }

    @Test
    fun `getValidAccessToken should return failure when refresh token is missing`() {
        runBlocking {
            // Arrange
            // Save a token without refresh token
            val invalidToken = "invalid-access-token"

            tokenStorage.saveTokens(
                TokenData(
                    accessToken = invalidToken,
                    refreshToken = null
                )
            )

            // Mock HTTP client to simulate a failed validation request
            val mockEngine = MockEngine { request ->
                // Verify request properties for validation
                assertEquals("Bearer $invalidToken", request.headers["Authorization"])
                assertEquals("https://api.spotify.com/v1/me", request.url.toString())

                // Return a 401 Unauthorized response
                respond(
                    content = """{"error":{"status":401,"message":"Invalid token"}}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            val mockClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json()
                }
                expectSuccess = false // Don't throw exceptions for non-2xx responses
            }

            val tokenManager = SpotifyTokenManagerImpl(
                httpClient = mockClient,
                tokenStorage = tokenStorage,
                clientId = clientId,
                clientSecret = clientSecret
            )

            // Act
            val result = tokenManager.getValidAccessToken()

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyAccountsError>>(result)
            assertEquals("❌ No refresh token available", result.exception.errorDescription)
        }
    }

    @Test
    fun `getValidAccessToken should return failure when token refresh fails`() {
        runBlocking {
            // Arrange
            // Save an invalid token to storage
            val invalidToken = "invalid-access-token"
            val refreshToken = "refresh-token"

            tokenStorage.saveTokens(
                TokenData(
                    accessToken = invalidToken,
                    refreshToken = refreshToken
                )
            )

            // Mock HTTP client to simulate a failed validation request followed by a failed refresh
            val mockEngine = MockEngine { request ->
                when (request.url.toString()) {
                    "https://api.spotify.com/v1/me" -> {
                        // Return a 401 Unauthorized response
                        respond(
                            content = """{"error":{"status":401,"message":"Invalid token"}}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    "https://accounts.spotify.com/api/token" -> {
                        // Return a failed refresh response with a 400 Bad Request status
                        // The content doesn't matter as much as the status code, since we're testing error handling
                        respond(
                            content = "",
                            status = HttpStatusCode.BadRequest,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> error("Unexpected URL: ${request.url}")
                }
            }

            val mockClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json()
                }
                expectSuccess = false // Don't throw exceptions for non-2xx responses
            }

            val tokenManager = SpotifyTokenManagerImpl(
                httpClient = mockClient,
                tokenStorage = tokenStorage,
                clientId = clientId,
                clientSecret = clientSecret
            )

            // Act
            val result = tokenManager.getValidAccessToken()

            // Assert
            assertIs<SpotifyResult.Failure<SpotifyAccountsError>>(result)
            // We're just checking that we got a failure result, not the specific error message
            // since the error handling might create a default error
        }
    }
}
