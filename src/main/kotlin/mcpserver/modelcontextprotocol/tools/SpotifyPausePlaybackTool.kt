package mcpserver.modelcontextprotocol.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import mcpserver.spotify.spotifyapi.SpotifyApi
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyPausePlaybackTool(server: Server, spotifyApi: SpotifyApi) {
    server.addTool(
        name = "pause-spotify",
        description = "Pause the Spotify playback"
    ) {
        val result = when (val res = spotifyApi.pausePlayback()) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception.message}"
                println("Error: $errorMessage")
                errorMessage
            }
            is SpotifyResult.Success -> {
                val successMessage = "Playing track: ${res.data}"
                println("Success: $successMessage")
                successMessage
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}