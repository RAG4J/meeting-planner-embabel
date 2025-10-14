package com.meetingplanner.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for OAuth2 Authorization Server functionality.
 * 
 * This test validates the complete OAuth2 server setup including:
 * - OIDC Discovery endpoint
 * - JWK Set endpoint 
 * - Client Credentials flow
 * - Token introspection
 * - Error handling for invalid requests
 * 
 * Tests only run when the 'integration-test' Maven profile is active:
 * mvn test -Pintegration-test
 */
@SpringBootTest(
    classes = AuthServerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
    "logging.level.org.springframework.security=WARN", // Reduce test noise
    "logging.level.org.springframework.web=WARN",
    "logging.level.org.springframework.boot.autoconfigure=WARN"
})
class OAuth2AuthServerIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testOidcDiscoveryEndpoint() throws Exception {
        // Test OIDC Discovery Document
        String url = getBaseUrl() + "/.well-known/openid-configuration";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        
        JsonNode discoveryDoc = objectMapper.readTree(response.getBody());
        
        // Validate required OIDC fields
        assertTrue(discoveryDoc.has("issuer"));
        assertTrue(discoveryDoc.has("authorization_endpoint"));
        assertTrue(discoveryDoc.has("token_endpoint"));
        assertTrue(discoveryDoc.has("jwks_uri"));
        assertTrue(discoveryDoc.has("userinfo_endpoint"));
        assertTrue(discoveryDoc.has("scopes_supported"));
        assertTrue(discoveryDoc.has("response_types_supported"));
        assertTrue(discoveryDoc.has("grant_types_supported"));
        
        // Validate custom scopes are included
        JsonNode scopes = discoveryDoc.get("scopes_supported");
        assertNotNull(scopes);
        assertTrue(scopes.isArray());
        
        boolean hasOpenid = false, hasMeetingRead = false, hasMeetingWrite = false;
        for (JsonNode scope : scopes) {
            String scopeValue = scope.asText();
            if ("openid".equals(scopeValue)) hasOpenid = true;
            if ("meeting.read".equals(scopeValue)) hasMeetingRead = true;
            if ("meeting.write".equals(scopeValue)) hasMeetingWrite = true;
        }
        
