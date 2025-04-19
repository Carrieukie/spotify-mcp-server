package mcpserver.spotify.services.userservice

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mcpserver.spotify.auth.authmanager.SpotifyTokenManager
import mcpserver.spotify.services.userservice.model.SpotifyUserProfile
import mcpserver.spotify.services.userservice.storage.FileUserProfileStorage
import mcpserver.spotify.services.userservice.storage.UserProfileStorage
import mcpserver.spotify.utils.getHttpClient
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError
import mcpserver.spotify.utils.networkutils.safeSpotifyApiCall

class SpotifyUserServiceImpl(
    private val tokenManager: SpotifyTokenManager,
    private val storage: UserProfileStorage,
    private val client: HttpClient = getHttpClient(),
) : SpotifyUserService {

    override suspend fun getCurrentUserProfile(): SpotifyResult<SpotifyUserProfile, SpotifyApiError> {
        // Check if we already have the profile in storage
        val cachedProfile = storage.getUserProfile()
        if (cachedProfile != null) {
            return SpotifyResult.Success(cachedProfile)
        }

        // If not in storage, fetch from API
        return refreshUserProfile()
    }

    override suspend fun refreshUserProfile(): SpotifyResult<SpotifyUserProfile, SpotifyApiError> {
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

        // Make the API call to get user profile
        val result = safeSpotifyApiCall<SpotifyUserProfile, SpotifyApiError> {
            val response = client.get("https://api.spotify.com/v1/me") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
            }

            response.body<SpotifyUserProfile>()
        }

        // If successful, store the profile
        if (result is SpotifyResult.Success) {
            storage.saveUserProfile(result.data)
        }

        return result
    }
}
