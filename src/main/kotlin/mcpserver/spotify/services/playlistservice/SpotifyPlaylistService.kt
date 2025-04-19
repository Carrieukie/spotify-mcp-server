package mcpserver.spotify.services.playlistservice

import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistsResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyPlaylistService {
    suspend fun getUserPlaylists(
        userId: String,
        limit: Int = 20,
        offset: Int = 0
    ): SpotifyResult<SpotifyPlaylistsResponse, SpotifyApiError>
}