package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySkipToPrevTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Skips to the previous track on Spotify.
        
        Use this tool when the user asks to go back to the last song or replay the current track. This works only if there is track history or something is currently playing.
        
        No input is required for this tool.
        
        Examples of when to use:
        - "Go back to the previous song"
        - "Play the last track again"
        - "Skip back on Spotify"
    """.trimIndent()

    server.addTool(
        name = "skip-to-prev-spotify",
        description = toolDescription
    ) {
        val result = when (val res = spotifyPlayerService.skipToPreviousTrack()) {
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
