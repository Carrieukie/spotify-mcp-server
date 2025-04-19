package mcpserver.spotifymcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class McpServerTest {

    @Test
    fun `createServer should return a valid Server instance`() {
        // Act
        val server = createServer()

        // Assert
        assertNotNull(server)

        // Note: We can't directly test internal properties of the Server class,
        // but we can verify that the server is created without errors.
        // In a real-world scenario, we would test the server's behavior by
        // making requests to it and verifying the responses.
    }
}
