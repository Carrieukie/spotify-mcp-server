package mcpserver.spotify.spotifyapi

import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyApi {
    suspend fun playTrack(trackUri: String? = null): SpotifyResult<String, SpotifyApiError>
    suspend fun pausePlayback(): SpotifyResult<String,SpotifyApiError>
}
