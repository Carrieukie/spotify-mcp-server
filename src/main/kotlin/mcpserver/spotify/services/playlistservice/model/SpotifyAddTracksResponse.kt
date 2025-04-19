package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyAddTracksResponse(
    @SerialName("snapshot_id")
    val snapshotId: String? = null
)