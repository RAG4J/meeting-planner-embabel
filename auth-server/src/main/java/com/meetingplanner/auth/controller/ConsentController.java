package com.meetingplanner.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.*;

/**
 * Controller for handling OAuth2 authorization consent.
 * 
 * This controller manages the consent screen that is displayed to users
 * when they need to authorize an OAuth2 client application to access
 * their resources with specific scopes/permissions.
 */
@Controller
public class ConsentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsentController.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationConsentService authorizationConsentService;

    public ConsentController(RegisteredClientRepository registeredClientRepository,
                             OAuth2AuthorizationConsentService authorizationConsentService) {
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationConsentService = authorizationConsentService;
    }

    /**
     * Display the consent page.
     * 
     * This endpoint is called by Spring Authorization Server when user consent
     * is required for an OAuth2 authorization request.
     */
    @GetMapping(value = "/oauth2/consent")
    public String consent(Principal principal, Model model,
                         @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                         @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                         @RequestParam(OAuth2ParameterNames.STATE) String state,
                         @RequestParam(value = OAuth2ParameterNames.RESPONSE_TYPE, required = false) String responseType,
                         @RequestParam(value = OAuth2ParameterNames.REDIRECT_URI, required = false) String redirectUri,
                         @RequestParam Map<String, Object> parameters) {
        
        LOGGER.info("=== CONSENT DEBUG INFO ===");
        LOGGER.info("Client ID: {}", clientId);
        LOGGER.info("Requested Scope: {}", scope);
        LOGGER.info("State: {}", state);
        LOGGER.info("Response Type (param): {}", responseType);
        LOGGER.info("Redirect URI (param): {}", redirectUri);
        LOGGER.info("Response Type (from map): {}", parameters.get("response_type"));
        LOGGER.info("Redirect URI (from map): {}", parameters.get("redirect_uri"));
        LOGGER.info("All parameters: {}", parameters);
        LOGGER.info("=========================");

        // Get the registered client information
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new IllegalArgumentException("Invalid client: " + clientId);
        }

        // Get user's existing consents for this client
        Set<String> authorizedScopes = Collections.emptySet();
        OAuth2AuthorizationConsent currentConsent = 
            this.authorizationConsentService.findById(registeredClient.getId(), principal.getName());
        if (currentConsent != null) {
            authorizedScopes = currentConsent.getScopes();
        }

        // Parse requested scopes (OAuth2 uses space-separated, not comma-separated)
        String[] scopeArray = StringUtils.tokenizeToStringArray(scope, " ");
        List<ScopeWithDescription> scopesForConsent = getScopeWithDescriptions(scopeArray, authorizedScopes);

        LOGGER.info("Scopes for consent: {}", scopesForConsent.size());
        for (ScopeWithDescription scopeDesc : scopesForConsent) {
            LOGGER.info("  - {} : {}", scopeDesc.getScope(), scopeDesc.getDescription());
        }

        // Filter parameters to only include the ones needed for the authorization request
        Map<String, Object> filteredParameters = new HashMap<>();
        filteredParameters.put("client_id", clientId);
        filteredParameters.put("response_type", responseType != null ? responseType : parameters.get("response_type"));
        filteredParameters.put("redirect_uri", redirectUri != null ? redirectUri : parameters.get("redirect_uri"));
        filteredParameters.put("state", state);
        // Don't include the original scope parameter - we'll handle scopes separately
        
        LOGGER.info("Filtered parameters: {}", filteredParameters);
        
        // Add attributes to the model for the template
        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", getClientDisplayName(registeredClient));
        model.addAttribute("state", state);
        model.addAttribute("scopes", scopesForConsent);
        model.addAttribute("principalName", principal.getName());
        model.addAttribute("parameters", filteredParameters);

        return "consent";
    }

    private List<ScopeWithDescription> getScopeWithDescriptions(String[] scopeArray, Set<String> authorizedScopes) {
        Set<String> requestedScopes = new HashSet<>(Arrays.asList(scopeArray));

        // Create scope objects with descriptions for the template
        // Include ALL requested scopes, even if user has already consented
        // This ensures the consent form submits all required scopes
        List<ScopeWithDescription> scopesForConsent = new ArrayList<>();
        for (String requestedScope : requestedScopes) {
            ScopeWithDescription scopeInfo = new ScopeWithDescription();
            scopeInfo.setScope(requestedScope);
            scopeInfo.setDescription(getScopeDescription(requestedScope));
            scopeInfo.setAlreadyApproved(authorizedScopes.contains(requestedScope));
            scopesForConsent.add(scopeInfo);
        }
        return scopesForConsent;
    }

    /**
     * Get a user-friendly display name for the client.
     */
    private String getClientDisplayName(RegisteredClient client) {
        // You could store display names in client metadata or database
        // For now, convert client ID to a more readable format
        String clientId = client.getClientId();
        return switch (clientId) {
            case "meeting-planner-web" -> "Meeting planner Web Application";
            case "location-mcp" -> "Locations MCP Server";
            default -> clientId.replace("-", " ").toUpperCase();
        };
    }

    /**
     * Get user-friendly descriptions for OAuth2 scopes.
     */
    private String getScopeDescription(String scope) {
        return switch (scope) {
            case "openid" -> "Verify your identity and sign you in";
            case "profile" -> "Access your basic profile information (name, username)";
            case "email" -> "Access your email address";
            case "meeting.read" -> "View the meetings that are planned";
            case "meeting.write" -> "Plan a meeting on your behalf";
            case "mcp.invoke" -> "Access MCP (Model Context Protocol) services on your behalf";
            case "offline_access" -> "Access your account when you're not actively using the app";
            default -> "Access " + scope + " permissions";
        };
    }

    /**
     * Helper class to represent a scope with its description for the template.
     */
    public static class ScopeWithDescription {
        private String scope;
        private String description;
        private boolean alreadyApproved = false;

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        
        public boolean isAlreadyApproved() {
            return alreadyApproved;
        }
        
        public void setAlreadyApproved(boolean alreadyApproved) {
            this.alreadyApproved = alreadyApproved;
        }
    }
}