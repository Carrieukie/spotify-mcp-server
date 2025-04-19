package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mcpserver.spotify.services.playlistservice.SpotifyPlaylistService
import mcpserver.spotify.services.playlistservice.model.SpotifyRemoveTracksRequest
import mcpserver.spotify.services.playlistservice.model.TrackUri
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyRemovePlaylistTracksTool(server: Server, spotifyPlaylistService: SpotifyPlaylistService) {
    val toolDescription = """
        Removes one or more tracks from a specific Spotify playlist.
        
        Use this tool when the user wants to remove tracks from a playlist.
        
        Required parameters:
        - playlist_id: The Spotify ID of the playlist
        - track_uris: A comma-separated list of Spotify track URIs to remove
        
        Optional parameters:
        - snapshot_id: The playlist's snapshot ID
        
        Examples of when to use:
        - "Remove a track from my playlist"
        - "Delete the song with URI spotify:track:123456 from my playlist with ID 3cEYpjA9oz9GiPac4AsH4n"
        - "Remove multiple tracks from my playlist"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("playlist_id") {
                put("type", "string")
                put("description", "The Spotify ID of the playlist")
            }
            putJsonObject("track_uris") {
                put("type", "string")
                put("description", "A comma-separated list of Spotify track URIs to remove")
            }
            putJsonObject("snapshot_id") {
                put("type", "string")
                put("description", "The playlist's snapshot ID (optional)")
            }
        },
        required = listOf("playlist_id", "track_uris")
    )

    server.addTool(
        name = "remove-playlist-tracks-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<RemovePlaylistTracksPayload>(input.arguments.toString())
        val playlistId = payload.playlistId
        val trackUris = payload.trackUris.split(",").map { it.trim() }
        val snapshotId = payload.snapshotId

        val request = SpotifyRemoveTracksRequest(
            tracks = trackUris.map { TrackUri(uri = it) },
            snapshotId = snapshotId
        )

        val result = when (val res = spotifyPlaylistService.removePlaylistTracks(playlistId, request)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Failed to remove tracks from playlist: ${res.exception.error?.message}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val response = res.data
                val newSnapshotId = response.snapshotId ?: "Unknown"
                
                val message = """
                |Successfully removed ${trackUris.size} track(s) from playlist.
                |New snapshot ID: $newSnapshotId
                """.trimMargin()

                println("Success: Removed ${trackUris.size} track(s) from playlist")
                message
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class RemovePlaylistTracksPayload(
    @SerialName("playlist_id")
    val playlistId: String,
    
    @SerialName("track_uris")
    val trackUris: String,
    
    @SerialName("snapshot_id")
    val snapshotId: String? = null
)