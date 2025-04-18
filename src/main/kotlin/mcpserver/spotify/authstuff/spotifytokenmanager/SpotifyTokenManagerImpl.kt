package mcpserver.spotify.authstuff.spotifytokenmanager

import com.sun.net.httpserver.HttpServer
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import mcpserver.spotify.authstuff.filetokenstorage.FileTokenStorageImpl
import mcpserver.spotify.authstuff.filetokenstorage.model.TokenData
import mcpserver.spotify.authstuff.spotifytokenmanager.model.RefreshTokenResponse
import mcpserver.spotify.utils.getHttpClient
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.safeSpotifyApiCall
import java.net.InetSocketAddress
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SpotifyTokenManagerImpl(
    private val httpClient: HttpClient = getHttpClient(),
    private val tokenStorage: FileTokenStorageImpl,
    private val clientId: String = dotenv()["SPOTIFY_CLIENT_ID"] ?: "",
    private val clientSecret: String = dotenv()["SPOTIFY_CLIENT_SECRET"] ?: "",
) : SpotifyTokenManager {
    init {
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            throw IllegalArgumentException("Spotify Client ID and Secret must be provided.")
        }
    }
    override suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError> {
        return safeSpotifyApiCall<String, SpotifyAccountsError> {
            val currentToken = tokenStorage.getTokens()?.accessToken
                ?: authenticate().accessToken
                ?: error("❌ Not authenticated yet")

            try {
                val response = httpClient.get("https://api.spotify.com/v1/me") {
                    headers.append(HttpHeaders.Authorization, "Bearer $currentToken")
                }

                if (response.status.isSuccess()) {
                    currentToken
                } else {
                    refreshAccessToken()
                }
            } catch (e: ClientRequestException) {
                // Most likely a 401 Unauthorized — token is invalid or expired
                if (e.response.status.value == 401) {
                    refreshAccessToken()
                } else {
                    throw e // Let safeSpotifyApiCall catch other issues
                }
            }
        }
    }

    private suspend fun authenticate(): TokenData = suspendCoroutine { continuation ->
        startHttpServerForCode { code ->
            // Resume the coroutine once tokens are fetched
            runBlocking {
                try {
                    val tokenData = exchangeCodeForTokens(code)
                    tokenStorage.saveTokens(tokenData)
                    continuation.resume(tokenData)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }

        openInBrowser(url = generateSpotifyAuthUrl())
    }

    private suspend fun exchangeCodeForTokens(code: String): TokenData = httpClient.submitForm(
        url = "$SPOTIFY_ACCOUNTS_BASE_URL/api/token",
        formParameters = Parameters.build {
            append("grant_type", "authorization_code")
            append("code", code)
            append("redirect_uri", SPOTIFY_REDIRECT_URL)
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }
    ).body<TokenData>()


    private suspend fun refreshAccessToken(): String {
        val credentials = "$clientId:$clientSecret"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
        val currentRefreshToken = tokenStorage.getTokens()?.refreshToken ?: error("❌ No refresh token available")

        val tokenData = httpClient.post("$SPOTIFY_ACCOUNTS_BASE_URL/api/token") {
            header(HttpHeaders.Authorization, "Basic $encodedCredentials")
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", currentRefreshToken)
                }.formUrlEncode()
            )
        }.body<RefreshTokenResponse>()

        tokenStorage.saveTokens(
            TokenData(
                refreshToken = currentRefreshToken,
                accessToken = tokenData.accessToken,
                scope = tokenData.scope,
            )
        )
        return tokenData.accessToken ?: error("❌ No access token found in response")
    }

    private fun generateSpotifyAuthUrl(): String {
        val scopes = listOf(
            "user-modify-playback-state",
            "user-read-playback-state"
        ).joinToString(" ")

        return URLBuilder("$SPOTIFY_ACCOUNTS_BASE_URL/authorize").apply {
            parameters.append("client_id", clientId)
            parameters.append("response_type", "code")
            parameters.append("redirect_uri", SPOTIFY_REDIRECT_URL)
            parameters.append("scope", scopes)
        }.buildString()
    }

    @Throws(RuntimeException::class)
    private fun openInBrowser(url: String) {
        try {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("mac") -> arrayOf("open", url)
                os.contains("nix") || os.contains("nux") -> arrayOf("xdg-open", url)
                os.contains("win") -> arrayOf("rundll32", "url.dll,FileProtocolHandler", url)
                else -> throw UnsupportedOperationException("Unsupported OS: $os")
            }
            Runtime.getRuntime().exec(command)
        } catch (e: Exception) {
            throw RuntimeException("❌ Failed to open browser. Please visit the following URL manually: $url", e)


        }
    }

    private fun startHttpServerForCode(onCodeReceived: (String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress(8888), 0)

        server.createContext("/callback") { exchange ->
            val query = exchange.requestURI.query.orEmpty()
            val code = query.split("&")
                .mapNotNull { it.split("=").takeIf { parts -> parts.size == 2 } }
                .firstOrNull { it[0] == "code" }
                ?.get(1)

            val response = if (code != null) {
                onCodeReceived(code)
                response
            } else {
                "❌ Missing 'code' parameter in callback URL."
            }

            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }

        server.executor = null
        server.start()
        println("🌐 HTTP server started at $SPOTIFY_REDIRECT_URL")
    }

    companion object {
        const val SPOTIFY_ACCOUNTS_BASE_URL = "https://accounts.spotify.com"
        const val SPOTIFY_REDIRECT_URL = "http://127.0.0.1:8888/callback"
        val response = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Spotify Auth</title>
    <style>
        body {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
            background-color: #1DB954;
            color: white;
            font-family: Arial, sans-serif;
        }
        .gif-container {
            width: 400px;
            height: auto;
            overflow: hidden;
            margin-bottom: 20px;
        }
        .tenor-gif-embed {
            max-width: 100%;
        }
    </style>
</head>
<body>
    <div class="gif-container">
        <div class="tenor-gif-embed"
            data-postid="15846581740880760712"
            data-share-method="host"
            data-aspect-ratio="1.1267"
            data-width="100%">
            <a href="https://tenor.com/view/deadpool-let-this-man-cook-cook-deadpool-and-wolverine-cooking-gif-15846581740880760712">Deadpool Let This Man Cook GIF</a>
            from <a href="https://tenor.com/search/deadpool-gifs">Deadpool GIFs</a>
        </div>
    </div>
    <script type="text/javascript" async src="https://tenor.com/embed.js"></script>
    <h1>Success!</h1>
    <p>You may now close this tab.</p>
</body>
</html>
""".trimIndent()

    }
}


