package mcpserver.spotifymcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import mcpserver.spotify.auth.tokenstorage.FileTokenStorageImpl
import mcpserver.spotify.auth.authmanager.SpotifyTokenManagerImpl
import mcpserver.spotify.services.playerservice.SpotifyPlayerServiceImpl
import mcpserver.spotify.services.userservice.SpotifyUserServiceImpl
import mcpserver.spotify.services.userservice.storage.FileUserProfileStorage
import mcpserver.spotify.services.playlistservice.SpotifyPlaylistServiceImpl
import mcpserver.spotifymcp.tools.*
import java.io.File

fun createServer(): Server {
    val info = Implementation(
        "Spotify Kotlin MCP",
        "1.0.0",
    )
    val options = ServerOptions(
        capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(true))
    )
    val server = Server(info, options)

    // Setup Spotify API
    val tokenManager = SpotifyTokenManagerImpl(
        tokenStorage = FileTokenStorageImpl(File("tokens.json"))
    )
    val spotifyPlayerApi = SpotifyPlayerServiceImpl(tokenManager = tokenManager)
    val spotifyPlaylistApi = SpotifyPlaylistServiceImpl(tokenManager = tokenManager)
    val spotifyUserApi = SpotifyUserServiceImpl(
        tokenManager = tokenManager,
        storage = FileUserProfileStorage(File("userprofile.json"))
    )

    // Register tools
    addSpotifyPausePlaybackTool(server, spotifyPlayerApi)
    addSpotifySearchTool(server, spotifyPlayerApi)
    addSpotifySkipToPrevTool(server, spotifyPlayerApi)
    addSpotifySkipToNextTool(server, spotifyPlayerApi)
    addSpotifySetVolumeTool(server, spotifyPlayerApi)
    addSpotifySeekToPositionTool(server, spotifyPlayerApi)
    addSpotifyGetQueueTool(server, spotifyPlayerApi)
    addSpotifySetRepeatModeTool(server, spotifyPlayerApi)
    addPlaySpotifyPlaylist(server, spotifyPlayerApi)
    addPlaySpotifyTrack(server, spotifyPlayerApi)
    addResumeSpotifyPlayBack(server, spotifyPlayerApi)

    addGetUserProfileTool(server, spotifyUserApi)

    addSpotifyGetPlaylistsTool(server, spotifyPlaylistApi)
    addSpotifyGetPlaylistItemsTool(server, spotifyPlaylistApi)
    addSpotifyRemovePlaylistTracksTool(server, spotifyPlaylistApi)
    addSpotifyAddPlaylistTracksTool(server, spotifyPlaylistApi)
    addSpotifyCreatePlaylistTool(server, spotifyPlaylistApi)

    return server
}
