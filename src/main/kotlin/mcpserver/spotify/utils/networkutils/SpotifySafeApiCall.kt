package mcpserver.spotify.utils.networkutils

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import mcpserver.spotify.utils.networkutils.model.Error
import mcpserver.spotify.utils.networkutils.model.SpotifyAccountsError
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError


suspend inline fun <reified T : Any, reified E : Exception> safeSpotifyApiCall(
    crossinline apiCall: suspend () -> T
): SpotifyResult<T, E> {
    return try {
        val result = apiCall()
        SpotifyResult.Success(result)
    } catch (e: Exception) {
        val error = when (e) {
            is ClientRequestException, is ServerResponseException -> handleHttpException<E>(e)
            else -> createDefaultError<E>(e)
        }
        SpotifyResult.Failure(error)
    }
}


suspend inline fun <reified E : Exception> handleHttpException(e: Exception): E {
    val response = (e as? ResponseException)?.response
    val errorBody = response?.bodyAsText().orEmpty()

    return try {
        Json.decodeFromString(errorBody)
    } catch (_: Exception) {
        createDefaultError<E>(e, response)
    }
}

inline fun <reified E : Exception> createDefaultError(
    e: Exception,
    response: HttpResponse? = null
): E {
    return when (E::class) {
        SpotifyApiError::class -> SpotifyApiError(
            error = Error(
                message = e.message ?: "SpotifyApiError Unexpected error occurred",
                status = response?.status?.value ?: 0
            )
        ) as E

        SpotifyAccountsError::class -> SpotifyAccountsError(
            errorDescription = e.message ?: "SpotifyAccountsError Unexpected error occurred",
            error = null,
            status = response?.status?.value ?: 0
        ) as E

        else -> throw IllegalStateException("Unsupported error type: ${E::class}")
    }
}



