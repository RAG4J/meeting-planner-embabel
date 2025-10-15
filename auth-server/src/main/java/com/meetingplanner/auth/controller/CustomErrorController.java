package com.meetingplanner.auth.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Custom error controller to handle HTTP errors with styled pages.
 * 
 * This controller provides custom error pages that match the application's
 * design instead of the default Spring Boot whitelabel error pages.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            model.addAttribute("statusCode", statusCode);
            model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "An error occurred");
            model.addAttribute("requestUri", requestUri != null ? requestUri : "Unknown");
            model.addAttribute("currentUser", principal != null ? principal.getName() : null);
            
            // Handle specific error codes
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }
        
        // Default error page
        return "error/generic";
    }
}