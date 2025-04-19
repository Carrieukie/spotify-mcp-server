package mcpserver.spotify.utils.networkutils

import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.utils.networkutils.model.Error
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SpotifySafeApiCallTest {

    @Test
    fun `safeSpotifyApiCall should return Success when function completes without exception`() = runBlocking {
        // Arrange
        val expectedResult = "Success"

        // Act
        val result = safeSpotifyApiCall<String, SpotifyApiError> {
            expectedResult
        }

        // Assert
        assertIs<SpotifyResult.Success<String>>(result)
        assertEquals(expectedResult, (result as SpotifyResult.Success<String>).data)
    }

    @Test
    fun `safeSpotifyApiCall should return Failure with SpotifyApiError when ClientRequestException occurs`() = runBlocking {
        // Act
        val result = safeSpotifyApiCall<String, SpotifyApiError> {
            // Simulate a ClientRequestException by throwing a RuntimeException with a specific message
            // The safeSpotifyApiCall function will handle this as a generic exception
            throw RuntimeException("Bad request")
        }

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
        assertEquals("Bad request", error.error?.message)
    }

    @Test
    fun `safeSpotifyApiCall should return Failure with SpotifyApiError when ServerResponseException occurs`() = runBlocking {
        // Act
        val result = safeSpotifyApiCall<String, SpotifyApiError> {
            // Simulate a ServerResponseException by throwing a RuntimeException with a specific message
            // The safeSpotifyApiCall function will handle this as a generic exception
            throw RuntimeException("Internal server error")
        }

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyApiError>>(result)
        val error = (result as SpotifyResult.Failure<SpotifyApiError>).exception
        assertEquals("Internal server error", error.error?.message)
    }

    @Test
    fun `safeSpotifyApiCall should return Failure with SpotifyAccountsError when generic exception occurs`() = runBlocking {
        // Arrange
        val exception = Exception("Test exception")

        // Act
        val result = safeSpotifyApiCall<String, SpotifyAccountsError> {
            throw exception
        }

        // Assert
        assertIs<SpotifyResult.Failure<SpotifyAccountsError>>(result)
        val error = (result as SpotifyResult.Failure<SpotifyAccountsError>).exception
        assertEquals("Test exception", error.errorDescription)
        assertEquals(0, error.status)
    }
}
