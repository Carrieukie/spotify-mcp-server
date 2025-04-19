# Kotlin MCP Server Development Guidelines

This document provides essential information for developing and maintaining the Kotlin MCP Server project.

## Build/Configuration Instructions

### Prerequisites
- JDK 20 or higher
- Kotlin 2.1.10
- Gradle (wrapper included)

### Building the Project
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run
```

### Configuration
The application uses a `tokens.json` file in the project root to store Spotify authentication tokens. This file is created automatically when the application runs for the first time.

#### Environment Variables
While not currently implemented, the project is set up to use environment variables through the dotenv-kotlin library. Consider using a `.env` file for sensitive configuration in the future.

## Testing Information

### Running Tests
```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "mcpserver.spotify.services.playerservice.SpotifyPlayerServiceImplTest"

# Run a specific test method
./gradlew test --tests "mcpserver.spotify.services.playerservice.SpotifyPlayerServiceImplTest.playTrack should return success when API call succeeds"
```

### Writing Tests
The project uses JUnit 5 for testing. Tests should be placed in the `src/test/kotlin` directory, mirroring the structure of the main source code.

#### Mocking
For HTTP client mocking, use Ktor's `MockEngine`:

```kotlin
val mockEngine = MockEngine { request: HttpRequestData ->
    // Verify request properties
    assertEquals("Bearer mock-token", request.headers["Authorization"])

    // Return a mock response
    respond(
        content = "",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}

val mockClient = HttpClient(mockEngine) {
    install(ContentNegotiation) {
        json()
    }
}
```

#### Testing Services
When testing services that depend on external APIs:
1. Create mock implementations of dependencies (e.g., `SpotifyTokenManager`)
2. Use Ktor's `MockEngine` to simulate HTTP responses
3. Test both success and failure scenarios

Example:

```kotlin
@Test
fun `playTrack should return success when API call succeeds`() = runBlocking {
    // Arrange
    val mockTokenManager = object : SpotifyTokenManager {
        override suspend fun getValidAccessToken(): SpotifyResult<String, SpotifyAccountsError> {
            return SpotifyResult.Success("mock-token")
        }
    }

    val mockEngine = MockEngine { request: HttpRequestData ->
        // Return success response
        respond(
            content = "",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    val mockClient = HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json()
        }
    }

    val service = SpotifyPlayerServiceImpl(
        tokenManager = mockTokenManager,
        client = mockClient
    )

    // Act
    val result = service.playTrack(listOf("spotify:track:123456"))

    // Assert
    assertIs<SpotifyResult.Success<String>>(result)
}
```

## Additional Development Information

### Project Structure
- `src/main/kotlin/mcpserver/spotify/`: Core Spotify API integration
  - `auth/`: Authentication and token management
  - `services/`: Service implementations for Spotify API endpoints
  - `utils/`: Utility functions and error handling
- `src/main/kotlin/mcpserver/spotifymcp/`: MCP server implementation
  - `tools/`: Tool implementations for the MCP server

### Error Handling
The project uses a custom `SpotifyResult<T, E>` sealed class for error handling:
- `SpotifyResult.Success<T>`: Contains successful response data
- `SpotifyResult.Failure<E>`: Contains an exception with error details

Use the `safeSpotifyApiCall` function to make API calls that return `SpotifyResult` objects:

```kotlin
val result = safeSpotifyApiCall<ResponseType, ErrorType> {
    // API call code here
}
```

### Adding New Tools
To add a new tool to the MCP server:
1. Create a new Kotlin file in the `mcpserver/spotifymcp/tools` package
2. Define a function that takes a `Server` instance and any required services
3. Use `server.addTool()` to register the tool with a name, description, and input schema
4. Implement the tool's functionality in the callback

Example:
```kotlin
fun addNewSpotifyTool(server: Server, spotifyService: SpotifyService) {
    val toolDescription = """
        Tool description here.
    """.trimIndent()

    val inputSchema = Tool.Input(
        properties = buildJsonObject {
            // Define input properties
        }
    )

    server.addTool(
        name = "tool-name",
        description = toolDescription,
        inputSchema = inputSchema
    ) { input: Tool.CallInput ->
        // Tool implementation
        CallToolResult(listOf(TextContent("Result")))
    }
}
```

### Code Style
- Use Kotlin idioms and language features
- Prefer coroutines for asynchronous operations
- Use sealed classes for representing states with multiple possible values
- Follow the existing error handling pattern with `SpotifyResult`
