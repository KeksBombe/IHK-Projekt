package com.example.backend.config;


import com.example.backend.mcp.CustomAsyncMcpToolCallbackProvider;
import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;


/**
 * Configuration for custom MCP tool callback provider that properly handles
 * tool execution errors.
 * <p>
 * This is a workaround for Spring AI issue #2857:
 * https://github.com/spring-projects/spring-ai/issues/2857
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = "enabled", havingValue = "true")
public class McpToolConfig
{

    /**
     * Creates a custom MCP tool callback provider that uses our enhanced
     * error handling implementation.
     *
     * @param mcpClients The list of MCP async clients (auto-configured by Spring AI)
     * @return CustomAsyncMcpToolCallbackProvider instance
     */
    @Bean
    @Primary
    public ToolCallbackProvider customMcpToolCallbackProvider (List<McpAsyncClient> mcpClients)
    {
        return new CustomAsyncMcpToolCallbackProvider(mcpClients);
    }
}
