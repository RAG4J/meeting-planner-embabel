package org.rag4j.meetingplanner.webapp.controller;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.meetingplanner.agent.model.Meeting;
import org.rag4j.meetingplanner.agent.model.MeetingRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HomeController {
    private final AgentPlatform agentPlatform;

    public HomeController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
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
        model.addAttribute("meetings", List.of());
        return "meetings";
    }

    @GetMapping("/create-meeting")
    public String createMeeting(Model model) {
        model.addAttribute("title", "Create Meeting");
        return "create-meeting";
    }

    @PostMapping("/create-meeting")
    public String handleCreateMeeting(MeetingRequest meetingRequest, Model model) {
        var agentInvocation = AgentInvocation.create(agentPlatform, Meeting.class);
        Meeting meeting = agentInvocation.invoke(meetingRequest);
        model.addAttribute("meetings", List.of(meeting));
        return "redirect:/meetings";
    }

}
