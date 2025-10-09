package com.example.backend.mcp;


import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.execution.ToolExecutionException;

import java.util.Map;


/**
 * Custom AsyncMcpToolCallback that properly handles tool execution errors
 * by throwing ToolExecutionException instead of IllegalStateException.
 * <p>
 * This is a workaround for Spring AI issue #2857:
 * https://github.com/spring-projects/spring-ai/issues/2857
 */
public class CustomAsyncMcpToolCallback extends AsyncMcpToolCallback
{

    private static final Logger log = LoggerFactory.getLogger(CustomAsyncMcpToolCallback.class);

    private final McpAsyncClient mcpClient;
    private final McpSchema.Tool tool;

    /**
     * Creates a new {@code CustomAsyncMcpToolCallback} instance.
     *
     * @param mcpClient the MCP client to use for tool execution
     * @param tool      the MCP tool definition to adapt
     */
    public CustomAsyncMcpToolCallback (McpAsyncClient mcpClient, McpSchema.Tool tool)
    {
        super(mcpClient, tool);
        this.mcpClient = mcpClient;
        this.tool = tool;
    }

    @Override
    public String call (String functionInput)
    {
        try
        {
            Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);

            // Note that we use the original tool name here, not the adapted one from getToolDefinition
            McpSchema.CallToolResult response = this.mcpClient.callTool(
                    new McpSchema.CallToolRequest(this.tool.name(), arguments)
            ).block();

            if (response != null && response.isError() != null && response.isError())
            {
                log.warn("MCP tool '{}' execution failed: {}", this.tool.name(), response.content());

                // Create a proper ToolDefinition for the exception
                DefaultToolDefinition toolDefinition = new DefaultToolDefinition(
                        tool.name(),
                        tool.description(),
                        ModelOptionsUtils.toJsonString(tool.inputSchema())
                );

                // Throw ToolExecutionException instead of IllegalStateException
                throw new ToolExecutionException(
                        toolDefinition,
                        new RuntimeException("Error calling tool: " + response.content())
                );
            }

            return ModelOptionsUtils.toJsonString(response != null ? response.content() : null);

        } catch (ToolExecutionException e)
        {
            // Re-throw ToolExecutionException as-is
            throw e;
        } catch (Exception e)
        {
            log.error("Unexpected error executing MCP tool '{}'", this.tool.name(), e);

            // Wrap any other exception in ToolExecutionException
            DefaultToolDefinition toolDefinition = new DefaultToolDefinition(
                    tool.name(),
                    tool.description(),
                    ModelOptionsUtils.toJsonString(tool.inputSchema())
            );

            throw new ToolExecutionException(toolDefinition, e);
        }
    }
}
