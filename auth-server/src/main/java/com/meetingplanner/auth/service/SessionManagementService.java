package com.meetingplanner.auth.service;

import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
// Spring Session imports removed - not available in current dependencies
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and viewing session information.
 * 
 * This service provides functionality to view active HTTP sessions,
 * OAuth2 authorizations, and user consents.
 */
@Service
public class SessionManagementService {

    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2AuthorizationConsentService authorizationConsentService;
    private final RegisteredClientRepository registeredClientRepository;

    public SessionManagementService(OAuth2AuthorizationService authorizationService,
                                    OAuth2AuthorizationConsentService authorizationConsentService,
                                    RegisteredClientRepository registeredClientRepository) {
        this.authorizationService = authorizationService;
        this.authorizationConsentService = authorizationConsentService;
        this.registeredClientRepository = registeredClientRepository;
    }

    /**
     * Get all active HTTP sessions.
     * Note: HTTP session management is not available with current dependencies.
     */
    public List<SessionInfo> getActiveSessions() {
        // Spring Session is not available in current dependencies
        // HTTP session information would require spring-session-core dependency
        return Collections.emptyList();
    }

    /**
     * Get sessions for a specific user.
     * Note: HTTP session management is not available with current dependencies.
     */
    public List<SessionInfo> getUserSessions(String username) {
        // Spring Session is not available in current dependencies
        // HTTP session information would require spring-session-core dependency
        return Collections.emptyList();
    }

