package com.meetingplanner.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic integration test to verify the OAuth2 Authorization Server starts correctly.
 */
@SpringBootTest(
    classes = AuthServerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
    "logging.level.org.springframework.security=WARN", // Reduce test noise
    "logging.level.org.springframework.web=WARN"
})
class AuthServerApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with all OAuth2 Authorization Server configurations
    }
}