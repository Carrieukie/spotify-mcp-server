package mcpserver.spotify.services.userservice

import mcpserver.spotify.services.userservice.model.SpotifyUserProfile
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyUserService {
    suspend fun getCurrentUserProfile(): SpotifyResult<SpotifyUserProfile, SpotifyApiError>
    suspend fun refreshUserProfile(): SpotifyResult<SpotifyUserProfile, SpotifyApiError>
}
