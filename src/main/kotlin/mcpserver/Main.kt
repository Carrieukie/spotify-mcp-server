package mcpserver

import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import mcpserver.spotifymcp.createServer
import mcpserver.spotify.auth.tokenstorage.FileTokenStorageImpl
import mcpserver.spotify.auth.authmanager.SpotifyTokenManagerImpl
import mcpserver.spotify.services.playerservice.SpotifyPlayerServiceImpl
import java.io.File

// Setup Spotify API
val tokenManager = SpotifyTokenManagerImpl(
    tokenStorage = FileTokenStorageImpl(File("tokens.json"))
)
val spotifyApi = SpotifyPlayerServiceImpl(tokenManager = tokenManager)


suspend fun main()   {
//    spotifyApi.playTrack(
//        listOf(
////            "spotify:track:3eNZcLsKbLIAFJRKiVrerK",
////            "spotify:track:0jV229QHtkNuYxC3gFD8Vg"
//        )
//    )
    runBlocking {
        val server = createServer()
        val transport = StdioServerTransport(
            inputStream = System.`in`.asSource().buffered(),
            outputStream = System.out.asSink().buffered()
        )

        val shutdownSignal = CompletableDeferred<Unit>()
        server.onCloseCallback = { shutdownSignal.complete(Unit) }

        server.connect(transport)
        shutdownSignal.await()
    }
}

