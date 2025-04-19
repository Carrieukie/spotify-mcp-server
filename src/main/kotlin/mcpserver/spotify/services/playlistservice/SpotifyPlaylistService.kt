package mcpserver.spotify.services.playlistservice

import mcpserver.spotify.services.playlistservice.model.PlaylistItem
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyAddTracksResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyCreatePlaylistRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistItemsResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyPlaylistResponse
import mcpserver.spotify.services.playlistservice.model.SpotifyRemoveTracksRequest
import mcpserver.spotify.services.playlistservice.model.SpotifyRemoveTracksResponse
import mcpserver.spotify.utils.networkutils.SpotifyResult
import mcpserver.spotify.utils.networkutils.model.SpotifyApiError

interface SpotifyPlaylistService {
    /**
     * Get a list of the playlists owned or followed by the current Spotify user.
     *
     * @param limit The maximum number of playlists to return. Default: 20. Minimum: 1. Maximum: 50.
     * @param offset The index of the first playlist to return. Default: 0 (the first object).
     * @return A SpotifyResult containing either the playlist response or an error.
     */
    suspend fun getCurrentUserPlaylists(
        limit: Int = 20,
        offset: Int = 0
    ): SpotifyResult<SpotifyPlaylistResponse, SpotifyApiError>

    /**
     * Get full details of the items of a playlist owned by a Spotify user.
     *
     * @param playlistId The Spotify ID of the playlist.
     * @param limit The maximum number of items to return. Default: 20. Minimum: 1. Maximum: 50.
     * @param offset The index of the first item to return. Default: 0 (the first object).
     * @return A SpotifyResult containing either the playlist items response or an error.
     */
    suspend fun getPlaylistItems(
        playlistId: String,
        limit: Int = 20,
        offset: Int = 0
    ): SpotifyResult<SpotifyPlaylistItemsResponse, SpotifyApiError>

    /**
     * Remove one or more tracks from a user's playlist.
     *
     * @param playlistId The Spotify ID of the playlist.
     * @param request The request containing the tracks to remove and optionally a snapshot ID.
     * @return A SpotifyResult containing either the response with the new snapshot ID or an error.
     */
    suspend fun removePlaylistTracks(
        playlistId: String,
        request: SpotifyRemoveTracksRequest
    ): SpotifyResult<SpotifyRemoveTracksResponse, SpotifyApiError>

    /**
     * Add one or more tracks to a user's playlist.
     *
     * @param playlistId The Spotify ID of the playlist.
     * @param request The request containing the tracks to add and optionally a position.
     * @return A SpotifyResult containing either the response with the new snapshot ID or an error.
     */
    suspend fun addPlaylistTracks(
        playlistId: String,
        request: SpotifyAddTracksRequest
    ): SpotifyResult<SpotifyAddTracksResponse, SpotifyApiError>

    /**
     * Create a playlist for a Spotify user.
     *
     * @param userId The user's Spotify user ID.
     * @param request The request containing the playlist details.
     * @return A SpotifyResult containing either the created playlist or an error.
     */
    suspend fun createPlaylist(
        userId: String,
        request: SpotifyCreatePlaylistRequest
    ): SpotifyResult<PlaylistItem, SpotifyApiError>
}
