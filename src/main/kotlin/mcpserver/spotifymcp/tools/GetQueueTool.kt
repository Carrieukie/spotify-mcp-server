package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyGetQueueTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
        Retrieves the current Spotify playback queue.
        
        Use this tool when the user asks about what songs are coming up next, or wants to see the current queue.
        
        No input is required for this tool.
        
        Examples of when to use:
        - "What's next on Spotify?"
        - "Show me the queue"
        - "What songs are coming up?"
    """.trimIndent()

    server.addTool(
        name = "get-queue-spotify",
        description = toolDescription,
    ) {
        val result = when (val res = spotifyPlayerService.getQueue()) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Failed to retrieve the queue: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val successMessage = "Current queue: ${res.data}"
                println("Success: $successMessage")
                successMessage
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}



