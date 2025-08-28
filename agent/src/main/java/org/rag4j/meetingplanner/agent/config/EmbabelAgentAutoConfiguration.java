package org.rag4j.meetingplanner.agent.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Embabel Agent
 */
@AutoConfiguration
@ComponentScan(basePackages = "org.rag4j.meetingplanner.agent")
public class EmbabelAgentAutoConfiguration {

}
