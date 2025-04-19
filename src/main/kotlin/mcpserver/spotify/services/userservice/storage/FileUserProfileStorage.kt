package mcpserver.spotify.services.userservice.storage

import kotlinx.serialization.json.Json
import mcpserver.spotify.services.userservice.model.SpotifyUserProfile
import java.io.File

/**
 * File-based implementation of UserProfileStorage
 * Stores the user profile in a JSON file
 *
 * ## ⚠️ WARNING: Use this in production and you're cooked. 😂😂😂
 * If you're using this in production, you're basically storing user data like a raccoon
 * hides snacks—unguarded, and just waiting to be stolen. Don't be that raccoon.
 *
 * Great for local dev, testing, or impressing raccoons. Not so great for real users, lawsuits,
 * or trying to sleep at night knowing your user data is one `cat file.txt` away.
 *
 * 🔐 Encrypt. Secure. Sanitize. Otherwise, may your logs be forever cursed.
 */
class FileUserProfileStorage(private val file: File) : UserProfileStorage {

    @Synchronized
    override fun saveUserProfile(profile: SpotifyUserProfile) {
        val json = Json.encodeToString(SpotifyUserProfile.serializer(), profile)
        file.writeText(json)
        println("User profile saved: ${profile.displayName} (${profile.userId})")
    }

    override fun getUserProfile(): SpotifyUserProfile? = runCatching {
        if (!file.exists()) return null
        Json.decodeFromString<SpotifyUserProfile>(file.readText())
    }.getOrElse {
        println("❌ Failed to read user profile from file: ${it.message}")
        null
    }

}