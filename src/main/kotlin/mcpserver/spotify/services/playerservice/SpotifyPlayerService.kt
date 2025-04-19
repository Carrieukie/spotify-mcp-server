package mcpserver.spotify.services.playerservice

import mcpserver.spotify.services.playerservice.model.SpotifySearchResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyPlayerService {
    suspend fun playTrack(
        trackUris: List<String> = listOf(),
        contextUri: String? = null,
        positionMs: Int? = null,
        deviceId: String? = null
    ): SpotifyResult<String, SpotifyApiError>
    suspend fun pausePlayback(): SpotifyResult<String, SpotifyApiError>
    suspend fun search(
        query: String,
        type: String,
        limit: Int = 20,
        offset: Int = 0,
        market: String? = null,
        includeExternal: String? = null
    ): SpotifyResult<SpotifySearchResponse, SpotifyApiError>

    suspend fun skipToNextTrack(): SpotifyResult<String, SpotifyApiError>
    suspend fun skipToPreviousTrack(): SpotifyResult<String, SpotifyApiError>
    suspend fun seekToPosition(positionMs: Int): SpotifyResult<String, SpotifyApiError>
    suspend fun setRepeatMode(state: String): SpotifyResult<String, SpotifyApiError>
    suspend fun setVolume(volumePercent: Int): SpotifyResult<String, SpotifyApiError>
    suspend fun getQueue(): SpotifyResult<String, SpotifyApiError>
}
