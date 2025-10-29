package org.rag4j.meetingplanner.location;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * Only used by the test script to verify that MCP authentication works.
 */
@RestController
public class McpTestController {
    
    @GetMapping("/mcp/test")
    public Map<String, Object> mcpTest(Authentication authentication) {
        return Map.of(
            "status", "authenticated",
            "endpoint", "/mcp/test", 
            "user", authentication != null ? authentication.getName() : "anonymous",
            "message", "MCP authentication test successful"
        );
    }
}