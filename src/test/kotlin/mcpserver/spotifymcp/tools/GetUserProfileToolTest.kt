package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import mcpserver.spotify.services.userservice.SpotifyUserService
import mcpserver.spotify.services.userservice.model.SpotifyUserProfile
import mcpserver.spotify.services.userservice.model.Followers
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import org.junit.jupiter.api.Test

class GetUserProfileToolTest {

    /**
     * This is a simple test to verify that the tool function can be called without errors.
     * In a real-world scenario, we would use a mocking framework to create mock objects
     * and verify the behavior of the tool in more detail.
     */
    @Test
    fun `addGetUserProfileTool should not throw exceptions`() {
        // Arrange
        val info = Implementation(
            "Test Server",
            "1.0.0"
        )
        val options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(true))
        )
        val server = Server(info, options)
        val userService = TestUserService()

        // Act & Assert - no exception should be thrown
        addGetUserProfileTool(server, userService)

        // Note: We can't directly access the tools property of the Server class
        // as it's private. In a real-world scenario, we would use a mocking framework
        // to verify that the addTool method was called with the expected parameters.
    }

    /**
     * A simple test implementation of SpotifyUserService.
     */
    private class TestUserService : SpotifyUserService {
        override suspend fun getCurrentUserProfile(): SpotifyResult<SpotifyUserProfile, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyUserProfile(
                    userId = "test-user",
                    displayName = "Test User",
                    followers = Followers(total = 42),
                    uri = "spotify:user:test-user"
                )
            )
        }

        override suspend fun refreshUserProfile(): SpotifyResult<SpotifyUserProfile, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyUserProfile(
                    userId = "test-user",
                    displayName = "Test User",
                    followers = Followers(total = 42),
                    uri = "spotify:user:test-user"
                )
            )
        }
    }
}
