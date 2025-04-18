package mcpserver.spotify.spotifyapi

import mcpserver.spotify.spotifyapi.model.response.SpotifySearchResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyApi {
    suspend fun playTrack(trackUris: List<String> = listOf()): SpotifyResult<String, SpotifyApiError>
    suspend fun pausePlayback(): SpotifyResult<String,SpotifyApiError>
    suspend fun search(
        query: String,
        type: String,
        limit: Int = 20,
        offset: Int = 0,
        market: String? = null,
        includeExternal: String? = null
    ): SpotifyResult<SpotifySearchResponse, SpotifyApiError>
}
