package mcpserver.spotify.authstuff.tokenstorage

import mcpserver.spotify.authstuff.tokenstorage.model.TokenData

interface TokenStorage {
    fun saveTokens(tokenData: TokenData)
    fun getTokens(): TokenData?
}
