package org.rag4j.meetingplanner.webapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller for handling OAuth2 logout operations.
 * Ensures proper logout from both the application and the OAuth2 provider.
 */
@Controller
public class LogoutController {
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${server.port:8080}")
    private String serverPort;

    public LogoutController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
     * Handle OAuth2 logout by clearing local session and redirecting to the 
     * authorization server's logout endpoint.
     */
    @PostMapping("/oauth2/logout")
    public String logout(HttpServletRequest request, Authentication authentication) {
        logger.info("Processing OAuth2 logout request");
        
        try {
            // Get the client registration for our OAuth2 provider
            ClientRegistration clientRegistration = this.clientRegistrationRepository
                    .findByRegistrationId("meeting-planner");
            
            if (clientRegistration != null && authentication != null && authentication.isAuthenticated()) {
                // Build the logout URL for the authorization server
                String logoutUrl = buildLogoutUrl(clientRegistration, authentication);
                
                // Perform local logout first
                SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
                logoutHandler.logout(request, null, authentication);
                
                logger.info("Local logout completed, redirecting to: {}", logoutUrl);
                return "redirect:" + logoutUrl;
            }
        } catch (Exception e) {
            logger.error("Error during OAuth2 logout", e);
        }
        
        // Fallback: redirect to home page
        logger.info("Fallback logout, redirecting to home");
        return "redirect:/";
    }

    /**
     * Build the logout URL for the OAuth2 authorization server.
     */
    private String buildLogoutUrl(ClientRegistration clientRegistration, Authentication authentication) {
        // Get the issuer URI from client registration
        String issuerUri = clientRegistration.getProviderDetails().getIssuerUri();
        
        // Construct the post-logout redirect URI
        String postLogoutRedirectUri = "http://localhost:" + serverPort + "/";
        
        // Build the logout URL
        String logoutUrl = UriComponentsBuilder.fromUriString(issuerUri)
                .path("/connect/logout")
                .queryParam("post_logout_redirect_uri", postLogoutRedirectUri)
                .build()
                .toUriString();

        // If we have an OIDC user with an ID token, include it for a cleaner logout
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String idToken = oidcUser.getIdToken().getTokenValue();
            logoutUrl = UriComponentsBuilder.fromUriString(logoutUrl)
                    .queryParam("id_token_hint", idToken)
                    .build()
                    .toUriString();
        }
        
        return logoutUrl;
    }
}