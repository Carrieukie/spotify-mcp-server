package mcpserver.spotify.auth.tokenstorage

import kotlinx.serialization.json.Json
import mcpserver.spotify.auth.tokenstorage.model.TokenData
import java.io.File

/**
 * ## ⚠️ WARNING: Use this in production and you're cooked. 😂😂😂
 * If you're using this in production, you're basically storing your tokens like a raccoon
 * hides snacks—unguarded, and just waiting to be stolen. Don't be that raccoon.
 *
 * Great for local dev, testing, or impressing raccoons. Not so great for real users, lawsuits,
 * or trying to sleep at night knowing your secrets are one `cat file.txt` away.
 *
 * 🔐 Encrypt. Secure. Sanitize. Otherwise, may your logs be forever cursed.
 */
class FileTokenStorageImpl(private val file: File) : TokenStorage {

    @Synchronized
    override fun saveTokens(
        tokenData: TokenData
    ) {
        val json = Json.encodeToString(tokenData)
        file.writeText(json)
        println(getTokens())
    }

    override fun getTokens(): TokenData? = runCatching {
        if (!file.exists()) return null
        Json.decodeFromString<TokenData>(file.readText())
    }.getOrElse {
        println("❌ Failed to read tokens from file: ${it.message}")
        null
    }

}


