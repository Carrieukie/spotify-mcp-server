package mcpserver.modelcontextprotocol.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun addAdditionTool(server: Server) {
    server.addTool(
        name = "Add",
        description = "Add two numbers",
        inputSchema = Tool.Input(
            buildJsonObject {
                put("a", buildJsonObject { put("type", "number") })
                put("b", buildJsonObject { put("type", "number") })
            }
        )
    ) { input ->
        val a = input.arguments["a"]?.jsonPrimitive?.int
            ?: error("Missing or invalid argument: a")
        val b = input.arguments["b"]?.jsonPrimitive?.int
            ?: error("Missing or invalid argument: b")

        CallToolResult(listOf(TextContent((a + b).toString())))
    }
}