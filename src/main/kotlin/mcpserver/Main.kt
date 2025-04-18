package mcpserver

import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import mcpserver.modelcontextprotocol.createServer

fun main() = runBlocking {
    val server = createServer()
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )

    val shutdownSignal = CompletableDeferred<Unit>()
    server.onCloseCallback = { shutdownSignal.complete(Unit) }

    server.connect(transport)
    shutdownSignal.await()
}



