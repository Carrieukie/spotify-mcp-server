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

fun addPlaySpotifyTrack(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    server.addTool(
        name = "play-spotify-track",
        description = """
        Plays one or more specific Spotify tracks on the user's active device.

        Use this when the user requests specific songs and you have their Spotify track URIs.
        """.trimIndent(),
        inputSchema = Tool.Input(properties = buildJsonObject {
            putJsonObject("trackUri") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "string")
                }
                put("description", "List of Spotify track URIs to play.")
            }
        })
    ) { input ->
        val payload = Json.decodeFromString<TrackPayload>(input.arguments.toString())
        val result = when (val res = spotifyPlayerService.playTrack(payload.trackUri)) {
            is SpotifyResult.Success -> "Playing track(s): ${res.data}"
            is SpotifyResult.Failure -> "Error playing track(s): ${res.exception}"
        }
        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class TrackPayload(
    @SerialName("trackUri") val trackUri: List<String>
)

