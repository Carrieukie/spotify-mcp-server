package mcpserver.spotify.authstuff.authmanager.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RefreshTokenResponse(

	@SerialName("access_token")
	val accessToken: String? = null,

	@SerialName("scope")
	val scope: String? = null,

	@SerialName("token_type")
	val tokenType: String? = null,

	@SerialName("expires_in")
	val expiresIn: Int? = null
)
