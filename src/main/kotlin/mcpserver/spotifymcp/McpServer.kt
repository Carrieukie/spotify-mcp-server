package mcpserver.spotifymcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import mcpserver.spotify.auth.tokenstorage.FileTokenStorageImpl
import mcpserver.spotify.auth.authmanager.SpotifyTokenManagerImpl
import mcpserver.spotify.services.playerservice.SpotifyPlayerServiceImpl
import mcpserver.spotifymcp.tools.*
import java.io.File

fun createServer(): Server {
    val info = Implementation(
        "Spotify Kotlin MCP",
        "1.0.0"
    )
    val options = ServerOptions(
        capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(true))
    )
    val server = Server(info, options)

    // Setup Spotify API
    val tokenManager = SpotifyTokenManagerImpl(
        tokenStorage = FileTokenStorageImpl(File("tokens.json"))
    )
    val spotifyApi = SpotifyPlayerServiceImpl(tokenManager =tokenManager)

    // Register tools
    addSpotifyPlayTool(server, spotifyApi)
    addSpotifyPausePlaybackTool(server, spotifyApi)
    addSpotifySearchTool(server, spotifyApi)
    addSpotifySkipToPrevTool(server, spotifyApi)
    addSpotifySkipToNextTool(server, spotifyApi)

    return server
}
