package mcpserver.spotify.services.playlistservice

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistsResponse
import mcpserver.spotify.utils.getHttpClient
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import mcpserver.spotify.utils.networkutils.safeSpotifyApiCall

class SpotifyPlaylistServiceImpl(
    private val tokenManager: SpotifyTokenManager,
    private val client: HttpClient = getHttpClient()
) : SpotifyPlaylistService {

    override suspend fun getUserPlaylists(
        userId: String,
        limit: Int,
        offset: Int
    ): SpotifyResult<SpotifyPlaylistsResponse, SpotifyApiError> {
        // First, get a valid access token
        val tokenResult = tokenManager.getValidAccessToken()

        if (tokenResult is SpotifyResult.Failure) {
            return SpotifyResult.Failure(
                SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = "Failed to get access token: ${tokenResult.exception.errorDescription}",
                        status = tokenResult.exception.status
                    )
                )
            )
        }

        val token = (tokenResult as SpotifyResult.Success).data

        // Make the API call to get user playlists
        return safeSpotifyApiCall {
            val response = client.get("https://api.spotify.com/v1/users/$userId/playlists") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                parameter("limit", limit)
                parameter("offset", offset)
            }

            response.body<SpotifyPlaylistsResponse>()
        }
    }
}