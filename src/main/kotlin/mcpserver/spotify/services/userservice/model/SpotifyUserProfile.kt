package mcpserver.spotify.services.userservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyUserProfile(
    @SerialName("display_name")
    val displayName: String?,
    
    @SerialName("id")
    val userId: String,
    
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,
    
    @SerialName("followers")
    val followers: Followers? = null,
    
    @SerialName("href")
    val href: String? = null,
    
    @SerialName("images")
    val images: List<ImageItem>? = emptyList(),
    
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
data class Followers(
    @SerialName("href")
    val href: String? = null,
    
    @SerialName("total")
    val total: Int? = 0
)

@Serializable
data class ImageItem(
    @SerialName("url")
    val url: String? = null,
    
    @SerialName("height")
    val height: Int? = null,
    
    @SerialName("width")
    val width: Int? = null
)