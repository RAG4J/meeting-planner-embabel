package com.meetingplanner.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.OidcProviderConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * OAuth2 Authorization Server Configuration for Meeting Planner Application.
 * <p>
 * This configuration sets up:
 * - OAuth2 clients for web-app and MCP server components
 * - Demo users for testing
 * - JWT token configuration
 * - Security filter chains
 * - Server-to-server authentication for MCP communication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher()) // ~~~ limit to oauth2 endpoints
                .with(authorizationServerConfigurer, authorizationServer -> // ~~~ use the provided configurer
                        authorizationServer
                                .authorizationEndpoint(authorizationEndpoint ->
                                        authorizationEndpoint.consentPage("/oauth2/consent")) // ~~~ register custom consent page
                                .oidc(oidc -> oidc
                                        .providerConfigurationEndpoint(providerConfiguration ->
                                                providerConfiguration.providerConfigurationCustomizer(this::customizeProviderConfiguration)
                                        )
                                )
                )
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());

        http
                .exceptionHandling((exceptions) -> // If any errors occur redirect user to login page
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // enable auth server to accept JWT for endpoints such as /userinfo
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2) // security filter chain for the rest of your application and any custom endpoints you may have
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(Customizer.withDefaults()) // Enable form login
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout") // Redirect to index page after logout with parameter
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
        .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/").permitAll() // ~~~ index page is public
                        .requestMatchers("/actuator/health").permitAll() // ~~~ health endpoint is public
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Admin endpoints require ADMIN role
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/favicon.ico").permitAll() // Static
                        // resources
                        .anyRequest().authenticated());

        return http.build();
    }

    /**
     * Demo user details service with test users.
     * Returns InMemoryUserDetailsManager to allow user management.
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user1 = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("USER", "ADMIN")
                .build();

        UserDetails meetingPlanner = User.builder()
                .username("planner")
                .password(passwordEncoder.encode("planner"))
                .roles("USER", "MEETING_PLANNER")
                .build();

        return new InMemoryUserDetailsManager(user1, admin, meetingPlanner);
    }

    /**
     * Registered client repository with OAuth2 clients.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        // Web App Client - Authorization Code Flow
        RegisteredClient webAppClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("meeting-planner-web")
                .clientSecret(passwordEncoder.encode("web-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login/oauth2/code/meeting-planner")
                .redirectUri("http://localhost:8080/authorized")
                .postLogoutRedirectUri("http://localhost:8080/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope("meeting.read")
                .scope("meeting.write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        // Location MCP Client - Client Credentials Flow (Server-to-Server)
        RegisteredClient locationMcpClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("location-mcp")
                .clientSecret(passwordEncoder.encode("mcp-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("mcp.invoke")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false) // No consent required for server-to-server
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1)) // Server-to-server tokens can be shorter
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(webAppClient, locationMcpClient);
    }

    /**
     * Authorization server settings.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000") // Use auth-server hostname to match /etc/hosts
                .build();
    }

    /**
     * JWT decoder for validating tokens.
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * JWK source for JWT token signing.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * Password encoder for client secrets and user passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * OAuth2 authorization service for managing authorizations.
     */
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        // In production, you'd use a database-backed implementation
        return new InMemoryOAuth2AuthorizationService();
    }

    /**
     * OAuth2 authorization consent service for managing user consent.
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        // In production, you'd use a database-backed implementation
        return new InMemoryOAuth2AuthorizationConsentService();
    }


    /**
     * Customize OIDC provider configuration to include custom scopes.
     */
    private void customizeProviderConfiguration(OidcProviderConfiguration.Builder builder) {
        builder.scopes(scopes -> {
            scopes.add(OidcScopes.OPENID);
            scopes.add(OidcScopes.PROFILE);
            scopes.add(OidcScopes.EMAIL);
            scopes.add("meeting.read");
            scopes.add("meeting.write");
        });
    }


    /**
     * Generate RSA key pair for JWT signing.
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}