package mcpserver.spotify.utils.networkutils

sealed class SpotifyResult<out T, out E : Exception> {
    data class Success<out T : Any>(
        val data: T
    ) : SpotifyResult<T, Nothing>()

    data class Failure<out E : Exception>(
        val exception: E
    ) : SpotifyResult<Nothing, E>()
}

