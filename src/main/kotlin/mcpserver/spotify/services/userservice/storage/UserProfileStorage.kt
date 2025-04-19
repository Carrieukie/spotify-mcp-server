package mcpserver.spotify.services.userservice.storage

import mcpserver.spotify.services.userservice.model.SpotifyUserProfile


interface UserProfileStorage {
    fun saveUserProfile(profile: SpotifyUserProfile)
    fun getUserProfile(): SpotifyUserProfile?
}