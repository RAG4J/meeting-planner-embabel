package com.meetingplanner.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing OAuth2 registered clients.
 * 
 * This service provides functionality to view and manage OAuth2 client
 * applications registered with the authorization server.
 */
@Service
public class ClientManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ClientManagementService.class);
    private final RegisteredClientRepository registeredClientRepository;

    public ClientManagementService(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    /**
     * Test method to verify registered clients at startup.
     */
    @PostConstruct
    public void logRegisteredClients() {
        logger.info("ClientManagementService initialized");
        logger.info("RegisteredClientRepository type: {}", registeredClientRepository.getClass().getName());
        
        // Test direct access to known clients
        RegisteredClient webClient = registeredClientRepository.findByClientId("meeting-planner-web");
        RegisteredClient mcpClient = registeredClientRepository.findByClientId("location-mcp");
        
        logger.info("Web client found: {}", webClient != null ? webClient.getClientId() : "NOT FOUND");
        logger.info("MCP client found: {}", mcpClient != null ? mcpClient.getClientId() : "NOT FOUND");
        
        // Test reflection access
        List<ClientInfo> clients = getAllClients();
        logger.info("Total clients retrieved via getAllClients(): {}", clients.size());
        for (ClientInfo client : clients) {
            logger.info("Client: {} (ID: {})", client.getClientId(), client.getId());
        }
    }

    /**
     * Get all registered clients.
     */
    public List<ClientInfo> getAllClients() {
        List<ClientInfo> clients = new ArrayList<>();
        logger.debug("Retrieving all registered clients");
        
        if (registeredClientRepository instanceof InMemoryRegisteredClientRepository) {
            try {
                logger.debug("Using reflection to access InMemoryRegisteredClientRepository");
                
                // Try multiple possible field names (based on Spring Security 1.5.2)
                String[] possibleFieldNames = {"idRegistrationMap", "clientIdRegistrationMap", "registrations", "clients", "registeredClients"};
                Map<String, RegisteredClient> clientMap = null;
                
                for (String fieldName : possibleFieldNames) {
                    try {
                        var field = InMemoryRegisteredClientRepository.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        Map<String, RegisteredClient> map = 
                            (Map<String, RegisteredClient>) field.get(registeredClientRepository);
                        clientMap = map;
                        logger.debug("Successfully accessed field '{}' with {} clients", fieldName, map.size());
                        break;
                    } catch (NoSuchFieldException e) {
                        logger.debug("Field '{}' not found, trying next", fieldName);
                    }
                }
                
                if (clientMap != null) {
                    for (RegisteredClient client : clientMap.values()) {
                        clients.add(toClientInfo(client));
                        logger.debug("Added client: {} ({})", client.getClientId(), client.getId());
                    }
                } else {
                    logger.error("Could not find any suitable field in InMemoryRegisteredClientRepository");
                }
            } catch (Exception e) {
                logger.error("Failed to access registered clients via reflection", e);
            }
        } else {
            logger.warn("RegisteredClientRepository is not InMemoryRegisteredClientRepository: {}", 
                       registeredClientRepository.getClass().getName());
        }
        
        logger.info("Retrieved {} registered clients", clients.size());
        return clients.stream()
                .sorted(Comparator.comparing(ClientInfo::getClientId))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific client by client ID.
     */
    public Optional<ClientInfo> getClient(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        return client != null ? Optional.of(toClientInfo(client)) : Optional.empty();
    }

    /**
     * Get client statistics.
     */
    public ClientStats getClientStats() {
        List<ClientInfo> clients = getAllClients();
        
        int webClients = (int) clients.stream()
                .filter(client -> client.getGrantTypes().contains("authorization_code"))
                .count();
        
        int apiClients = (int) clients.stream()
                .filter(client -> client.getGrantTypes().contains("client_credentials"))
                .count();
        
        Set<String> allScopes = clients.stream()
                .flatMap(client -> client.getScopes().stream())
                .collect(Collectors.toSet());
        
        return new ClientStats(
                clients.size(),
                webClients,
                apiClients,
                allScopes.size()
        );
    }

    /**
     * Get client display name.
     */
    public String getClientDisplayName(RegisteredClient client) {
        String clientId = client.getClientId();
        return switch (clientId) {
            case "meeting-planner-web" -> "Meeting Planner Web Application";
            case "location-mcp" -> "Location MCP Server";
            default -> clientId.replace("-", " ").toUpperCase();
        };
    }

    /**
     * Convert RegisteredClient to ClientInfo DTO.
     */
    private ClientInfo toClientInfo(RegisteredClient client) {
        return new ClientInfo(
                client.getId(),
                client.getClientId(),
                getClientDisplayName(client),
                client.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .collect(Collectors.toSet()),
                client.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.toSet()),
                client.getRedirectUris(),
                client.getPostLogoutRedirectUris(),
                client.getScopes(),
                formatDuration(client.getTokenSettings().getAccessTokenTimeToLive()),
                formatDuration(client.getTokenSettings().getRefreshTokenTimeToLive()),
                client.getClientSettings().isRequireAuthorizationConsent(),
                client.getTokenSettings().isReuseRefreshTokens(),
                LocalDateTime.now() // Placeholder for registration time
        );
    }

    /**
     * Format duration for display.
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "N/A";
        }
        
        long hours = duration.toHours();
        long days = duration.toDays();
        
        if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else {
            return duration.toMinutes() + " minute" + (duration.toMinutes() != 1 ? "s" : "");
        }
    }

    /**
     * DTO classes for client information
     */
    public static class ClientInfo {
        private final String id;
        private final String clientId;
        private final String displayName;
        private final Set<String> authenticationMethods;
        private final Set<String> grantTypes;
        private final Set<String> redirectUris;
        private final Set<String> postLogoutRedirectUris;
        private final Set<String> scopes;
        private final String accessTokenTtl;
        private final String refreshTokenTtl;
        private final boolean requireConsent;
        private final boolean reuseRefreshTokens;
        private final LocalDateTime registeredTime;

        public ClientInfo(String id, String clientId, String displayName,
                         Set<String> authenticationMethods, Set<String> grantTypes,
                         Set<String> redirectUris, Set<String> postLogoutRedirectUris,
                         Set<String> scopes, String accessTokenTtl, String refreshTokenTtl,
                         boolean requireConsent, boolean reuseRefreshTokens,
                         LocalDateTime registeredTime) {
            this.id = id;
            this.clientId = clientId;
            this.displayName = displayName;
            this.authenticationMethods = authenticationMethods;
            this.grantTypes = grantTypes;
            this.redirectUris = redirectUris;
            this.postLogoutRedirectUris = postLogoutRedirectUris;
            this.scopes = scopes;
            this.accessTokenTtl = accessTokenTtl;
            this.refreshTokenTtl = refreshTokenTtl;
            this.requireConsent = requireConsent;
            this.reuseRefreshTokens = reuseRefreshTokens;
            this.registeredTime = registeredTime;
        }

        public String getId() { return id; }
        public String getClientId() { return clientId; }
        public String getDisplayName() { return displayName; }
        public Set<String> getAuthenticationMethods() { return authenticationMethods; }
        public Set<String> getGrantTypes() { return grantTypes; }
        public Set<String> getRedirectUris() { return redirectUris; }
        public Set<String> getPostLogoutRedirectUris() { return postLogoutRedirectUris; }
        public Set<String> getScopes() { return scopes; }
        public String getAccessTokenTtl() { return accessTokenTtl; }
        public String getRefreshTokenTtl() { return refreshTokenTtl; }
        public boolean isRequireConsent() { return requireConsent; }
        public boolean isReuseRefreshTokens() { return reuseRefreshTokens; }
        public LocalDateTime getRegisteredTime() { return registeredTime; }
    }

    public static class ClientStats {
        private final int totalClients;
        private final int webClients;
        private final int apiClients;
        private final int totalScopes;

        public ClientStats(int totalClients, int webClients, int apiClients, int totalScopes) {
            this.totalClients = totalClients;
            this.webClients = webClients;
            this.apiClients = apiClients;
            this.totalScopes = totalScopes;
        }

        public int getTotalClients() { return totalClients; }
        public int getWebClients() { return webClients; }
        public int getApiClients() { return apiClients; }
        public int getTotalScopes() { return totalScopes; }
    }
}