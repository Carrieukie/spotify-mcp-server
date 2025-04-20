package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.*
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifySearchTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
    Searches Spotify for music or audio content based on a keyword, phrase, or lyrics.

    This tool supports flexible search across different types of content, including:
    - 🎵 Tracks (songs)
    - 👤 Artists
    - 💿 Albums
    - 📚 Audiobooks
    - 📻 Shows (like podcasts)
    - 🎙️ Episodes (individual podcast episodes)
    - 📜 Playlists

    You can filter results using advanced field filters like:
    - track, artist, album, genre, year, isrc, upc, tag:new, tag:hipster

    Use the `type` parameter to specify what you’re searching for (e.g. "track", "artist", or "playlist"). You can provide multiple types in a comma-separated string (e.g., "album,track").

    The tool returns results grouped under the following top-level keys in the response:
    - `tracks`, `artists`, `albums`, `playlists`, `shows`, `episodes`, `audiobooks`

    Example input:
    {
        "query": "track:Calm Down artist:Rema",
        "type": "track",
        "limit": 5,
        "market": "US"
    }

    Use the returned `uri` values from this tool to interact with playback tools like `play-spotify`.
""".trimIndent()


    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("query") {
                put("type", "string")
                put("description", "Search string (e.g. 'track:Calm Down artist:Rema'). You can use field filters.")
            }
            putJsonObject("type") {
                put("type", "string")
                put("enum", buildJsonArray {
                    add("album"); add("artist"); add("playlist"); add("track"); add("show"); add("episode"); add("audiobook")
                })
                put("description", "Comma-separated list of types to search: album, artist, track, etc.")
            }
            putJsonObject("market") {
                put("type", "string")
                put("description", "ISO 3166-1 alpha-2 country code (e.g., 'US', 'KE') to filter results available in that market.")
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "Number of results to return (default 20, max 50).")
            }
            putJsonObject("offset") {
                put("type", "integer")
                put("description", "The index of the first result to return (for pagination).")
            }
            putJsonObject("include_external") {
                put("type", "string")
                put("enum", buildJsonArray { add("audio") })
                put("description", "Include externally hosted audio content.")
            }
        }
    )

    server.addTool(
        name = "search-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val args = input.arguments
        val query = args["query"]?.jsonPrimitive?.content ?: return@addTool CallToolResult(listOf(TextContent("Missing required query")))
        val type = args["type"]?.jsonPrimitive?.content ?: "track"
        val market = args["market"]?.jsonPrimitive?.content
        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 20
        val offset = args["offset"]?.jsonPrimitive?.intOrNull ?: 0
        val includeExternal = args["include_external"]?.jsonPrimitive?.content

        val result = when (val res = spotifyPlayerService.search(
            query = query,
            type = type,
            market = market,
            limit = limit,
            offset = offset,
            includeExternal = includeExternal
        )) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }
            is SpotifyResult.Success -> {
                val tracks = res.data.toString()
                println("Success: $tracks")
                tracks
            }
        }

        CallToolResult(listOf(TextContent(result)))
    }
}

