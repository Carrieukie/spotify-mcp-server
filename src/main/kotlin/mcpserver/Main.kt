package mcpserver

import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import mcpserver.spotifymcp.createServer
import mcpserver.spotify.authstuff.filetokenstorage.FileTokenStorageImpl
import mcpserver.spotify.authstuff.spotifytokenmanager.SpotifyTokenManagerImpl
import mcpserver.spotify.spotifyapi.SpotifyApiImpl
import java.io.File

// Setup Spotify API
val tokenManager = SpotifyTokenManagerImpl(
    tokenStorage = FileTokenStorageImpl(File("tokens.json"))
)
val spotifyApi = SpotifyApiImpl(tokenManager = tokenManager)


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

