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

fun addSpotifyPlayResumeTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Plays sone music.

        Use this tool when the user wants to listen to music """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("trackUri") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "string")
                }
                put("description", "List of Spotify track URIs to play. If empty, tool continues the previous playback.")
            }
        },
    )

    server.addTool(
        name = "play-or-resume-spotify",
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