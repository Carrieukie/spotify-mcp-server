package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyPlaylistResponse(
    @SerialName("href")
    val href: String? = null,
    
    @SerialName("limit")
    val limit: Int? = null,
    
    @SerialName("next")
    val next: String? = null,
    
    @SerialName("offset")
    val offset: Int? = null,
    
    @SerialName("previous")
    val previous: String? = null,
    
    @SerialName("total")
    val total: Int? = null,
    
    @SerialName("items")
    val items: List<PlaylistItem?>? = null
)

@Serializable
data class PlaylistItem(
    @SerialName("collaborative")
    val collaborative: Boolean? = null,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,
    
    @SerialName("href")
    val href: String? = null,
    
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("images")
    val images: List<Image?>? = null,
    
    @SerialName("name")
    val name: String? = null,
    
    @SerialName("owner")
    val owner: Owner? = null,
    
    @SerialName("public")
    val public: Boolean? = null,
    
    @SerialName("snapshot_id")
    val snapshotId: String? = null,
    
    @SerialName("tracks")
    val tracks: Tracks? = null,
    
    @SerialName("type")
    val type: String? = null,
    
    @SerialName("uri")
    val uri: String? = null
)

@Serializable
data class ExternalUrls(
    @SerialName("spotify")
    val spotify: String? = null
)

@Serializable
data class Image(
    @SerialName("url")
    val url: String? = null,
    
    @SerialName("height")
    val height: Int? = null,
    
    @SerialName("width")
    val width: Int? = null
)

@Serializable
data class Owner(
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,
    
    @SerialName("href")
    val href: String? = null,
    
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("type")
    val type: String? = null,
    
    @SerialName("uri")
    val uri: String? = null,
    
    @SerialName("display_name")
    val displayName: String? = null
)

@Serializable
data class Tracks(
    @SerialName("href")
    val href: String? = null,
    
    @SerialName("total")
    val total: Int? = null
)