        assertTrue(hasOpenid, "Should support 'openid' scope");
        assertTrue(hasMeetingRead, "Should support 'meeting.read' scope");
        assertTrue(hasMeetingWrite, "Should support 'meeting.write' scope");
    }

    @Test
    void testJwkSetEndpoint() throws Exception {
        // Test JWK Set endpoint
        String url = getBaseUrl() + "/oauth2/jwks";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MediaType contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.isCompatibleWith(MediaType.APPLICATION_JSON));
        
        JsonNode jwkSet = objectMapper.readTree(response.getBody());
        
        assertTrue(jwkSet.has("keys"));
        JsonNode keys = jwkSet.get("keys");
        assertTrue(keys.isArray());
        assertTrue(keys.size() > 0);
        
        // Validate first key has required fields
        JsonNode firstKey = keys.get(0);
        assertTrue(firstKey.has("kty")); // Key type
        assertTrue(firstKey.has("kid")); // Key ID
        assertTrue(firstKey.has("n"));   // RSA modulus
        assertTrue(firstKey.has("e"));   // RSA exponent
    }

    @Test
    void testClientCredentialsFlow_LocationMcp_Success() throws Exception {
        // Test successful client credentials flow for location-mcp client
        String url = getBaseUrl() + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // location-mcp:mcp-secret -> base64 encoded
        String credentials = Base64.getEncoder().encodeToString("location-mcp:mcp-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("scope", "mcp.invoke");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MediaType contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.isCompatibleWith(MediaType.APPLICATION_JSON));

        JsonNode tokenResponse = objectMapper.readTree(response.getBody());
        
        // Validate token response structure
        assertTrue(tokenResponse.has("access_token"));
        assertTrue(tokenResponse.has("token_type"));
        assertTrue(tokenResponse.has("expires_in"));
        assertTrue(tokenResponse.has("scope"));
        
        assertEquals("Bearer", tokenResponse.get("token_type").asText());
        assertEquals("mcp.invoke", tokenResponse.get("scope").asText());
        assertTrue(tokenResponse.get("expires_in").asInt() > 0);
        
        String accessToken = tokenResponse.get("access_token").asText();
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());
        
        // Access token should be a JWT (has 3 parts separated by dots)
        String[] tokenParts = accessToken.split("\\.");
        assertEquals(3, tokenParts.length, "Access token should be a JWT with 3 parts");
    }

    @Test
    void testClientCredentialsFlow_InvalidScope() throws Exception {
        // Test client credentials flow with invalid scope
        String url = getBaseUrl() + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String credentials = Base64.getEncoder().encodeToString("location-mcp:mcp-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("scope", "invalid.scope meeting.read"); // location-mcp doesn't have these scopes

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            fail("Should have thrown HttpClientErrorException for invalid scope");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            
            JsonNode errorResponse = objectMapper.readTree(e.getResponseBodyAsString());
            assertEquals("invalid_scope", errorResponse.get("error").asText());
        }
    }

    @Test
    void testClientCredentialsFlow_InvalidClient() throws Exception {
        // Test client credentials flow with invalid client credentials
        String url = getBaseUrl() + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String credentials = Base64.getEncoder().encodeToString("invalid-client:wrong-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("scope", "mcp.invoke");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            fail("Should have thrown HttpClientErrorException for invalid client");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }

    @Test
    void testTokenIntrospection() throws Exception {
        // First, get a valid token
        String tokenResponse = getValidAccessToken();
        JsonNode tokenJson = objectMapper.readTree(tokenResponse);
        String accessToken = tokenJson.get("access_token").asText();
        
        // Now test token introspection
        String url = getBaseUrl() + "/oauth2/introspect";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Use location-mcp client for introspection
        String credentials = Base64.getEncoder().encodeToString("location-mcp:mcp-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("token", accessToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        JsonNode introspectionResponse = objectMapper.readTree(response.getBody());
        
        // Token should be active
        assertTrue(introspectionResponse.get("active").asBoolean());
        assertTrue(introspectionResponse.has("client_id"));
        assertTrue(introspectionResponse.has("scope"));
        assertTrue(introspectionResponse.has("exp"));
        assertTrue(introspectionResponse.has("iat"));
    }

    @Test
    void testTokenRevocation() throws Exception {
        // First, get a valid token
        String tokenResponse = getValidAccessToken();
        JsonNode tokenJson = objectMapper.readTree(tokenResponse);
        String accessToken = tokenJson.get("access_token").asText();
        
        // Now test token revocation
        String url = getBaseUrl() + "/oauth2/revoke";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String credentials = Base64.getEncoder().encodeToString("location-mcp:mcp-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("token", accessToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        // Token revocation should return 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // After revocation, introspection should show token as inactive
        String introspectionUrl = getBaseUrl() + "/oauth2/introspect";
        MultiValueMap<String, String> introspectionBody = new LinkedMultiValueMap<>();
        introspectionBody.add("token", accessToken);
        HttpEntity<MultiValueMap<String, String>> introspectionEntity = new HttpEntity<>(introspectionBody, headers);
        
        ResponseEntity<String> introspectionResponse = restTemplate.exchange(
            introspectionUrl, HttpMethod.POST, introspectionEntity, String.class);
        
        JsonNode introspectionJson = objectMapper.readTree(introspectionResponse.getBody());
        assertFalse(introspectionJson.get("active").asBoolean(), "Token should be inactive after revocation");
    }

    @Test
    void testHealthEndpoint() throws Exception {
        // Test health endpoint is accessible
        String url = getBaseUrl() + "/actuator/health";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        JsonNode healthResponse = objectMapper.readTree(response.getBody());
        assertEquals("UP", healthResponse.get("status").asText());
    }

    @Test
    void testMissingGrantType() throws Exception {
        // Test error handling for missing grant_type parameter
        String url = getBaseUrl() + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String credentials = Base64.getEncoder().encodeToString("location-mcp:mcp-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("scope", "mcp.invoke");
        // Missing grant_type parameter

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            fail("Should have thrown HttpClientErrorException for missing grant_type");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            
            JsonNode errorResponse = objectMapper.readTree(e.getResponseBodyAsString());
            assertEquals("invalid_request", errorResponse.get("error").asText());
        }
    }

    /**
     * Helper method to get a valid access token for testing other endpoints.
     */
    private String getValidAccessToken() throws Exception {
        String url = getBaseUrl() + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String credentials = Base64.getEncoder().encodeToString("location-mcp:mcp-secret".getBytes());
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("scope", "mcp.invoke");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }
}