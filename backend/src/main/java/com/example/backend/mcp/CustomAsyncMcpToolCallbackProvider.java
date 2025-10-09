package com.example.backend.mcp;


import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;


/**
 * Custom AsyncMcpToolCallbackProvider that uses CustomAsyncMcpToolCallback
 * to properly handle tool execution errors.
 * <p>
 * This is a workaround for Spring AI issue #2857:
 * https://github.com/spring-projects/spring-ai/issues/2857
 */
public class CustomAsyncMcpToolCallbackProvider extends AsyncMcpToolCallbackProvider
{

    private final List<McpAsyncClient> mcpClients;
    private final BiPredicate<McpAsyncClient, McpSchema.Tool> toolFilter;

    /**
     * Constructor for CustomAsyncMcpToolCallbackProvider with tool filter.
     *
     * @param toolFilter The filter to apply to tools.
     * @param mcpClients The list of MCP clients.
     */
    public CustomAsyncMcpToolCallbackProvider (BiPredicate<McpAsyncClient, McpSchema.Tool> toolFilter, List<McpAsyncClient> mcpClients)
    {
        super(toolFilter, mcpClients);
        Assert.notNull(mcpClients, "MCP clients must not be null");
        Assert.notNull(toolFilter, "Tool filter must not be null");
        this.mcpClients = mcpClients;
        this.toolFilter = toolFilter;
    }

    /**
     * Constructor for CustomAsyncMcpToolCallbackProvider without tool filter.
     *
     * @param mcpClients The list of MCP clients.
     */
    public CustomAsyncMcpToolCallbackProvider (List<McpAsyncClient> mcpClients)
    {
        this((mcpClient, tool) -> true, mcpClients);
    }

    /**
     * Get the tool callbacks using our custom callback implementation.
     *
     * @return An array of ToolCallback objects.
     */
    @Override
    public ToolCallback[] getToolCallbacks ()
    {
        var toolCallbacks = new ArrayList<ToolCallback>();

        this.mcpClients.forEach(mcpClient ->
        {
            McpSchema.ListToolsResult toolsResult = mcpClient.listTools().block();
            if (toolsResult != null && toolsResult.tools() != null)
            {
                toolCallbacks.addAll(toolsResult.tools()
                        .stream()
                        .filter(tool -> toolFilter.test(mcpClient, tool))
                        .map(tool -> new CustomAsyncMcpToolCallback(mcpClient, tool))
                        .toList());
            }
        });

        var array = toolCallbacks.toArray(new ToolCallback[0]);
        validateToolCallbacks(array);
        return array;
    }

    /**
     * Validate the tool callbacks to ensure there are no duplicate tool names.
     *
     * @param toolCallbacks An array of ToolCallback objects.
     * @throws IllegalStateException if there are duplicate tool names.
     */
    private void validateToolCallbacks (ToolCallback[] toolCallbacks)
    {
        // Check for duplicate tool names
        Set<String> toolNames = new HashSet<>();
        List<String> duplicateToolNames = new ArrayList<>();

        for (ToolCallback callback : toolCallbacks)
        {
            String toolName = callback.getToolDefinition().name();
            if (!toolNames.add(toolName))
            {
                duplicateToolNames.add(toolName);
            }
        }

        if (!duplicateToolNames.isEmpty())
        {
            throw new IllegalStateException(
                    "Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
        }
    }
}
