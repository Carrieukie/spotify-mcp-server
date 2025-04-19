package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mcpserver.spotify.services.userservice.model.ExternalUrls
import mcpserver.spotify.services.userservice.model.Followers
import mcpserver.spotify.services.userservice.model.ImageItem

@Serializable
data class SpotifyPlaylistsResponse(
    @SerialName("href")
    val href: String,
    
    @SerialName("items")
    val items: List<PlaylistItem>,
    
    @SerialName("limit")
    val limit: Int,
    
    @SerialName("next")
    val next: String?,
    
    @SerialName("offset")
    val offset: Int,
    
    @SerialName("previous")
    val previous: String?,
    
    @SerialName("total")
    val total: Int
)

@Serializable
data class PlaylistItem(
    @SerialName("collaborative")
    val collaborative: Boolean,
    
    @SerialName("description")
    val description: String?,
    
    @SerialName("external_urls")
    val externalUrls: ExternalUrls,
    
    @SerialName("href")
    val href: String,
    
    @SerialName("id")
    val id: String,
    
    @SerialName("images")
    val images: List<ImageItem>,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("owner")
    val owner: PlaylistOwner,
    
    @SerialName("public")
    val public: Boolean?,
    
    @SerialName("snapshot_id")
    val snapshotId: String,
    
    @SerialName("tracks")
    val tracks: PlaylistTracks,
    
    @SerialName("type")
    val type: String,
    
    @SerialName("uri")
    val uri: String
)

@Serializable
data class PlaylistOwner(
    @SerialName("display_name")
    val displayName: String?,
    
    @SerialName("external_urls")
    val externalUrls: ExternalUrls,
    
    @SerialName("href")
    val href: String,
    
    @SerialName("id")
    val id: String,
    
    @SerialName("type")
    val type: String,
    
    @SerialName("uri")
    val uri: String
)

@Serializable
data class PlaylistTracks(
    @SerialName("href")
    val href: String,
    
    @SerialName("total")
    val total: Int
)