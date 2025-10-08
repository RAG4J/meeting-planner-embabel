package com.meetingplanner.auth.controller;

import com.meetingplanner.auth.service.ClientManagementService;
import com.meetingplanner.auth.service.SessionManagementService;
import com.meetingplanner.auth.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Set;
import java.util.HashSet;

/**
 * Admin controller for managing users, sessions, and OAuth2 clients.
 * 
 * This controller provides administrative functionality that is restricted
 * to users with ADMIN role only.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserManagementService userManagementService;
    private final SessionManagementService sessionManagementService;
    private final ClientManagementService clientManagementService;

    public AdminController(UserManagementService userManagementService,
                          SessionManagementService sessionManagementService,
                          ClientManagementService clientManagementService) {
        this.userManagementService = userManagementService;
        this.sessionManagementService = sessionManagementService;
        this.clientManagementService = clientManagementService;
    }

    /**
     * Admin dashboard - main overview page.
     */
    @GetMapping
    public String dashboard(Model model, Principal principal) {
        logger.info("Admin dashboard accessed by: {}", principal.getName());
        
        // Get statistics for the dashboard
        var userStats = userManagementService.getAllUsers().size();
        var sessionStats = sessionManagementService.getSessionStats();
        var clientStats = clientManagementService.getClientStats();
        
        model.addAttribute("userCount", userStats);
        model.addAttribute("sessionStats", sessionStats);
        model.addAttribute("clientStats", clientStats);
        model.addAttribute("currentUser", principal.getName());
        
        return "admin/dashboard";
    }

    // ========== USER MANAGEMENT ==========

    /**
     * List all users.
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userManagementService.getAllUsers());
        return "admin/users";
    }

    /**
     * Show user creation form.
     */
    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("availableRoles", userManagementService.getAvailableRoles());
        return "admin/user-create";
    }

    /**
     * Create a new user.
     */
    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam(required = false) Set<String> roles,
                            RedirectAttributes redirectAttributes) {
        try {
            if (roles == null) {
                roles = Set.of("USER"); // Default role
            }
            
            userManagementService.createUser(username, password, roles);
            redirectAttributes.addFlashAttribute("successMessage", 
                "User '" + username + "' created successfully.");
            logger.info("User created: {} with roles: {}", username, roles);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to create user: " + e.getMessage());
            logger.error("Failed to create user: {}", username, e);
        }
        
        return "redirect:/admin/users";
    }

    /**
     * Show user edit form.
     */
    @GetMapping("/users/{username}/edit")
    public String editUserForm(@PathVariable String username, Model model, RedirectAttributes redirectAttributes) {
        var userOpt = userManagementService.getUser(username);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found: " + username);
            return "redirect:/admin/users";
        }
        
        model.addAttribute("user", userOpt.get());
        model.addAttribute("availableRoles", userManagementService.getAvailableRoles());
        return "admin/user-edit";
    }

    /**
     * Update an existing user.
     */
    @PostMapping("/users/{username}/edit")
    public String updateUser(@PathVariable String username,
                            @RequestParam(required = false) String password,
                            @RequestParam(required = false) Set<String> roles,
                            RedirectAttributes redirectAttributes) {
        try {
            if (roles == null) {
                roles = Set.of("USER"); // Default role
            }
            
            userManagementService.updateUser(username, password, roles);
            redirectAttributes.addFlashAttribute("successMessage", 
                "User '" + username + "' updated successfully.");
            logger.info("User updated: {} with roles: {}", username, roles);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update user: " + e.getMessage());
            logger.error("Failed to update user: {}", username, e);
        }
        
        return "redirect:/admin/users";
    }

    /**
     * Delete a user.
     */
    @PostMapping("/users/{username}/delete")
    public String deleteUser(@PathVariable String username, Principal principal, 
                            RedirectAttributes redirectAttributes) {
        try {
            // Prevent admin from deleting themselves
            if (username.equals(principal.getName())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "You cannot delete your own account.");
                return "redirect:/admin/users";
            }
            
            userManagementService.deleteUser(username);
            redirectAttributes.addFlashAttribute("successMessage", 
                "User '" + username + "' deleted successfully.");
            logger.info("User deleted: {}", username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to delete user: " + e.getMessage());
            logger.error("Failed to delete user: {}", username, e);
        }
        
        return "redirect:/admin/users";
    }

    // ========== SESSION MANAGEMENT ==========

    /**
     * View session overview.
     */
    @GetMapping("/sessions")
    public String sessions(Model model) {
        model.addAttribute("sessionStats", sessionManagementService.getSessionStats());
        model.addAttribute("authorizations", sessionManagementService.getOAuth2Authorizations());
        return "admin/sessions";
    }

    /**
     * View sessions for a specific user.
     */
    @GetMapping("/sessions/user/{username}")
    public String userSessions(@PathVariable String username, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("sessions", sessionManagementService.getUserSessions(username));
        model.addAttribute("authorizations", sessionManagementService.getUserAuthorizations(username));
        return "admin/user-sessions";
    }

    /**
     * Revoke an OAuth2 authorization.
     */
    @PostMapping("/sessions/revoke/{authorizationId}")
    public String revokeAuthorization(@PathVariable String authorizationId, 
                                     RedirectAttributes redirectAttributes) {
        try {
            sessionManagementService.revokeAuthorization(authorizationId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Authorization revoked successfully.");
            logger.info("OAuth2 authorization revoked: {}", authorizationId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to revoke authorization: " + e.getMessage());
            logger.error("Failed to revoke authorization: {}", authorizationId, e);
        }
        
        return "redirect:/admin/sessions";
    }

    // ========== CLIENT MANAGEMENT ==========

    /**
     * List all OAuth2 clients.
     */
    @GetMapping("/clients")
    public String listClients(Model model) {
        model.addAttribute("clients", clientManagementService.getAllClients());
        model.addAttribute("clientStats", clientManagementService.getClientStats());
        return "admin/clients";
    }

    /**
     * View client details.
     */
    @GetMapping("/clients/{clientId}")
    public String clientDetails(@PathVariable String clientId, Model model, 
                               RedirectAttributes redirectAttributes) {
        var clientOpt = clientManagementService.getClient(clientId);
        if (clientOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Client not found: " + clientId);
            return "redirect:/admin/clients";
        }
        
        model.addAttribute("client", clientOpt.get());
        model.addAttribute("authorizations", 
            sessionManagementService.getOAuth2Authorizations().stream()
                .filter(auth -> clientId.equals(auth.getClientId()))
                .toList());
        
        return "admin/client-details";
    }
}