package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyCreatePlaylistRequest(
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("public")
    val public: Boolean? = true,
    
    @SerialName("collaborative")
    val collaborative: Boolean? = false
)