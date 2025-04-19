package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.*
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySetVolumeTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Sets the Spotify volume to a specific level (from 0 to 100).
        
        Use this tool when the user asks to lower, increase, mute, or set the playback volume.
        
        The input should include a field called `volume_percent`, which must be an integer between 0 and 100.
        
        Examples of when to use:
        - "Set the volume to 50%"
        - "Turn the music down to 10"
        - "Max out the volume"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("volume_percent") {
                put("type", "integer")
                put("description", "The volume to set, between 0 and 100 inclusive.")
            }
        },
        required = listOf("volume_percent")
    )

    server.addTool(
        name = "set-volume-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val volumePercent = input.arguments["volume_percent"]?.jsonPrimitive?.intOrNull
        val result = if (volumePercent != null && (volumePercent in 0..100)) {
            when (val res = spotifyPlayerService.setVolume(volumePercent)) {
                is SpotifyResult.Failure -> {
                    val errorMessage = "Failed to set volume: ${res.exception}"
                    println("Error: $errorMessage")
                    errorMessage
                }

                is SpotifyResult.Success -> {
                    val successMessage = "Volume set to $volumePercent%"
                    println("Success: $successMessage")
                    successMessage
                }
            }
        } else {
            "Invalid volume input. Please provide an integer between 0 and 100."
        }

        CallToolResult(listOf(TextContent(result)))
    }
}
