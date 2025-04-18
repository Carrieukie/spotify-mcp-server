package mcpserver.spotify.auth.authmanager

import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError

interface SpotifyTokenManager {
    suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError>
}