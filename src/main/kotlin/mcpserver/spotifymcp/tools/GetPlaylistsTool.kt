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

fun addSpotifyGetPlaylistsTool(server: Server, spotifyPlaylistService: SpotifyPlaylistService) {
    val toolDescription = """
        Retrieves the current user's Spotify playlists.

        Use this tool when the user asks about their playlists or wants to see what playlists they have.

        Optional parameters:
        - limit: The maximum number of playlists to return (default: 20, max: 50)
        - offset: The index of the first playlist to return (default: 0)

        Examples of when to use:
        - "Show me my playlists"
        - "What playlists do I have on Spotify?"
        - "List my Spotify playlists"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "The maximum number of playlists to return (default: 20, max: 50)")
            }
            putJsonObject("offset") {
                put("type", "integer")
                put("description", "The index of the first playlist to return (default: 0)")
            }
        }
    )

    server.addTool(
        name = "get-my-playlists-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<PlaylistsPayload>(input.arguments.toString())
        val limit = payload.limit ?: 20
        val offset = payload.offset ?: 0

        val result = when (val res = spotifyPlaylistService.getCurrentUserPlaylists(limit, offset)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Failed to retrieve playlists: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val playlists = res.data
                val totalPlaylists = playlists.total ?: 0
                val playlistItems = playlists.items ?: emptyList()

                val playlistsInfo = playlistItems.filterNotNull().map { playlist ->
                    """
                    |Name: ${playlist.name}
                    |Description: ${playlist.description ?: "No description"}
                    |Owner: ${playlist.owner?.displayName ?: "Unknown"}
                    |Tracks: ${playlist.tracks?.total ?: 0}
                    |ID: ${playlist.id}
                    |URI: ${playlist.uri}
                    |${if (playlist.public == true) "Public" else "Private"}
                    """.trimMargin()
                }.joinToString("\n\n")

                val message = if (playlistItems.isEmpty()) {
                    "No playlists found."
                } else {
                    """
                    |Found ${playlistItems.size} playlists (total: $totalPlaylists):
                    |
                    |$playlistsInfo
                    """.trimMargin()
                }

                println("Success: Retrieved ${playlistItems.size} playlists")
                message
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class PlaylistsPayload(
    @SerialName("limit")
    val limit: Int? = null,

    @SerialName("offset")
    val offset: Int? = null
)
