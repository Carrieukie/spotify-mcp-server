package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.*
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySetRepeatModeTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Sets the Spotify repeat mode for playback.
        
        Use this tool when the user asks to repeat the current track, repeat the playlist/context, or turn off repeat.
        
        The input should include a field called `state`, which must be one of:
        - "track" to repeat the current song
        - "context" to repeat the current playlist/album
        - "off" to turn repeat off
        
        Examples of when to use:
        - "Repeat this song"
        - "Loop the playlist"
        - "Turn off repeat"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("state") {
                put("type", "string")
                put("description", "Repeat mode: 'track', 'context', or 'off'")
                putJsonArray("enum") {
                    add("track")
                    add("context")
                    add("off")
                }
            }
        },
        required = listOf("state")
    )

    server.addTool(
        name = "set-repeat-mode-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val repeatState = input.arguments["state"]?.jsonPrimitive?.contentOrNull
        val result = if (repeatState in listOf("track", "context", "off")) {
            when (val res = spotifyPlayerService.setRepeatMode(repeatState!!)) {
                is SpotifyResult.Failure -> {
                    val errorMessage = "Failed to set repeat mode: ${res.exception}"
                    println("Error: $errorMessage")
                    errorMessage
                }

                is SpotifyResult.Success -> {
                    val successMessage = "${res.data} Repeat mode set to '$repeatState'"
                    println("Success: $successMessage")
                    successMessage
                }
            }
        } else {
            "Invalid repeat mode. Please choose 'track', 'context', or 'off'."
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

