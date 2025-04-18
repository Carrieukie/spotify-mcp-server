package mcpserver.spotify.api.playerservice.model.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SpotifySearchResponse(

	@SerialName("tracks")
	val tracks: Tracks? = null
)

@Serializable
data class ArtistsItem(

	@SerialName("name")
	val name: String? = null,

	@SerialName("href")
	val href: String? = null,

	@SerialName("id")
	val id: String? = null,

	@SerialName("type")
	val type: String? = null,

	@SerialName("external_urls")
	val externalUrls: ExternalUrls? = null,

	@SerialName("uri")
	val uri: String? = null
)

@Serializable
data class ExternalUrls(

	@SerialName("spotify")
	val spotify: String? = null
)

@Serializable
data class ItemsItem(

	@SerialName("disc_number")
	val discNumber: Int? = null,

	@SerialName("album")
	val album: Album? = null,

	@SerialName("available_markets")
	val availableMarkets: List<String?>? = null,

	@SerialName("type")
	val type: String? = null,

	@SerialName("external_ids")
	val externalIds: ExternalIds? = null,

	@SerialName("uri")
	val uri: String? = null,

	@SerialName("duration_ms")
	val durationMs: Int? = null,

	@SerialName("explicit")
	val explicit: Boolean? = null,

	@SerialName("is_playable")
	val isPlayable: Boolean? = null,

	@SerialName("artists")
	val artists: List<ArtistsItem?>? = null,

	@SerialName("preview_url")
	val previewUrl: String? = null,

	@SerialName("popularity")
	val popularity: Int? = null,

	@SerialName("name")
	val name: String? = null,

	@SerialName("track_number")
	val trackNumber: Int? = null,

	@SerialName("href")
	val href: String? = null,

	@SerialName("id")
	val id: String? = null,

	@SerialName("is_local")
	val isLocal: Boolean? = null,

	@SerialName("external_urls")
	val externalUrls: ExternalUrls? = null
)

@Serializable
data class ExternalIds(

	@SerialName("isrc")
	val isrc: String? = null
)

@Serializable
data class Tracks(

	@SerialName("next")
	val next: String? = null,

	@SerialName("total")
	val total: Int? = null,

	@SerialName("offset")
	val offset: Int? = null,

	@SerialName("previous")
	val previous: String? = null,

	@SerialName("limit")
	val limit: Int? = null,

	@SerialName("href")
	val href: String? = null,

	@SerialName("items")
	val items: List<ItemsItem?>? = null
)

@Serializable
data class ImagesItem(

	@SerialName("width")
	val width: Int? = null,

	@SerialName("url")
	val url: String? = null,

	@SerialName("height")
	val height: Int? = null
)

@Serializable
data class Album(

	@SerialName("images")
	val images: List<ImagesItem?>? = null,

	@SerialName("available_markets")
	val availableMarkets: List<String?>? = null,

	@SerialName("release_date_precision")
	val releaseDatePrecision: String? = null,

	@SerialName("type")
	val type: String? = null,

	@SerialName("uri")
	val uri: String? = null,

	@SerialName("total_tracks")
	val totalTracks: Int? = null,

	@SerialName("is_playable")
	val isPlayable: Boolean? = null,

	@SerialName("artists")
	val artists: List<ArtistsItem?>? = null,

	@SerialName("release_date")
	val releaseDate: String? = null,

	@SerialName("name")
	val name: String? = null,

	@SerialName("album_type")
	val albumType: String? = null,

	@SerialName("href")
	val href: String? = null,

	@SerialName("id")
	val id: String? = null,

	@SerialName("external_urls")
	val externalUrls: ExternalUrls? = null
)
