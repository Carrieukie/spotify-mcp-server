package mcpserver.spotify.auth.tokenstorage

import kotlinx.serialization.json.Json
import mcpserver.spotify.auth.tokenstorage.model.TokenData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FileTokenStorageImplTest {

    private lateinit var tempFile: File
    private lateinit var tokenStorage: FileTokenStorageImpl

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        tempFile = File(tempDir.toFile(), "test-tokens.json")
        tokenStorage = FileTokenStorageImpl(tempFile)
    }

    @AfterEach
    fun tearDown() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    @Test
    fun `saveTokens should write token data to file`() {
        // Arrange
        val tokenData = TokenData(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            scope = "user-read-playback-state user-modify-playback-state"
        )

        // Act
        tokenStorage.saveTokens(tokenData)

        // Assert
        val fileContent = tempFile.readText()
        val savedTokenData = Json.decodeFromString<TokenData>(fileContent)
        
        assertEquals(tokenData.accessToken, savedTokenData.accessToken)
        assertEquals(tokenData.refreshToken, savedTokenData.refreshToken)
        assertEquals(tokenData.scope, savedTokenData.scope)
    }

    @Test
    fun `getTokens should return token data when file exists and contains valid JSON`() {
        // Arrange
        val tokenData = TokenData(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            scope = "user-read-playback-state user-modify-playback-state"
        )
        val json = Json.encodeToString(TokenData.serializer(), tokenData)
        tempFile.writeText(json)

        // Act
        val result = tokenStorage.getTokens()

        // Assert
        assertEquals(tokenData.accessToken, result?.accessToken)
        assertEquals(tokenData.refreshToken, result?.refreshToken)
        assertEquals(tokenData.scope, result?.scope)
    }

    @Test
    fun `getTokens should return null when file does not exist`() {
        // Arrange
        if (tempFile.exists()) {
            tempFile.delete()
        }

        // Act
        val result = tokenStorage.getTokens()

        // Assert
        assertNull(result)
    }

    @Test
    fun `getTokens should return null when file contains invalid JSON`() {
        // Arrange
        tempFile.writeText("invalid json content")

        // Act
        val result = tokenStorage.getTokens()

        // Assert
        assertNull(result)
    }

    @Test
    fun `saveTokens should overwrite existing file`() {
        // Arrange
        val initialTokenData = TokenData(
            accessToken = "initial-access-token",
            refreshToken = "initial-refresh-token",
            scope = "initial-scope"
        )
        val updatedTokenData = TokenData(
            accessToken = "updated-access-token",
            refreshToken = "updated-refresh-token",
            scope = "updated-scope"
        )
        
        // Act
        tokenStorage.saveTokens(initialTokenData)
        tokenStorage.saveTokens(updatedTokenData)
        
        // Assert
        val result = tokenStorage.getTokens()
        assertEquals(updatedTokenData.accessToken, result?.accessToken)
        assertEquals(updatedTokenData.refreshToken, result?.refreshToken)
        assertEquals(updatedTokenData.scope, result?.scope)
    }
}