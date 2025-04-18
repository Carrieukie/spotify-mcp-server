package mcpserver.spotify.authstuff.tokenstorage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenData(
    @SerialName("access_token")
    val accessToken: String?,

    @SerialName("refresh_token")
    val refreshToken: String?,

    @SerialName("scope")
    val scope: String? = "",
)
