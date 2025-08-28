package org.rag4j.meetingplanner.webapp.controller;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.meetingplanner.agent.model.Meeting;
import org.rag4j.meetingplanner.agent.model.MeetingRequest;
import org.rag4j.meetingplanner.agent.model.Person;
import org.rag4j.meetingplanner.agent.service.MeetingService;
import org.rag4j.meetingplanner.agent.service.PersonFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final AgentPlatform agentPlatform;
    private final MeetingService meetingService;

    public HomeController(AgentPlatform agentPlatform, MeetingService meetingService) {
        this.agentPlatform = agentPlatform;
        this.meetingService = meetingService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Meeting Planner");
        model.addAttribute("message", "Welcome to your Meeting Planner powered by Embabel!");
        return "index";
    }

    @GetMapping("/meetings")
    public String meetings(Model model) {
        var meetings = meetingService.getAllMeetings();

        model.addAttribute("title", "Meetings");
        model.addAttribute("meetings", meetings);
        return "meetings";
    }

    @GetMapping("/create-meeting")
    public String createMeeting(Model model) {
        model.addAttribute("title", "Create Meeting");
        model.addAttribute("meetingRequest", new MeetingRequest());
        return "create-meeting";
    }

    @PostMapping("/create-meeting")
    public String handleCreateMeeting(@ModelAttribute MeetingRequest meetingRequest, 
                                     @RequestParam(name = "participants", required = false) String participantsStr,
                                     Model model) {
        try {
            // Convert participants string to list
            if (participantsStr != null && !participantsStr.trim().isEmpty()) {
                List<String> participantList = Arrays.stream(participantsStr.split(","))
                        .map(String::trim)
                        .filter(email -> !email.isEmpty())
                        .toList();
                meetingRequest.setParticipants(participantList);
            }
            
            var agentInvocation = AgentInvocation.create(agentPlatform, Meeting.class);
            Meeting meeting = agentInvocation.invoke(meetingRequest);
            meetingService.addMeeting(meeting);
            model.addAttribute("success", "Meeting created successfully!");
            return "redirect:/meetings";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("error", "Failed to create meeting: " + e.getMessage());
            model.addAttribute("title", "Create Meeting");
            model.addAttribute("meetingRequest", meetingRequest);
            return "create-meeting";
        }
    }

}
