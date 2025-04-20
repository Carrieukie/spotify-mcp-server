package mcpserver

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import mcpserver.spotifymcp.createServer

fun main() {
//     Run the MCP server
    runMcpServerUsingStdio()
//    runSseMcpServerUsingKtorPlugin(8080)
}

fun runMcpServerUsingStdio() = runBlocking {
    val server = createServer()
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )

    val shutdownSignal = CompletableDeferred<Unit>()
    server.onClose { shutdownSignal.complete(Unit) }

    server.connect(transport)
    shutdownSignal.await()
}

fun runSseMcpServerUsingKtorPlugin(port: Int): Unit = runBlocking {
    println("Starting sse server on port $port")
    println("Use inspector to connect to the http://localhost:$port/sse")

    embeddedServer(CIO, host = "0.0.0.0", port = port) {
        mcp {
            return@mcp createServer()
        }
    }.start(wait = true)
}
