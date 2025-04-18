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
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyPlayTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Plays one or more Spotify tracks using their Spotify track URIs.

        Use this tool when the user wants to listen to specific track(s) and you have their Spotify URIs. If the user didn’t provide URIs directly, use the `search-spotify` tool first to find them.

        Input:
        - `trackUri` (optional): A list of Spotify track URIs to play. If omitted or empty, playback will default to the most recently played item or the current queue.
        
        Only valid Spotify track URIs should be provided. Do not attempt to search for songs or convert text to URIs using this tool — use a separate search tool for that.
        
        Examples:
        - Play a single track:
          { "trackUri": ["spotify:track:7GhIk7Il098yCjg4BQjzvb"] }
          
        - Play multiple tracks:
          { "trackUri": ["spotify:track:7GhIk7Il098yCjg4BQjzvb", "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp"] }
        """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("trackUri") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "string")
                }
                put("description", "List of Spotify track URIs to play. If empty, no tracks will be played.")
            }
        },
    )

    server.addTool(
        name = "play-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val songs = Json.decodeFromString<TrackUriPayload>(input.arguments.toString()).trackUri

        val result = when (val res = spotifyPlayerService.playTrack(songs)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val successMessage = "Successfully played the track: ${res.data}"
                println("Success: $successMessage")
                successMessage
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class TrackUriPayload(

    @SerialName("trackUri")
    val trackUri: List<String> = listOf()
)
