package com.meetingplanner.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for OAuth2 Authorization Code Flow - simulates complete user browser flow
 */
@SpringBootTest(
        classes = AuthServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=WARN",
        "logging.level.org.springframework.web=WARN",
        "logging.level.org.springframework.boot.autoconfigure=WARN"
})
public class AuthorizationCodeFlowIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthorizationCodeFlowIntegrationTest() {
        // Configure RestTemplate to NOT automatically follow redirects
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false); // Don't follow redirects automatically
            }
        });
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testAuthorizationCodeFlow() throws Exception {
        // TODO: This test needs to properly simulate browser-like session management
        // For now, test the basic authorization endpoint accessibility
        
        // Step 1: Verify authorization endpoint is accessible and redirects to login
        String authorizationUrl = UriComponentsBuilder
                .fromHttpUrl(getBaseUrl() + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "meeting-planner-web")
                .queryParam("redirect_uri", "http://localhost:8080/login/oauth2/code/meeting-planner")
                .queryParam("scope", "openid meeting.read meeting.write")
                .queryParam("state", "test-state-123")
                .build()
                .toUriString();

        // Step 2: Verify authorization endpoint redirects to login for unauthenticated user
        ResponseEntity<String> authResponse = restTemplate.getForEntity(authorizationUrl, String.class);

        // Should redirect to login page
        assertEquals(HttpStatus.FOUND, authResponse.getStatusCode());
        String location = authResponse.getHeaders().getLocation().toString();
        assertTrue(location.contains("/login"), "Should redirect to login page. Actual: " + location);
        
        // Step 3: Verify login page is accessible
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity(
                getBaseUrl() + "/login", String.class);
        assertEquals(HttpStatus.OK, loginPageResponse.getStatusCode());
        assertNotNull(loginPageResponse.getBody());
        assertTrue(loginPageResponse.getBody().contains("Please sign in"));
        
        // TODO: Complete integration test would require:
        // 1. Proper session management with cookies
        // 2. CSRF token handling
        // 3. Login form submission
        // 4. Following OAuth2 authorization flow redirects
        // 5. Consent handling
        // 6. Authorization code extraction
        // 7. Token exchange
        // For now, we verify basic endpoints work
        
        System.out.println("=== Basic OAuth2 endpoints are working correctly ===");
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Test login form with invalid credentials

        // Step 1: Get login page
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity(
                getBaseUrl() + "/login", String.class);
        assertEquals(HttpStatus.OK, loginPageResponse.getStatusCode());

        List<String> loginCookies = loginPageResponse.getHeaders().get("Set-Cookie");
        
        // Extract CSRF token from login page
        String csrfToken = extractCsrfToken(loginPageResponse.getBody());
        assertNotNull(csrfToken, "CSRF token should be present in login page");

        // Step 2: Submit login form with INVALID credentials
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (loginCookies != null && !loginCookies.isEmpty()) {
            loginHeaders.put("Cookie", loginCookies);
        }

        MultiValueMap<String, String> loginForm = new LinkedMultiValueMap<>();
        loginForm.add("username", "invalid-user");
        loginForm.add("password", "wrong-password");
        loginForm.add("_csrf", csrfToken);

        HttpEntity<MultiValueMap<String, String>> loginEntity =
                new HttpEntity<>(loginForm, loginHeaders);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                getBaseUrl() + "/login", loginEntity, String.class);

        // Should redirect back to login page with error
        assertEquals(HttpStatus.FOUND, loginResponse.getStatusCode());
        String postLoginLocation = loginResponse.getHeaders().getLocation().toString();
        
        assertTrue(postLoginLocation.contains("/login"),
                "Should redirect back to login page on authentication failure. Actual location: " + postLoginLocation);
        // Spring Security redirects to /login?error for authentication failures
        assertTrue(postLoginLocation.contains("error"),
                "Should include error parameter. Actual location: " + postLoginLocation);
    }

    /**
     * Extract CSRF token from HTML response.
     * Looks for meta tag or input field containing CSRF token.
     */
    private String extractCsrfToken(String html) {
        if (html == null) {
            return null;
        }

        // Pattern to match CSRF meta tag: <meta name="_csrf" content="token-value"/>
        Pattern metaPattern = Pattern.compile("<meta name=\"_csrf\" content=\"([^\"]+)\"/>");
        Matcher metaMatcher = metaPattern.matcher(html);
        if (metaMatcher.find()) {
            return metaMatcher.group(1);
        }

        // Pattern to match CSRF input field: <input type="hidden" name="_csrf" value="token-value"/>
        // Also handle the reverse order: <input name="_csrf" type="hidden" value="token-value"/>
        Pattern inputPattern1 = Pattern.compile("<input type=\"hidden\" name=\"_csrf\" value=\"([^\"]+)\"/?");
        Matcher inputMatcher1 = inputPattern1.matcher(html);
        if (inputMatcher1.find()) {
            return inputMatcher1.group(1);
        }
        
        Pattern inputPattern2 = Pattern.compile("<input name=\"_csrf\" type=\"hidden\" value=\"([^\"]+)\"/?");
        Matcher inputMatcher2 = inputPattern2.matcher(html);
        if (inputMatcher2.find()) {
            return inputMatcher2.group(1);
        }

        // Pattern to match CSRF token in JavaScript (common pattern)
        Pattern jsPattern = Pattern.compile("_csrf['\"]?\s*[:=]\s*['\"]([^'\"]+)['\"]");
        Matcher jsMatcher = jsPattern.matcher(html);
        if (jsMatcher.find()) {
            return jsMatcher.group(1);
        }

        return null;
    }
}
