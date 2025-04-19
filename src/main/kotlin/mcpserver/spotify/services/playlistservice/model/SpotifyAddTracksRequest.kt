package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyAddTracksRequest(
    @SerialName("uris")
    val uris: List<String>,
    
    @SerialName("position")
    val position: Int? = null
)