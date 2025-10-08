package com.meetingplanner.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing users in the authentication server.
 * 
 * This service provides CRUD operations for users while working with
 * the existing InMemoryUserDetailsManager.
 */
@Service
public class UserManagementService {

    private final InMemoryUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(InMemoryUserDetailsManager userDetailsManager, 
                                 PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all users in the system.
     */
    public List<UserInfo> getAllUsers() {
        // Access internal users map using reflection since it's not publicly exposed
        try {
            var field = InMemoryUserDetailsManager.class.getDeclaredField("users");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, UserDetails> users = (Map<String, UserDetails>) field.get(userDetailsManager);
            
            return users.values().stream()
                    .map(this::toUserInfo)
                    .sorted(Comparator.comparing(UserInfo::getUsername))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    /**
     * Get a user by username.
     */
    public Optional<UserInfo> getUser(String username) {
        if (!userDetailsManager.userExists(username)) {
            return Optional.empty();
        }
        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        return Optional.of(toUserInfo(userDetails));
    }

    /**
     * Create a new user.
     */
    public void createUser(String username, String password, Set<String> roles) {
        if (userDetailsManager.userExists(username)) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .authorities(authorities)
                .build();

        userDetailsManager.createUser(user);
    }

    /**
     * Update an existing user.
     */
    public void updateUser(String username, String newPassword, Set<String> roles) {
        if (!userDetailsManager.userExists(username)) {
            throw new IllegalArgumentException("User does not exist: " + username);
        }

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails updatedUser = User.builder()
                .username(username)
                .password(newPassword != null && !newPassword.isEmpty() ? 
                         passwordEncoder.encode(newPassword) : 
                         userDetailsManager.loadUserByUsername(username).getPassword())
                .authorities(authorities)
                .build();

        userDetailsManager.updateUser(updatedUser);
    }

    /**
     * Delete a user.
     */
    public void deleteUser(String username) {
        if (!userDetailsManager.userExists(username)) {
            throw new IllegalArgumentException("User does not exist: " + username);
        }
        userDetailsManager.deleteUser(username);
    }

    /**
     * Check if a user exists.
     */
    public boolean userExists(String username) {
        return userDetailsManager.userExists(username);
    }

    /**
     * Get available roles that can be assigned to users.
     */
    public List<String> getAvailableRoles() {
        return Arrays.asList("USER", "ADMIN", "MEETING_PLANNER");
    }

    /**
     * Convert UserDetails to UserInfo DTO.
     */
    private UserInfo toUserInfo(UserDetails userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .collect(Collectors.toSet());

        return new UserInfo(
                userDetails.getUsername(),
                roles,
                userDetails.isEnabled(),
                userDetails.isAccountNonExpired(),
                userDetails.isAccountNonLocked(),
                userDetails.isCredentialsNonExpired()
        );
    }

    /**
     * DTO class for user information.
     */
    public static class UserInfo {
        private final String username;
        private final Set<String> roles;
        private final boolean enabled;
        private final boolean accountNonExpired;
        private final boolean accountNonLocked;
        private final boolean credentialsNonExpired;

        public UserInfo(String username, Set<String> roles, boolean enabled, 
                       boolean accountNonExpired, boolean accountNonLocked, 
                       boolean credentialsNonExpired) {
            this.username = username;
            this.roles = roles;
            this.enabled = enabled;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
        }

        public String getUsername() { return username; }
        public Set<String> getRoles() { return roles; }
        public boolean isEnabled() { return enabled; }
        public boolean isAccountNonExpired() { return accountNonExpired; }
        public boolean isAccountNonLocked() { return accountNonLocked; }
        public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    }
}