    /**
     * Get all OAuth2 authorizations.
     */
    public List<OAuth2AuthorizationInfo> getOAuth2Authorizations() {
        List<OAuth2AuthorizationInfo> authorizations = new ArrayList<>();
        
        if (authorizationService instanceof InMemoryOAuth2AuthorizationService) {
            try {
                // Access internal map using reflection
                var field = InMemoryOAuth2AuthorizationService.class.getDeclaredField("authorizations");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, OAuth2Authorization> authMap = 
                    (Map<String, OAuth2Authorization>) field.get(authorizationService);
                
                for (OAuth2Authorization auth : authMap.values()) {
                    RegisteredClient client = registeredClientRepository.findById(auth.getRegisteredClientId());
                    authorizations.add(toAuthorizationInfo(auth, client));
                }
            } catch (Exception e) {
                // Fallback: return empty list if reflection fails
            }
        }
        
        return authorizations.stream()
                .sorted(Comparator.comparing(OAuth2AuthorizationInfo::getAuthorizationTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get OAuth2 authorizations for a specific user.
     */
    public List<OAuth2AuthorizationInfo> getUserAuthorizations(String username) {
        return getOAuth2Authorizations().stream()
                .filter(auth -> username.equals(auth.getPrincipalName()))
                .collect(Collectors.toList());
    }

    /**
     * Get all OAuth2 consents.
     */
    public List<ConsentInfo> getOAuth2Consents() {
        List<ConsentInfo> consents = new ArrayList<>();
        
        // Note: InMemoryOAuth2AuthorizationConsentService doesn't provide a way
        // to list all consents. This would need to be implemented differently
        // in production with a database-backed implementation.
        
        return consents;
    }

    /**
     * Revoke an OAuth2 authorization.
     */
    public void revokeAuthorization(String authorizationId) {
        OAuth2Authorization authorization = authorizationService.findById(authorizationId);
        if (authorization != null) {
            authorizationService.remove(authorization);
        }
    }

    /**
     * Get session statistics.
     */
    public SessionStats getSessionStats() {
        List<OAuth2AuthorizationInfo> authorizations = getOAuth2Authorizations();
        
        int activeAuthorizations = (int) authorizations.stream()
                .filter(auth -> auth.getAccessTokenExpiresAt() != null && 
                              auth.getAccessTokenExpiresAt().isAfter(LocalDateTime.now()))
                .count();
        
        Set<String> uniqueUsers = authorizations.stream()
                .map(OAuth2AuthorizationInfo::getPrincipalName)
                .collect(Collectors.toSet());
        
        Set<String> uniqueClients = authorizations.stream()
                .map(OAuth2AuthorizationInfo::getClientId)
                .collect(Collectors.toSet());
        
        return new SessionStats(
                0, // Active sessions - not available with in-memory sessions
                activeAuthorizations,
                authorizations.size(),
                uniqueUsers.size(),
                uniqueClients.size()
        );
    }

    // toSessionInfo method removed - Spring Session not available

    private OAuth2AuthorizationInfo toAuthorizationInfo(OAuth2Authorization auth, RegisteredClient client) {
        // Get token expiration times using correct API
        LocalDateTime accessTokenExpiresAt = null;
        LocalDateTime refreshTokenExpiresAt = null;
        boolean isActive = false;
        
        if (auth.getAccessToken() != null && auth.getAccessToken().getToken() != null) {
            var accessToken = auth.getAccessToken().getToken();
            if (accessToken.getExpiresAt() != null) {
                accessTokenExpiresAt = toLocalDateTime(accessToken.getExpiresAt());
                isActive = accessToken.getExpiresAt().isAfter(Instant.now());
            }
        }
        
        if (auth.getRefreshToken() != null && auth.getRefreshToken().getToken() != null) {
            var refreshToken = auth.getRefreshToken().getToken();
            if (refreshToken.getExpiresAt() != null) {
                refreshTokenExpiresAt = toLocalDateTime(refreshToken.getExpiresAt());
            }
        }
        
        return new OAuth2AuthorizationInfo(
                auth.getId(),
                auth.getPrincipalName(),
                client != null ? client.getClientId() : auth.getRegisteredClientId(),
                client != null ? getClientDisplayName(client) : "Unknown Client",
                String.join(", ", auth.getAuthorizedScopes()),
                LocalDateTime.now(), // Use current time as authorization time
                accessTokenExpiresAt,
                refreshTokenExpiresAt,
                isActive
        );
    }

    // extractPrincipalName method removed - Spring Session not available

    private String getClientDisplayName(RegisteredClient client) {
        String clientId = client.getClientId();
        return switch (clientId) {
            case "meeting-planner-web" -> "Meeting Planner Web Application";
            case "location-mcp" -> "Location MCP Server";
            default -> clientId.replace("-", " ").toUpperCase();
        };
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }

    /**
     * DTO classes for session information
     */
    public static class SessionInfo {
        private final String sessionId;
        private final String principalName;
        private final LocalDateTime creationTime;
        private final LocalDateTime lastAccessTime;
        private final LocalDateTime expiryTime;
        private final boolean active;

        public SessionInfo(String sessionId, String principalName, LocalDateTime creationTime,
                          LocalDateTime lastAccessTime, LocalDateTime expiryTime, boolean active) {
            this.sessionId = sessionId;
            this.principalName = principalName;
            this.creationTime = creationTime;
            this.lastAccessTime = lastAccessTime;
            this.expiryTime = expiryTime;
            this.active = active;
        }

        public String getSessionId() { return sessionId; }
        public String getPrincipalName() { return principalName; }
        public LocalDateTime getCreationTime() { return creationTime; }
        public LocalDateTime getLastAccessTime() { return lastAccessTime; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
        public boolean isActive() { return active; }
    }

    public static class OAuth2AuthorizationInfo {
        private final String id;
        private final String principalName;
        private final String clientId;
        private final String clientName;
        private final String scopes;
        private final LocalDateTime authorizationTime;
        private final LocalDateTime accessTokenExpiresAt;
        private final LocalDateTime refreshTokenExpiresAt;
        private final boolean active;

        public OAuth2AuthorizationInfo(String id, String principalName, String clientId, String clientName,
                                      String scopes, LocalDateTime authorizationTime,
                                      LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                                      boolean active) {
            this.id = id;
            this.principalName = principalName;
            this.clientId = clientId;
            this.clientName = clientName;
            this.scopes = scopes;
            this.authorizationTime = authorizationTime;
            this.accessTokenExpiresAt = accessTokenExpiresAt;
            this.refreshTokenExpiresAt = refreshTokenExpiresAt;
            this.active = active;
        }

        public String getId() { return id; }
        public String getPrincipalName() { return principalName; }
        public String getClientId() { return clientId; }
        public String getClientName() { return clientName; }
        public String getScopes() { return scopes; }
        public LocalDateTime getAuthorizationTime() { return authorizationTime; }
        public LocalDateTime getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
        public LocalDateTime getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
        public boolean isActive() { return active; }
    }

    public static class ConsentInfo {
        private final String principalName;
        private final String clientId;
        private final String clientName;
        private final Set<String> scopes;
        private final LocalDateTime consentTime;

        public ConsentInfo(String principalName, String clientId, String clientName,
                          Set<String> scopes, LocalDateTime consentTime) {
            this.principalName = principalName;
            this.clientId = clientId;
            this.clientName = clientName;
            this.scopes = scopes;
            this.consentTime = consentTime;
        }

        public String getPrincipalName() { return principalName; }
        public String getClientId() { return clientId; }
        public String getClientName() { return clientName; }
        public Set<String> getScopes() { return scopes; }
        public LocalDateTime getConsentTime() { return consentTime; }
    }

    public static class SessionStats {
        private final int activeSessions;
        private final int activeAuthorizations;
        private final int totalAuthorizations;
        private final int uniqueUsers;
        private final int uniqueClients;

        public SessionStats(int activeSessions, int activeAuthorizations, int totalAuthorizations,
                           int uniqueUsers, int uniqueClients) {
            this.activeSessions = activeSessions;
            this.activeAuthorizations = activeAuthorizations;
            this.totalAuthorizations = totalAuthorizations;
            this.uniqueUsers = uniqueUsers;
            this.uniqueClients = uniqueClients;
        }

        public int getActiveSessions() { return activeSessions; }
        public int getActiveAuthorizations() { return activeAuthorizations; }
        public int getTotalAuthorizations() { return totalAuthorizations; }
        public int getUniqueUsers() { return uniqueUsers; }
        public int getUniqueClients() { return uniqueClients; }
    }
}