package org.rag4j.meetingplanner.agent.config;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Configuration
public class McpToolsConfig {

    private final List<McpSyncClient> mcpSyncClients;

    @Autowired
    public McpToolsConfig(@Lazy List<McpSyncClient> mcpSyncClients) {
        Assert.notNull(mcpSyncClients, "McpSyncClients must not be null");
        this.mcpSyncClients = mcpSyncClients;
    }

    @Bean(name = "mcpLocationsToolsGroup")
    public ToolGroup mcpLocationsToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.Companion.invoke(
                        "A collection of tools to interact with the MCP location service",
                        "location"
                ),
                "Spring",
                "location",
                Set.of(ToolGroupPermission.HOST_ACCESS),
                mcpSyncClients,
                callback -> callback.getToolDefinition().name().contains("location") || callback.getToolDefinition().name().contains("room")
        );
    }

    @Bean(name = "mcpTimeToolsGroup")
    public ToolGroup mcpTimeToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.Companion.invoke(
                        "A collection of tools to interact with the MCP time service",
                        "time"
                ),
                "Docker",
                "time",
                Set.of(ToolGroupPermission.HOST_ACCESS),
                mcpSyncClients,
                callback -> callback.getToolDefinition().name().contains("time")
        );
    }

    @Bean(name = "mcpFoodAndDrinksToolsGroup")
    public ToolGroup mcpFoodAndDrinksToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.Companion.invoke(
                        "A collection of tools to interact with the MCP food and drinks service",
                        "food_and_drinks"
                ),
                "Spring",
                "food-drinks-mcp",
                Set.of(ToolGroupPermission.HOST_ACCESS),
                mcpSyncClients,
                callback -> callback.getToolDefinition().name().contains("food")
        );
    }
}