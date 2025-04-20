package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyPlayResumeTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
        val toolDescription = """
        Initiates playing songs, playlists or resumes Spotify playback on the user's currently active device.

        Use this tool when the user expresses an intent to **play** or **resume** content, and valid Spotify track URIs or playlist URI is available. 
        If no URIs are provided but intent to resume is clear (e.g. user says "resume", "continue", or "play where I left off"), 
        call this tool with an empty `trackUri` list: `{ "trackUri": [] }`.

        If track URIs or playlist URI are not available, use the `search-spotify` tool first to locate the desired content.

        Guidelines:
        - Only use **valid Spotify track URIs** in the `trackUri` array or a **valid Spotify playlist URI** in the `playlistUri` field.
        - Do **not** use this tool for searching or interpreting user intent — delegate that to the `search-spotify` tool.
        - To resume playback without selecting new tracks, pass an **empty array**: `{ "trackUri": [] }`.
        - If both `trackUri` and `playlistUri` are provided, `playlistUri` will take precedence.

        Examples:
        - Resume playback (no specific tracks given):
          `{ "trackUri": [] }`

        - Play a specific track:
          `{ "trackUri": ["spotify:track:7GhIk7Il098yCjg4BQjzvb"] }`

        - Play multiple tracks:
          `{ "trackUri": ["spotify:track:7GhIk7Il098yCjg4BQjzvb", "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp"] }`

        - Play a playlist:
          `{ "playlistUri": "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M" }`

        Example user commands:
        - "Resume Spotify"
        - "Continue playing music"
        - "Play the track Uptown Funk"
        - "Play Shape of You and Blinding Lights"
        - "Play my Discover Weekly playlist"
        - "Put on some chill beats"
        - "Start playing again"
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("trackUri") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "string")
                }
                put(
                    "description",
                    "List of Spotify track URIs to play. If empty, tool continues the previous playback."
                )
            }
            putJsonObject("playlistUri") {
                put("type", "string")
                put(
                    "description",
                    "Spotify playlist URI to play. If provided, this takes precedence over trackUri."
                )
            }
        },
    )

    server.addTool(
        name = "play-or-resume-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<TrackUriPayload>(input.arguments.toString())
        val songs = payload.trackUri
        val playlistUri = payload.playlistUri

        val result = if (playlistUri != null) {
            // If playlist URI is provided, play the playlist
            when (val res = spotifyPlayerService.playPlaylist(playlistUri)) {
                is SpotifyResult.Failure -> {
                    val errorMessage = "Something went wrong playing playlist: ${res.exception}"
                    println("Error: $errorMessage")
                    errorMessage
                }

                is SpotifyResult.Success -> {
                    val successMessage = "Successfully played the playlist: ${res.data}"
                    println("Success: $successMessage")
                    successMessage
                }
            }
        } else {
            // Otherwise, play the tracks
            when (val res = spotifyPlayerService.playTrack(songs)) {
                is SpotifyResult.Failure -> {
                    val errorMessage = "Something went wrong playing tracks: ${res.exception}"
                    println("Error: $errorMessage")
                    errorMessage
                }

                is SpotifyResult.Success -> {
                    val successMessage = "Successfully played the track(s): ${res.data}"
                    println("Success: $successMessage")
                    successMessage
                }
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class TrackUriPayload(

    @SerialName("trackUri")
    val trackUri: List<String> = listOf(),

    @SerialName("playlistUri")
    val playlistUri: String? = null
)
