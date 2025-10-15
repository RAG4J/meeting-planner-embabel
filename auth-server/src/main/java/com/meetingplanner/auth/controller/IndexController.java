package com.meetingplanner.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for handling the index/home page.
 * 
 * This controller serves the main landing page that introduces users
 * to the OAuth2 Authorization Server and provides navigation links.
 */
@Controller
public class IndexController {

    /**
     * Display the index page.
     * 
     * This endpoint serves the root path and provides an overview
     * of the authorization server and its capabilities.
     */
    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "logout", required = false) String logout) {
        model.addAttribute("serverVersion", "1.0.0");
        model.addAttribute("applicationName", "Meeting Planner Authorization Server");
        
        // Add logout success flag for template
        if (logout != null) {
            model.addAttribute("logoutSuccess", true);
        }
        
        return "index";
    }
}