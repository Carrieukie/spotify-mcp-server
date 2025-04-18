package mcpserver.spotify.spotifyapi

import mcpserver.spotify.spotifyapi.model.response.SpotifySearchResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyApi {
    suspend fun playTrack(trackUris: List<String> = listOf()): SpotifyResult<String, SpotifyApiError>
    suspend fun pausePlayback(): SpotifyResult<String,SpotifyApiError>
    suspend fun searchForTrack(
        query: String,
        type: String = "track",
        limit: Int = 10
    ): SpotifyResult<SpotifySearchResponse, SpotifyApiError>
}
