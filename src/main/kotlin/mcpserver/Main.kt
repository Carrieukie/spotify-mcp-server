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
import mcpserver.spotify.services.userservice.SpotifyUserServiceImpl
import mcpserver.spotify.services.userservice.storage.FileUserProfileStorage
import mcpserver.spotify.utils.networkutils.SpotifyResult
import java.io.File

// Setup Spotify API
val tokenManager = SpotifyTokenManagerImpl(
    tokenStorage = FileTokenStorageImpl(File("tokens.json"))
)
val userProfileStorage = FileUserProfileStorage(File("userprofile.json"))
val spotifyPlayerApi = SpotifyPlayerServiceImpl(tokenManager = tokenManager)
val spotifyUserApi = SpotifyUserServiceImpl(tokenManager = tokenManager, storage = userProfileStorage)


suspend fun main() {
    // Example 1: Play a track
//    println("Playing tracks...")
//    val playResult = spotifyPlayerApi.playTrack(
//        listOf(
//            "spotify:track:3eNZcLsKbLIAFJRKiVrerK",
//            "spotify:track:0jV229QHtkNuYxC3gFD8Vg"
//        )
//    )

    // Example 2: Get user profile
    println("\nFetching user profile...")
    val userProfileResult = spotifyUserApi.getCurrentUserProfile()

    when (userProfileResult) {
        is SpotifyResult.Success -> {
            val userProfile = userProfileResult.data
            println("User Profile:")
            println("Display Name: ${userProfile.displayName}")
            println("User ID: ${userProfile.userId}")
            println("Followers: ${userProfile.followers?.total}")
            println("Spotify URI: ${userProfile.uri}")

            // Access the stored profile
            val storedProfile = userProfileStorage.getUserProfile()
            println("\nStored Profile:")
            println("Display Name: ${storedProfile?.displayName}")
            println("User ID: ${storedProfile?.userId}")
        }
        is SpotifyResult.Failure -> {
            println("Failed to get user profile: ${userProfileResult.exception.error?.message}")
        }
    }

    // Run the MCP server
//    runBlocking {
//        val server = createServer()
//        val transport = StdioServerTransport(
//            inputStream = System.`in`.asSource().buffered(),
//            outputStream = System.out.asSink().buffered()
//        )
//
//        val shutdownSignal = CompletableDeferred<Unit>()
//        server.onCloseCallback = { shutdownSignal.complete(Unit) }
//
//        server.connect(transport)
//        shutdownSignal.await()
//    }
}
