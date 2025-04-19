package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyRemoveTracksRequest(
    @SerialName("tracks")
    val tracks: List<TrackUri>,
    
    @SerialName("snapshot_id")
    val snapshotId: String? = null
)

@Serializable
data class TrackUri(
    @SerialName("uri")
    val uri: String
)