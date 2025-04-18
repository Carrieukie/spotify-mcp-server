package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.*
import mcpserver.spotify.spotifyapi.SpotifyApi
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySearchTool(server: Server, spotifyApi: SpotifyApi) {
    val toolDescription = """
        Searches for a track on Spotify based on a keyword, phrase, or lyrics.

        Use this tool when a user wants to play a song but did not provide a Spotify URI. This tool returns a list of tracks matching the search query. Each track contains metadata such as the Spotify URI, name, and artist.

        Use the URI(s) from this tool's results as input for the `play-spotify` tool.
        
        Use this tool when a user wants to find a song but hasn't given a Spotify URI. This is useful when the user provides:
        - A song name (e.g. "Shape of You")
        - An artist name (e.g. "Burna Boy")
        - A combination of artist and song (e.g. "Adele Hello")
        - Lyrics (e.g. "I'm in love with the shape of you")
        
        Example input:
        { "query": "Calm Down Rema" }
        Use this before playing a track, if the user did not provide a Spotify URI.
        """.trimIndent()
    // Define proper JSON Schema for input

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("query") {
                put("type", "string")
                put("description", "It accepts a search query (song title, artist name, combination of both) and returns a list of tracks that match.")
            }
        },
    )

    server.addTool(
        name = "search-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val query = input.arguments["query"]?.jsonPrimitive?.content ?: ""
        val result = when (val res = spotifyApi.searchForTrack(query)) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception.message}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val successMessage = "Search results: ${res.data.tracks}"
                println("Success: $successMessage")
                successMessage
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}

