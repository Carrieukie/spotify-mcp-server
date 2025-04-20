package mcpserver

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import mcpserver.spotify.auth.authmanager.SpotifyTokenManagerImpl
import mcpserver.spotify.auth.tokenstorage.FileTokenStorageImpl
import mcpserver.spotify.services.playerservice.SpotifyPlayerServiceImpl
import mcpserver.spotify.services.userservice.SpotifyUserServiceImpl
import mcpserver.spotify.services.userservice.storage.FileUserProfileStorage
import mcpserver.spotifymcp.createServer
import java.io.File

// Setup Spotify API
val tokenManager = SpotifyTokenManagerImpl(
    tokenStorage = FileTokenStorageImpl(File("tokens.json"))
)
val userProfileStorage = FileUserProfileStorage(File("userprofile.json"))
val spotifyPlayerApi = SpotifyPlayerServiceImpl(tokenManager = tokenManager)
val spotifyUserApi = SpotifyUserServiceImpl(tokenManager = tokenManager, storage = userProfileStorage)


suspend fun main() {
    runSseMcpServerUsingKtorPlugin(port = 8080)
//    // Example 1: Play a track
//    println("Playing tracks...")
    val playResult = spotifyPlayerApi.playPlaylist(

    )



//     Run the MCP server
    runMcpServerUsingStdio()
//    runSseMcpServerUsingKtorPlugin(8080)
}

fun runMcpServerUsingStdio() = runBlocking {
    val server = createServer()
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )

    val shutdownSignal = CompletableDeferred<Unit>()
    server.onClose { shutdownSignal.complete(Unit) }

    server.connect(transport)
    shutdownSignal.await()
}

fun runSseMcpServerUsingKtorPlugin(port: Int): Unit = runBlocking {
    println("Starting sse server on port $port")
    println("Use inspector to connect to the http://localhost:$port/sse")

    embeddedServer(CIO, host = "0.0.0.0", port = port) {
        mcp {
            return@mcp createServer()
        }
    }.start(wait = true)
}
