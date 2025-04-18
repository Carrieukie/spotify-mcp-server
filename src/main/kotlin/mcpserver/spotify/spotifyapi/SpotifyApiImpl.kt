package mcpserver.spotify.spotifyapi

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mcpserver.spotify.authstuff.spotifytokenmanager.SpotifyTokenManager
import mcpserver.spotify.spotifyapi.model.response.SpotifySearchResponse
import mcpserver.spotify.utils.getHttpClient
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import mcpserver.spotify.utils.networkutils.safeSpotifyApiCall

class SpotifyApiImpl(
    private val tokenManager: SpotifyTokenManager,
    private val client: HttpClient = getHttpClient(),
) : SpotifyApi {
    override suspend fun playTrack(trackUris: List<String>): SpotifyResult<String, SpotifyApiError> {
        when (val accessToken = tokenManager.getValidAccessToken()) {
            is SpotifyResult.Failure<SpotifyAccountsError> -> {
                val error = SpotifyApiError(
                    error = mcpserver.spotify.utils.networkutils.model.Error(
                        message = accessToken.exception.errorDescription,
                        status = accessToken.exception.status,
                    )
                )
                return SpotifyResult.Failure(error)
            }

            is SpotifyResult.Success<String> -> {
                val response = safeSpotifyApiCall<String, SpotifyApiError> {
                    val endpoint = "https://api.spotify.com/v1/me/player/play"
                    client.put(endpoint) {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${accessToken.data}")
                            append(HttpHeaders.ContentType, "application/json")
                        }
                        if (trackUris.isEmpty()) {
                            setBody(mapOf("uris" to trackUris))
                        }
                    }.body<String>()
                }
                return response
            }
        }
    }

    override suspend fun pausePlayback(): SpotifyResult<String, SpotifyApiError> {
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
                val endpoint = "https://api.spotify.com/v1/me/player/pause"
                client.put(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                    }
                }.body()
            }
        }
    }

    override suspend fun searchForTrack(
        query: String,
        type: String,
        limit: Int
    ): SpotifyResult<SpotifySearchResponse, SpotifyApiError> {
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
                val endpoint = "https://api.spotify.com/v1/search"
                client.get(endpoint) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${tokenResult.data}")
                    }
                    parameter("q", query)
                    parameter("type", type)
                    parameter("limit", limit)
                }.body()
            }
        }
    }
}








