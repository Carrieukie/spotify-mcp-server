package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySetVolumeTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Skips to the next track on Spotify.
        
        Use this tool when the user asks to move on to the next song, skip the current one, or go forward in the playlist. This works only if something is currently playing or a queue exists.
        
        No input is required for this tool.
        
        Examples of when to use:
        - "Skip this song"
        - "Next track, please"
        - "Go to the next one on Spotify"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("query") {
                put("type", "string")
                put("description", "Search string (e.g. 'track:Calm Down artist:Rema'). You can use field filters.")
            }
        }
    )

    server.addTool(
        name = "set-volume-spotify",
        description = toolDescription
    ) {
        val result = when (val res = spotifyPlayerService.skipToNextTrack()) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val successMessage = "Successfully skipped to track: ${res.data}"
                println("Success: $successMessage")
                successMessage
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}
