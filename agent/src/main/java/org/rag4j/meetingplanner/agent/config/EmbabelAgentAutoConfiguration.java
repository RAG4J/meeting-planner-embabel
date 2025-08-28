package org.rag4j.meetingplanner.agent.config;

import org.rag4j.meetingplanner.agent.service.EmbabelAgentService;
import org.rag4j.meetingplanner.agent.service.impl.DefaultEmbabelAgentService;
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

    @Bean
    @ConditionalOnMissingBean(EmbabelAgentService.class)
    public EmbabelAgentService embabelAgentService() {
        return new DefaultEmbabelAgentService();
    }
}
