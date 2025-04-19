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
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyGetPlaylistItemsTool(server: Server, spotifyPlaylistService: SpotifyPlaylistService) {
    val toolDescription = """
        Retrieves the items (tracks) of a specific Spotify playlist.
        
        Use this tool when the user asks about the tracks in a specific playlist.
        
        Required parameters:
        - playlist_id: The Spotify ID of the playlist
        
        Optional parameters:
        - limit: The maximum number of items to return (default: 20, max: 50)
        - offset: The index of the first item to return (default: 0)
        
        Examples of when to use:
        - "Show me the tracks in my playlist"
        - "What songs are in my playlist with ID 3cEYpjA9oz9GiPac4AsH4n?"
        - "List the tracks in my playlist"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("playlist_id") {
                put("type", "string")
                put("description", "The Spotify ID of the playlist")
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "The maximum number of items to return (default: 20, max: 50)")
            }
            putJsonObject("offset") {
                put("type", "integer")
                put("description", "The index of the first item to return (default: 0)")
            }
        },
        required = listOf("playlist_id")
    )

    server.addTool(
        name = "get-playlist-items-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<PlaylistItemsPayload>(input.arguments.toString())
        val playlistId = payload.playlistId
        val limit = payload.limit ?: 20
        val offset = payload.offset ?: 0

        val result = when (val res = spotifyPlaylistService.getPlaylistItems(playlistId, limit, offset)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Failed to retrieve playlist items: ${res.exception.error?.message}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val playlistItems = res.data
                val totalItems = playlistItems.total ?: 0
                val items = playlistItems.items ?: emptyList()

                val tracksInfo = items.filterNotNull().mapIndexed { index, item ->
                    val track = item.track
                    val artists = track?.artists?.filterNotNull()?.joinToString(", ") { it.name ?: "Unknown Artist" } ?: "Unknown Artist"
                    val album = track?.album?.name ?: "Unknown Album"
                    val duration = track?.durationMs?.let { formatDuration(it) } ?: "Unknown Duration"
                    
                    """
                    |${index + 1 + offset}. ${track?.name ?: "Unknown Track"}
                    |   Artist: $artists
                    |   Album: $album
                    |   Duration: $duration
                    |   Added at: ${item.addedAt ?: "Unknown"}
                    |   ID: ${track?.id ?: "Unknown"}
                    |   URI: ${track?.uri ?: "Unknown"}
                    """.trimMargin()
                }.joinToString("\n\n")

                val message = if (items.isEmpty()) {
                    "No tracks found in this playlist."
                } else {
                    """
                    |Found ${items.size} tracks in playlist (total: $totalItems):
                    |
                    |$tracksInfo
                    """.trimMargin()
                }

                println("Success: Retrieved ${items.size} tracks from playlist")
                message
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class PlaylistItemsPayload(
    @SerialName("playlist_id")
    val playlistId: String,
    
    @SerialName("limit")
    val limit: Int? = null,
    
    @SerialName("offset")
    val offset: Int? = null
)

private fun formatDuration(durationMs: Int): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}