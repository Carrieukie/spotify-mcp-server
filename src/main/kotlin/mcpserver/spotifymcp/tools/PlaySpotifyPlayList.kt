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

fun addPlaySpotifyPlaylist(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    server.addTool(
        name = "play-spotify-playlist",
        description = """
        Plays a Spotify playlist on the user's active device.

        Use this when a Spotify playlist URI is available.
        """.trimIndent(),
        inputSchema = Tool.Input(properties = buildJsonObject {
            putJsonObject("playlistUri") {
                put("type", "string")
                put("description", "The Spotify playlist URI to play.")
            }
        })
    ) { input ->
        val payload = Json.decodeFromString<PlaylistPayload>(input.arguments.toString())
        val result = when (val res = spotifyPlayerService.playPlaylist(payload.playlistUri)) {
            is SpotifyResult.Success -> "Playing playlist: ${res.data}"
            is SpotifyResult.Failure -> "Error playing playlist: ${res.exception}"
        }
        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class PlaylistPayload(
    @SerialName("playlistUri") val playlistUri: String
)
