package mcpserver.modelcontextprotocol

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import mcpserver.modelcontextprotocol.tools.addAdditionTool
import mcpserver.modelcontextprotocol.tools.addSpotifyPlayTool
import mcpserver.spotify.authstuff.filetokenstorage.FileTokenStorageImpl
import mcpserver.spotify.authstuff.spotifytokenmanager.SpotifyTokenManagerImpl
import mcpserver.spotify.spotifyapi.SpotifyApiImpl
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
    val spotifyApi = SpotifyApiImpl(tokenManager =tokenManager)

    // Register tools
    addAdditionTool(server)
    addSpotifyPlayTool(server, spotifyApi)

    return server
}
