package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyPausePlaybackTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Pauses the currently playing track on Spotify.
        
        Use this tool when the user asks to pause music, stop playback, or take a break from listening. This works only if something is currently playing.
        
        No input is required for this tool.
        
        Examples of when to use:
        - "Pause the music"
        - "Can you stop the song for a bit?"
        - "Hold on, pause Spotify"
        """.trimIndent()
    server.addTool(
        name = "pause-spotify",
        description = toolDescription
    ) {
        val result = when (val res = spotifyPlayerService.pausePlayback()) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val successMessage = "Paused track: ${res.data}"
                println("Success: $successMessage")
                successMessage
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}