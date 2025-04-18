package mcpserver.modelcontextprotocol.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import mcpserver.spotify.spotifyapi.SpotifyApi
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyPlayTool(server: Server, spotifyApi: SpotifyApi) {
    server.addTool(
        name = "play-spotify",
        description = "Play the spotify"
    ) {
        val result = when (val res = spotifyApi.playTrack()) {
            is SpotifyResult.Failure -> {
                println("Error: ${res.exception.error?.message}")
                res.exception.error?.message ?: "Something went wrong"
            }
            is SpotifyResult.Success -> {
                println("Success: ${res.data}")
                res.data
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}