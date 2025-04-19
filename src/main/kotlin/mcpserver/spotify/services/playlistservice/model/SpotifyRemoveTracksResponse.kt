package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyRemoveTracksResponse(
    @SerialName("snapshot_id")
    val snapshotId: String? = null
)