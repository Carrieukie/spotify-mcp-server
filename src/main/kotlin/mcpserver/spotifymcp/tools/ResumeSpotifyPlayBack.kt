package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addResumeSpotifyPlayBack(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    server.addTool(
        name = "resume-spotify-playback",
        description = "Resumes playback on the user's currently active Spotify device.",
        inputSchema = Tool.Input(properties = buildJsonObject { })
    ) {
        val result = when (val res = spotifyPlayerService.playTrack()) {
            is SpotifyResult.Success -> "Playback resumed."
            is SpotifyResult.Failure -> "Failed to resume playback: ${res.exception}"
        }
        CallToolResult(listOf(TextContent(result)))
    }
}
