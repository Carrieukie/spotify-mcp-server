package mcpserver.spotify.utils.networkutils.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyAccountsError(

	@SerialName("error_description")
	val errorDescription: String? = null,

	@SerialName("error")
	val error: String? = null,

	@SerialName("code")
	val status: Int? = null
): Exception()
