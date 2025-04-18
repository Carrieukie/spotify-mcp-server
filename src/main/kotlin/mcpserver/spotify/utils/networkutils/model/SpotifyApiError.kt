package mcpserver.spotify.utils.networkutils.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyApiError(

	@SerialName("error")
	val error: Error? = null
): Exception()

@Serializable
data class Error(

	@SerialName("message")
	val message: String? = null,

	@SerialName("status")
	val status: Int? = null
)
