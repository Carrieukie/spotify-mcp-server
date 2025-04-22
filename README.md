# Kotlin MCP Server for Spotify

A Kotlin implementation of a Model Context Protocol (MCP) server that integrates with the Spotify Web API. This server
provides tools for controlling Spotify playback, managing playlists, and retrieving user information through a
standardized interface.

## Features

- **Spotify Authentication**: Manages Spotify API tokens and authentication
- **Playback Control**: Play, pause, skip tracks, seek to position, set volume, and control repeat mode
- **Playlist Management**: Create playlists, add/remove tracks, and retrieve playlist information
- **User Profile**: Retrieve user profile information
- **MCP Integration**: Implements the Model Context Protocol for standardized tool interactions

## Prerequisites

- JDK 20 or higher
- Kotlin 2.1.10
- Gradle (wrapper included)
- Spotify Developer Account (for API access)

## Installation and Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/kotlin-mcp-server.git
   cd kotlin-mcp-server
   ```

2. Set up Spotify Developer credentials:
   - Go to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/)
   - Log in with your Spotify account or create a new one
   - Click "Create an App"
   - Fill in the app name and description, then click "Create"
   - From your app's dashboard, note your Client ID and Client Secret
   - Create a `.env` file in the project root with the following content:
     ```
     SPOTIFY_CLIENT_ID = "your-client-id"
     SPOTIFY_CLIENT_SECRET = "your-client-secret"
     ```
     For example:
     ```
     SPOTIFY_CLIENT_ID = "d4k32j4kl32j4k23j4k23j4k32if"
     SPOTIFY_CLIENT_SECRET = "5gd6f56fdsd6g5a6d7sd5656cvbx"
     ```

## Usage

The server can be run in two modes:

1. **Standard I/O Mode** (default):
   Uncomment the appropriate line in `Main.kt` to run the server using Ktor with stdio:
   ```kotlin
   // In Main.kt
   fun main() {
       runMcpServerUsingStdio()
       // runSseMcpServerUsingKtorPlugin(8080)
   }
   ```

   1. Run the following command to generate the executable:
       ```bash
       ./gradlew installDist
       ```
       This will generate an executable at: `build/install/kotlin-mcp-server/bin/kotlin-mcp-server`

   **Connecting with VS Code's Copilot**:
   VS Code's Copilot has a built-in MCP client that can connect to the server when running in Standard I/O mode. You can
   use other mcp clients like claude desktop but Code's Copilot is the easiest that I found to use. To use it:


2. Configure VS Code to connect to the MCP server:
    - Open VS Code Settings (Settings > Settings again )
    - Search for "mcp" in the settings search bar
    - Click on "Edit in settings.json"
    - Add the following configuration (adjust the path to match your installation):

   ```json
   {
       "security.workspace.trust.untrustedFiles": "open",
       "terminal.integrated.fontFamily": "MesloLGS Nerd Font",
       "mcp": {
           "inputs": [],
           "servers": {
               "spotify-mcp-server": {
                   "command": "/Users/karis/IdeaProjects/kotlin-mcp-server/build/install/kotlin-mcp-server/bin/kotlin-mcp-server",
                   "args": [],
                   "env": {}
               }
           }
       },
       "git.autofetch": true
   }
   ```

3. In VS Code, open the Copilot chat panel and pick Agent Mode.
4. Copilot will automatically detect and connect to the MCP server
5. You can now interact with the Spotify tools through the Copilot interface

2. **Server-Sent Events (SSE) Mode**:
   Uncomment the appropriate line in `Main.kt` to run the server using Ktor with SSE:
   ```kotlin
   // In Main.kt
   fun main() {
       // runMcpServerUsingStdio()
       runSseMcpServerUsingKtorPlugin(8080)
   }
   ```
   Then run the application and connect to `http://localhost:8080/sse` using an MCP inspector.

## Project Structure

- `src/main/kotlin/mcpserver/spotify/`: Core Spotify API integration
    - `auth/`: Authentication and token management
    - `services/`: Service implementations for Spotify API endpoints
    - `utils/`: Utility functions and error handling
- `src/main/kotlin/mcpserver/spotifymcp/`: MCP server implementation
    - `tools/`: Tool implementations for the MCP server

## Available Tools

The server provides the following tools for interacting with Spotify:

### Playback Control

- Pause playback
- Resume playback
- Skip to next track
- Skip to previous track
- Seek to position
- Set volume
- Set repeat mode
- Get current queue

### Playlist Management

- Get user playlists
- Get playlist items
- Create playlist
- Add tracks to playlist
- Remove tracks from playlist

### User Information

- Get user profile

## Development Guidelines

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

## Testing

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

The project uses JUnit 5 for testing. Tests should be placed in the `src/test/kotlin` directory, mirroring the structure
of the main source code.

For HTTP client mocking, use Ktor's `MockEngine` to simulate HTTP responses. When testing services that depend on
external APIs, create mock implementations of dependencies and test both success and failure scenarios.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
