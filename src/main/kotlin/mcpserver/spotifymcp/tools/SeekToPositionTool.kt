package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.*
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySeekToPositionTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Seeks to a specific position (in milliseconds) within the currently playing track on Spotify.
        
        Use this tool when the user asks to rewind, fast-forward, or jump to a certain part of the song.
        
        The input must include a `position_ms` field, which should be a positive integer. 
        If the position is longer than the track, playback will continue from the next song.
        
        Examples of when to use:
        - "Skip to 25 seconds"
        - "Go back to the beginning"
        - "Jump to 1 minute 15 seconds"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("position_ms") {
                put("type", "integer")
                put("description", "The position in milliseconds to seek to. Must be a positive number.")
            }
        },
        required = listOf("position_ms")
    )

    server.addTool(
        name = "seek-to-position-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val positionMs = input.arguments["position_ms"]?.jsonPrimitive?.intOrNull
        val result = if (positionMs != null && positionMs >= 0) {
            when (val res = spotifyPlayerService.seekToPosition(positionMs)) {
                is SpotifyResult.Failure -> {
                    val errorMessage = "Failed to seek to position: ${res.exception}"
                    println("Error: $errorMessage")
                    errorMessage
                }

                is SpotifyResult.Success -> {
                    val successMessage = "Playback position set to ${positionMs}ms"
                    println("Success: $successMessage")
                    successMessage
                }
            }
        } else {
            "Invalid input. Please provide a non-negative number for `position_ms`."
        }

        CallToolResult(listOf(TextContent(result)))
    }
}
