package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mcpserver.spotify.services.playerservice.SpotifyPlayerService
import mcpserver.spotify.utils.networkutils.SpotifyResult

fun addSpotifyPlayTool(server: Server, spotifyPlayerService: SpotifyPlayerService) {
    val toolDescription = """
    Starts or resumes playback of Spotify content (tracks, albums, playlists) on the user's active or specified device.

    It supports:
    - Starting playback of specific tracks (`trackUri`)
    - Playing a full album, artist, or playlist (`contextUri`)
    - Resuming playback from the user's last session (no content input)

    Use this tool when the user expresses a desire to play or resume something, and you have valid Spotify URIs. 
    If URIs are not provided, use the `search-spotify` tool first to locate the desired content.
    If the user simply says "resume" or "continue," call this tool with an empty body ie {} .

    Input Parameters:
    - `trackUri` (optional): A list of Spotify track URIs to play. Cannot be used with `contextUri`.
    - `contextUri` (optional): A Spotify URI representing an album, artist, or playlist. Cannot be used with `trackUri`.
    - `offset` (optional): Starting point within the context.
        - `"position"` (integer): 0-based index of the item in the context.
        - `"uri"` (string): URI of the track within the context to begin playback from.
    - `positionMs` (optional): Starting position in milliseconds within the first track.
    - `deviceId` (optional): Spotify device ID to target. If omitted, playback starts on the user’s currently active device.

    Notes:
    - Only valid Spotify URIs should be provided.
    - Do not use this tool to perform search or natural language interpretation — use a search tool instead.
    - To resume playback, omit `trackUri` and `contextUri`.

    Examples:
    - Resume playback on the current active device when no input is provided:
      { }
    - Play a single track:
      { "trackUri": ["spotify:track:7GhIk7Il098yCjg4BQjzvb"] }

    - Play multiple tracks:
      { "trackUri": ["spotify:track:7GhIk7Il098yCjg4BQjzvb", "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp"] }

    - Play an album from the 6th track:
      { "contextUri": "spotify:album:5ht7ItJgpBH7W6vJ5BqpPr", "offset": {"position": 5} }

    - Play a playlist from a specific track:
      { "contextUri": "spotify:playlist:37i9dQZF1DX4sWSpwq3LiO", "offset": {"uri": "spotify:track:1301WleyT98MSxVHPZCA6M"} }

    Reference: https://developer.spotify.com/documentation/web-api/reference/start-a-users-playback
""".trimIndent()


    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("trackUri") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "string")
                }
                put("description", "List of Spotify track URIs to play. Use this when playing individual tracks.")
            }
            put("contextUri", buildJsonObject {
                put("type", "string")
                put("description", "A Spotify URI for an album, artist, or playlist. Use this when playing an entire album or playlist.")
            })
            put("offset", buildJsonObject {
                put("type", "object")
                put("description", "Indicates from where in the context playback should start. Can specify either a position (integer) or a URI.")
            })
            put("positionMs", buildJsonObject {
                put("type", "integer")
                put("description", "Position in milliseconds to start playback from.")
            })
            put("deviceId", buildJsonObject {
                put("type", "string")
                put("description", "The ID of the device to play on. If not provided, the user's currently active device is used.")
            })
        },
    )

    server.addTool(
        name = "play-spotify",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val payload = Json.decodeFromString<TrackUriPayload>(input.arguments.toString())

        // Convert JsonObject to Map<String, Any> if offset is provided
        val offsetMap = payload.offset?.let { jsonObject ->
            jsonObject.toMap()
        }

        val result = when (val res = spotifyPlayerService.playTrack(
            trackUris = payload.trackUri,
            contextUri = payload.contextUri,
            offset = offsetMap,
            positionMs = payload.positionMs,
            deviceId = payload.deviceId
        )) {
            is SpotifyResult.Failure -> {
                val errorMessage = "Something went wrong: ${res.exception}"
                println("Error: $errorMessage")
                errorMessage
            }

            is SpotifyResult.Success -> {
                val successMessage = if (payload.contextUri != null) {
                    "Successfully started playback of ${payload.contextUri}"
                } else if (payload.trackUri.isNotEmpty()) {
                    "Successfully played the track(s)"
                } else {
                    "Successfully started playback"
                }
                println("Success: $successMessage")
                successMessage
            }
        }
        CallToolResult(listOf(TextContent(result)))
    }
}

@Serializable
data class TrackUriPayload(
    @SerialName("trackUri")
    val trackUri: List<String> = listOf(),

    @SerialName("contextUri")
    val contextUri: String? = null,

    @SerialName("offset")
    val offset: JsonObject? = null,

    @SerialName("positionMs")
    val positionMs: Int? = null,

    @SerialName("deviceId")
    val deviceId: String? = null
)

/**
 * Extension function to convert a JsonObject to a Map<String, Any>
 */
fun JsonObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    for ((key, element) in this.entries) {
        map[key] = element.toAny()
    }
    return map
}

/**
 * Extension function to convert a JsonElement to its corresponding Kotlin type
 */
private fun JsonElement.toAny(): Any {
    return when (this) {
        is JsonPrimitive -> {
            when {
                this.booleanOrNull != null -> this.booleanOrNull!!
                this.intOrNull != null -> this.intOrNull!!
                this.longOrNull != null -> this.longOrNull!!
                this.doubleOrNull != null -> this.doubleOrNull!!
                else -> this.toString().removeSurrounding("\"")
            }
        }
        is JsonObject -> this.toMap()
        else -> toString()
    }
}
