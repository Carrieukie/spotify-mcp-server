package mcpserver.spotify.authstuff.filetokenstorage

import mcpserver.spotify.authstuff.filetokenstorage.model.TokenData

interface TokenStorage {
    fun saveTokens(tokenData: TokenData)
    fun getTokens(): TokenData?
}
