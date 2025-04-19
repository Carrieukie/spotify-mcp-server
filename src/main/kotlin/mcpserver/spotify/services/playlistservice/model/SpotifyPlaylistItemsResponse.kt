package mcpserver.spotify.services.playlistservice.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifyPlaylistItemsResponse(
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
    val items: List<PlaylistTrackItem?>? = null
)

@Serializable
data class PlaylistTrackItem(
    @SerialName("added_at")
    val addedAt: String? = null,

    @SerialName("added_by")
    val addedBy: AddedBy? = null,

    @SerialName("is_local")
    val isLocal: Boolean? = null,

    @SerialName("track")
    val track: Track? = null
)

@Serializable
data class AddedBy(
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,

    @SerialName("href")
    val href: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("uri")
    val uri: String? = null
)

@Serializable
data class Track(
    @SerialName("album")
    val album: Album? = null,

    @SerialName("artists")
    val artists: List<Artist?>? = null,

    @SerialName("available_markets")
    val availableMarkets: List<String?>? = null,

    @SerialName("disc_number")
    val discNumber: Int? = null,

    @SerialName("duration_ms")
    val durationMs: Int? = null,

    @SerialName("explicit")
    val explicit: Boolean? = null,

    @SerialName("external_ids")
    val externalIds: ExternalIds? = null,

    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,

    @SerialName("href")
    val href: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("is_playable")
    val isPlayable: Boolean? = null,

    @SerialName("linked_from")
    val linkedFrom: LinkedFrom? = null,

    @SerialName("restrictions")
    val restrictions: Restrictions? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("popularity")
    val popularity: Int? = null,

    @SerialName("preview_url")
    val previewUrl: String? = null,

    @SerialName("track_number")
    val trackNumber: Int? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("uri")
    val uri: String? = null,

    @SerialName("is_local")
    val isLocal: Boolean? = null
)

@Serializable
data class Album(
    @SerialName("album_type")
    val albumType: String? = null,

    @SerialName("total_tracks")
    val totalTracks: Int? = null,

    @SerialName("available_markets")
    val availableMarkets: List<String?>? = null,

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

    @SerialName("release_date")
    val releaseDate: String? = null,

    @SerialName("release_date_precision")
    val releaseDatePrecision: String? = null,

    @SerialName("restrictions")
    val restrictions: Restrictions? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("uri")
    val uri: String? = null,

    @SerialName("artists")
    val artists: List<Artist?>? = null
)

@Serializable
data class Artist(
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,

    @SerialName("href")
    val href: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("uri")
    val uri: String? = null
)

@Serializable
data class ExternalIds(
    @SerialName("isrc")
    val isrc: String? = null,

    @SerialName("ean")
    val ean: String? = null,

    @SerialName("upc")
    val upc: String? = null
)

@Serializable
data class LinkedFrom(
    @SerialName("uri")
    val uri: String? = null
    // This is a placeholder for the linked_from field
    // Add more properties as needed
)

@Serializable
data class Restrictions(
    @SerialName("reason")
    val reason: String? = null
)
