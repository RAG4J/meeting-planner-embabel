package org.rag4j.meetingplanner.location.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);   // include ?param=value
        filter.setIncludePayload(false);      // disable request body
        filter.setIncludeHeaders(false);      // no headers
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}
