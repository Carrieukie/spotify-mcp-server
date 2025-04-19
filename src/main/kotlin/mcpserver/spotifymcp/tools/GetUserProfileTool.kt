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
        Retrieves the current user's Spotify profile information via the Spotify Web API.

        This includes:
        - Display name
        - User ID
        - Number of followers
        - Spotify URI

        The retrieved User ID can be used to:
        - Access the user's public playlists (e.g., via `GET /users/{user_id}/playlists`)
        - Reference the user in collaborative playlist endpoints
        - Display profile links using their Spotify URI

        Use this tool when you need to identify the current user, personalize responses, 
        or retrieve their content, such as playlists or followers.

        Reference: https://developer.spotify.com/documentation/web-api/reference/get-current-users-profile
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
