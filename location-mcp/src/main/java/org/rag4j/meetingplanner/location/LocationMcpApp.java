package org.rag4j.meetingplanner.location;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LocationMcpApp {
    public static void main(String[] args) {
        SpringApplication.run(LocationMcpApp.class, args);
    }

    @Bean
    public ToolCallbackProvider locationTools(LocationService locationService) {
        return MethodToolCallbackProvider.builder().toolObjects(locationService).build();
    }
}
