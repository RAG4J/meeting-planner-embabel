package com.meetingplanner.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OAuth2 Authorization Server for Meeting Planner Application.
 * 
 * This application provides OAuth2 authentication and authorization services
 * for the meeting planner ecosystem, including the web-app and sse components.
 */
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}