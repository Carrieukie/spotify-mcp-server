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
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksRequest
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyAddPlaylistTracksTool(server: Server, spotifyPlaylistService: SpotifyPlaylistService) {
    val toolDescription = """
        Adds one or more tracks to a user's Spotify playlist.
        
        Use this tool when the user wants to add tracks to a playlist.
        
        Required parameters:
        - playlist_id: The Spotify ID of the playlist
        - track_uris: A comma-separated list of Spotify track URIs to add
        
        Optional parameters:
        - position: The position to insert the tracks (zero-based index, default: appends to the end)
        
        Examples of when to use:
        - "Add a track to my playlist"
        - "Add the song with URI spotify:track:4iV5W9uYEdYUVa79Axb7Rh to my playlist with ID 3cEYpjA9oz9GiPac4AsH4n"
        - "Add multiple tracks to my playlist at position 0"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("playlist_id") {
                put("type", "string")
                put("description", "The Spotify ID of the playlist")
            }
            putJsonObject("track_uris") {
                put("type", "string")
                put("description", "A comma-separated list of Spotify track URIs to add")
            }
            putJsonObject("position") {
                put("type", "integer")
                put("description", "The position to insert the tracks (zero-based index, default: appends to the end)")
            }
        },
        required = listOf("playlist_id", "track_uris")
    )

    server.addTool(
        name = "add-playlist-tracks-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<AddPlaylistTracksPayload>(input.arguments.toString())
        val playlistId = payload.playlistId
        val trackUris = payload.trackUris.split(",").map { it.trim() }
        val position = payload.position

        val request = SpotifyAddTracksRequest(
            uris = trackUris,
            position = position
        )

        val result = when (val res = spotifyPlaylistService.addPlaylistTracks(playlistId, request)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Failed to add tracks to playlist: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val response = res.data
                val snapshotId = response.snapshotId ?: "Unknown"
                
                val positionText = if (position != null) {
                    "at position $position"
                } else {
                    "at the end of the playlist"
                }
                
                val message = """
                |Successfully added ${trackUris.size} track(s) to playlist $positionText.
                |Snapshot ID: $snapshotId
                """.trimMargin()

                println("Success: Added ${trackUris.size} track(s) to playlist")
                message
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class AddPlaylistTracksPayload(
    @SerialName("playlist_id")
    val playlistId: String,
    
    @SerialName("track_uris")
    val trackUris: String,
    
    @SerialName("position")
    val position: Int? = null
)