package mcpserver.spotifymcp.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import mcpserver.spotify.services.userservice.SpotifyUserService
import mcpserver.spotify.utils.networkutils.SpotifyResult

/**
 * Adds a tool to get the current user's Spotify profile
 */
fun addGetUserProfileTool(server: Server, spotifyUserService: SpotifyUserService) {
    val toolDescription = """
    Retrieves the current user's Spotify profile information using the Spotify Web API.

    This includes:
    - Display name
    - User ID
    - Number of followers
    - Spotify URI

    The User ID can be used to access additional user-related resources such as public playlists, 
    as specified in the Spotify Web API documentation
""".trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            // No input properties needed for this tool
        }
    )

    server.addTool(
        name = "get-user-profile",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input ->
        val result = spotifyUserService.getCurrentUserProfile()

        when (result) {
            is SpotifyResult.Success -> {
                val profile = result.data
                val response = """
                    User Profile:
                    Display Name: ${profile.displayName ?: "N/A"}
                    User ID: ${profile.userId}
                    Followers: ${profile.followers?.total ?: 0}
                    Spotify URI: ${profile.uri ?: "N/A"}
                """.trimIndent()

                CallToolResult(listOf(TextContent(response)))
            }

            is SpotifyResult.Failure -> {
                val errorMessage = result.exception.error?.message ?: "Unknown error"
                CallToolResult(listOf(TextContent("Failed to get user profile: $errorMessage")))
            }
        }
    }
}
