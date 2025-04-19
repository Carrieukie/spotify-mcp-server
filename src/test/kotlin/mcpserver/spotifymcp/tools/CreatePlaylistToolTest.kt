package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import mcpserver.spotify.services.playlistservice.SpotifyPlaylistService
import mcpserver.spotify.services.playlistservice.model.*
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import org.junit.jupiter.api.Test

class CreatePlaylistToolTest {

    /**
     * This is a simple test to verify that the tool function can be called without errors.
     * In a real-world scenario, we would use a mocking framework to create mock objects
     * and verify the behavior of the tool in more detail.
     */
    @Test
    fun `addSpotifyCreatePlaylistTool should not throw exceptions`() {
        // Arrange
        val info = Implementation(
            "Test Server",
            "1.0.0"
        )
        val options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(true))
        )
        val server = Server(info, options)
        val playlistService = TestPlaylistService()

        // Act & Assert - no exception should be thrown
        addSpotifyCreatePlaylistTool(server, playlistService)

        // Note: We can't directly access the tools property of the Server class
        // as it's private. In a real-world scenario, we would use a mocking framework
        // to verify that the addTool method was called with the expected parameters.
    }

    /**
     * A simple test implementation of SpotifyPlaylistService.
     */
    private class TestPlaylistService : SpotifyPlaylistService {
        override suspend fun getCurrentUserPlaylists(
            limit: Int,
            offset: Int
        ): SpotifyResult<SpotifyPlaylistResponse, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyPlaylistResponse(
                    href = "https://api.spotify.com/v1/users/test-user/playlists",
                    limit = limit,
                    offset = offset,
                    total = 0,
                    items = emptyList()
                )
            )
        }

        override suspend fun getPlaylistItems(
            playlistId: String,
            limit: Int,
            offset: Int
        ): SpotifyResult<SpotifyPlaylistItemsResponse, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyPlaylistItemsResponse(
                    href = "https://api.spotify.com/v1/playlists/$playlistId/tracks",
                    limit = limit,
                    offset = offset,
                    total = 0,
                    items = emptyList()
                )
            )
        }

        override suspend fun addPlaylistTracks(
            playlistId: String,
            request: SpotifyAddTracksRequest
        ): SpotifyResult<SpotifyAddTracksResponse, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyAddTracksResponse(
                    snapshotId = "test-snapshot-id"
                )
            )
        }

        override suspend fun removePlaylistTracks(
            playlistId: String,
            request: SpotifyRemoveTracksRequest
        ): SpotifyResult<SpotifyRemoveTracksResponse, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyRemoveTracksResponse(
                    snapshotId = "test-snapshot-id"
                )
            )
        }

        override suspend fun createPlaylist(
            userId: String,
            request: SpotifyCreatePlaylistRequest
        ): SpotifyResult<SpotifyPlaylistItem, SpotifyApiError> {
            return SpotifyResult.Success(
                SpotifyPlaylistItem(
                    id = "test-playlist",
                    name = request.name,
                    description = request.description,
                    public = request.public,
                    collaborative = request.collaborative
                )
            )
        }
    }
}
