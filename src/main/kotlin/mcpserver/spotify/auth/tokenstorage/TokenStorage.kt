package mcpserver.spotify.auth.tokenstorage

import mcpserver.spotify.auth.tokenstorage.model.TokenData

interface TokenStorage {
    fun saveTokens(tokenData: TokenData)
    fun getTokens(): TokenData?
}
