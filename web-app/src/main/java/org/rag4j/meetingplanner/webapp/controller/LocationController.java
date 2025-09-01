package org.rag4j.meetingplanner.webapp.controller;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.meetingplanner.agent.model.location.Location;
import org.rag4j.meetingplanner.agent.model.location.LocationRequest;
import org.rag4j.meetingplanner.agent.model.meeting.MeetingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LocationController {
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final AgentPlatform agentPlatform;

    public LocationController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/locations")
    public String locations(Model model) {
        model.addAttribute("title", "Location Lookup");
        return "locations";
    }

    @PostMapping("/locations")
    public String searchLocations(@RequestParam("description") String description, Model model) {
        logger.info("Location search request received: {}", description);

        var agentInvocation = AgentInvocation.create(agentPlatform, Location.class);
        Location location = agentInvocation.invoke(new LocationRequest(description));
        logger.info("Location found: {}", location);
        model.addAttribute("location", location);

        model.addAttribute("title", "Location Lookup");
        model.addAttribute("searchDescription", description);
        model.addAttribute("success", "Location search request logged: " + description);
        
        return "locations";
    }
}
