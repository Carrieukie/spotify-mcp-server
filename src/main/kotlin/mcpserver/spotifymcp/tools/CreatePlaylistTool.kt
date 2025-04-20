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
import mcpserver.spotify.services.playlistservice.model.SpotifyCreatePlaylistRequest
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyCreatePlaylistTool(server: Server, spotifyPlaylistService: SpotifyPlaylistService) {
    val toolDescription = """
        Creates a new Spotify playlist for the specified user.
        
        Use this tool when the user wants to create a new playlist on Spotify.
        
        Required parameters:
        - userId: The Spotify user ID for whom to create the playlist ie qetbl2jvi1d4m1usve88jxgcp
        - name: The name for the new playlist
        
        Optional parameters:
        - description: A description for the playlist
        - public: Whether the playlist should be public (default: false)
        - collaborative: Whether the playlist should be collaborative (default: false)
        
        Examples of when to use:
        - "Create a new playlist for me"
        - "Make a playlist called 'My Favorites'"
        - "I want a new private playlist named 'Workout Mix' with the description 'Songs for the gym'"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("userId") {
                put("type", "string")
                put("description", "The Spotify user ID for whom to create the playlist")
            }
            putJsonObject("name") {
                put("type", "string")
                put("description", "The name for the new playlist")
            }
            putJsonObject("description") {
                put("type", "string")
                put("description", "A description for the playlist")
            }
            putJsonObject("public") {
                put("type", "boolean")
                put("description", "Whether the playlist should be public (default: false)")
            }
            putJsonObject("collaborative") {
                put("type", "boolean")
                put("description", "Whether the playlist should be collaborative (default: false)")
            }
        },
        required = listOf("userId", "name")
    )

    server.addTool(
        name = "create-playlist-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<CreatePlaylistPayload>(input.arguments.toString())
        
        val request = SpotifyCreatePlaylistRequest(
            name = payload.name,
            description = payload.description,
            public = payload.public,
            collaborative = payload.collaborative
        )

        val result = when (val res = spotifyPlaylistService.createPlaylist(payload.userId, request)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Failed to create playlist: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val playlist = res.data
                val message = """
                    |Successfully created playlist:
                    |
                    |Name: ${playlist.name}
                    |Description: ${playlist.description ?: "No description"}
                    |Owner: ${playlist.owner?.displayName ?: "Unknown"}
                    |ID: ${playlist.id}
                    |URI: ${playlist.uri}
                    |${if (playlist.public == true) "Public" else "Private"}
                    |${if (playlist.collaborative == true) "Collaborative" else "Not collaborative"}
                    |
                    |The playlist is currently empty. You can add tracks to it using the add-tracks-to-playlist tool.
                """.trimMargin()

                println("Success: Created playlist '${playlist.name}' with ID ${playlist.id}")
                message
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class CreatePlaylistPayload(
    @SerialName("userId")
    val userId: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("public")
    val public: Boolean? = false,
    
    @SerialName("collaborative")
    val collaborative: Boolean? = false
)