package org.rag4j.meetingplanner.webapp.controller;

import org.rag4j.meetingplanner.agent.service.EmbabelAgentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final EmbabelAgentService agentService;

    public HomeController(EmbabelAgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Meeting Planner");
        model.addAttribute("message", "Welcome to your Meeting Planner powered by Embabel!");
        return "index";
    }

    @GetMapping("/meetings")
    public String meetings(Model model) {
        model.addAttribute("title", "Meetings");
        model.addAttribute("meetings", agentService.getAllMeetings());
        return "meetings";
    }

    @GetMapping("/create-meeting")
    public String createMeeting(Model model) {
        model.addAttribute("title", "Create Meeting");
        return "create-meeting";
    }

}
