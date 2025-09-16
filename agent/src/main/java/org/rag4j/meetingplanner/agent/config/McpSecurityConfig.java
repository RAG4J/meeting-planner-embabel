package org.rag4j.meetingplanner.agent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for MCP SSE client with OAuth2 security.
 *
 *
 *
 * https://www.youtube.com/watch?v=nUvJjMjEDyE
 * @author Alexey Lavrenchenko
 */
@Configuration
@EnableConfigurationProperties(McpSseClientProperties.class)
public class McpSecurityConfig {

    @Bean
    public List<NamedClientMcpTransport> webFluxClientTransports(McpSseClientProperties sseProperties,
                                                                 ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                                                                 ObjectProvider<ObjectMapper> objectMapperProvider,
                                                                 OAuth2AuthorizedClientManager authorizedClientManager) {
        List<NamedClientMcpTransport> sseTransports = new ArrayList();
        WebClient.Builder webClientBuilderTemplate =
                (WebClient.Builder) webClientBuilderProvider.getIfAvailable(WebClient::builder);
        ObjectMapper objectMapper = (ObjectMapper) objectMapperProvider.getIfAvailable(ObjectMapper::new);

        for (Map.Entry<String, McpSseClientProperties.SseParameters> serverParameters :
                sseProperties.getConnections().entrySet()) {
            ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                    new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
            oauth2Client.setDefaultClientRegistrationId("location-mcp");

            WebClient.Builder webClientBuilder =
                    webClientBuilderTemplate.clone().baseUrl(((McpSseClientProperties.SseParameters) serverParameters.getValue()).url());
            String sseEndpoint =
                    ((McpSseClientProperties.SseParameters) serverParameters.getValue()).sseEndpoint() != null ?
                            ((McpSseClientProperties.SseParameters) serverParameters.getValue()).sseEndpoint() : "/sse";
            WebFluxSseClientTransport transport =
                    WebFluxSseClientTransport.builder(webClientBuilder.apply(oauth2Client.oauth2Configuration())).sseEndpoint(sseEndpoint).objectMapper(objectMapper).build();
            sseTransports.add(new NamedClientMcpTransport((String) serverParameters.getKey(), transport));
        }

        return sseTransports;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

}
