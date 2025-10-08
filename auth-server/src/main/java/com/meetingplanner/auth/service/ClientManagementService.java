package com.meetingplanner.auth.service;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

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

    private final RegisteredClientRepository registeredClientRepository;

    public ClientManagementService(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    /**
     * Get all registered clients.
     */
    public List<ClientInfo> getAllClients() {
        List<ClientInfo> clients = new ArrayList<>();
        
        if (registeredClientRepository instanceof InMemoryRegisteredClientRepository) {
            try {
                // Access internal map using reflection
                var field = InMemoryRegisteredClientRepository.class.getDeclaredField("registrations");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, RegisteredClient> clientMap = 
                    (Map<String, RegisteredClient>) field.get(registeredClientRepository);
                
                for (RegisteredClient client : clientMap.values()) {
                    clients.add(toClientInfo(client));
                }
            } catch (Exception e) {
                // Fallback: return empty list if reflection fails
            }
        }
        
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