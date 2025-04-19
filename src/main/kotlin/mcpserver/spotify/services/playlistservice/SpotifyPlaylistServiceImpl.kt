package mcpserver.spotify.services.playlistservice

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.playlistservice.model.PlaylistItem
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyCreatePlaylistRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistItemsResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyRemoveTracksRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyRemoveTracksResponse
import mcpserver.spotify.utils.getHttpClient
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import mcpserver.spotify.utils.networkutils.safeSpotifyApiCall

class SpotifyPlaylistServiceImpl(
    private val tokenManager: SpotifyTokenManager,
    private val client: HttpClient = getHttpClient(),
) : SpotifyPlaylistService {

    override suspend fun getCurrentUserPlaylists(
        limit: Int,
        offset: Int
    ): SpotifyResult<SpotifyPlaylistResponse, SpotifyApiError> {
        return when (val tokenResult = tokenManager.getValidAccessToken()) {
            is SpotifyResult.Failure -> SpotifyResult.Failure(
                SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = tokenResult.exception.errorDescription,
                        status = tokenResult.exception.status,
                    )
                )
            )

            is SpotifyResult.Success -> safeSpotifyApiCall {
                val endpoint = "https://api.spotify.com/v1/me/playlists"
                client.get(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                    }
                    parameter("limit", limit)
                    parameter("offset", offset)
                }.body()
            }
        }
    }

    override suspend fun getPlaylistItems(
        playlistId: String,
        limit: Int,
        offset: Int
    ): SpotifyResult<SpotifyPlaylistItemsResponse, SpotifyApiError> {
        return when (val tokenResult = tokenManager.getValidAccessToken()) {
            is SpotifyResult.Failure -> SpotifyResult.Failure(
                SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = tokenResult.exception.errorDescription,
                        status = tokenResult.exception.status,
                    )
                )
            )

            is SpotifyResult.Success -> safeSpotifyApiCall {
                val endpoint = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
                client.get(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                    }
                    parameter("limit", limit)
                    parameter("offset", offset)
                }.body()
            }
        }
    }

    override suspend fun removePlaylistTracks(
        playlistId: String,
        request: SpotifyRemoveTracksRequest
    ): SpotifyResult<SpotifyRemoveTracksResponse, SpotifyApiError> {
        return when (val tokenResult = tokenManager.getValidAccessToken()) {
            is SpotifyResult.Failure -> SpotifyResult.Failure(
                SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = tokenResult.exception.errorDescription,
                        status = tokenResult.exception.status,
                    )
                )
            )

            is SpotifyResult.Success -> safeSpotifyApiCall {
                val endpoint = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
                client.delete(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(request)
                }.body()
            }
        }
    }

    override suspend fun addPlaylistTracks(
        playlistId: String,
        request: SpotifyAddTracksRequest
    ): SpotifyResult<SpotifyAddTracksResponse, SpotifyApiError> {
        return when (val tokenResult = tokenManager.getValidAccessToken()) {
            is SpotifyResult.Failure -> SpotifyResult.Failure(
                SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = tokenResult.exception.errorDescription,
                        status = tokenResult.exception.status,
                    )
                )
            )

            is SpotifyResult.Success -> safeSpotifyApiCall {
                val endpoint = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
                client.post(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(request)
                }.body()
            }
        }
    }

    override suspend fun createPlaylist(
        userId: String,
        request: SpotifyCreatePlaylistRequest
    ): SpotifyResult<PlaylistItem, SpotifyApiError> {
        return when (val tokenResult = tokenManager.getValidAccessToken()) {
            is SpotifyResult.Failure -> SpotifyResult.Failure(
                SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = tokenResult.exception.errorDescription,
                        status = tokenResult.exception.status,
                    )
                )
            )

            is SpotifyResult.Success -> safeSpotifyApiCall {
                val endpoint = "https://api.spotify.com/v1/users/$userId/playlists"
                client.post(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(request)
                }.body()
            }
        }
    }
}
