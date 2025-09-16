package org.rag4j.meetingplanner.webapp.controller;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.meetingplanner.agent.model.meeting.MeetingRequest;
import org.rag4j.meetingplanner.agent.model.meeting.MeetingResponse;
import org.rag4j.meetingplanner.agent.model.person.Person;
import org.rag4j.meetingplanner.common.model.Agenda;
import org.rag4j.meetingplanner.agent.service.MeetingService;
import org.rag4j.meetingplanner.agent.service.PersonFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final AgentPlatform agentPlatform;
    private final MeetingService meetingService;
    private final PersonFinder personFinder;

    public HomeController(AgentPlatform agentPlatform, MeetingService meetingService, PersonFinder personFinder) {
        this.agentPlatform = agentPlatform;
        this.meetingService = meetingService;
        this.personFinder = personFinder;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("title", "Meeting Planner");
        
        // Add authentication info to model
        if (authentication != null && authentication.isAuthenticated()) {
            String username = "Unknown";
            String email = "";
            
            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                username = oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getGivenName();
                email = oidcUser.getEmail();
            } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                username = oauth2User.getAttribute("name");
                email = oauth2User.getAttribute("email");
            }
            
            model.addAttribute("authenticated", true);
            model.addAttribute("username", username != null ? username : authentication.getName());
            model.addAttribute("email", email);
            model.addAttribute("message", "Welcome back, " + (username != null ? username : authentication.getName()) + "!");
        } else {
            model.addAttribute("authenticated", false);
            model.addAttribute("message", "Welcome to your Meeting Planner! Please log in to continue.");
        }
        
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
            
            var agentInvocation = AgentInvocation.create(agentPlatform, MeetingResponse.class);
            MeetingResponse meeting = agentInvocation.invoke(meetingRequest);
            if (meeting.getMeeting().isPresent()) {
                model.addAttribute("success", "Meeting created successfully!");
                meetingService.addMeeting(meeting.getMeeting().get());
            } else {
                model.addAttribute("error", "Failed to create meeting: " + meeting.getMessage());
                model.addAttribute("title", "Create Meeting");
                model.addAttribute("meetingRequest", meetingRequest);
                return "create-meeting";
            }
            return "redirect:/meetings";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("error", "Failed to create meeting: " + e.getMessage());
            model.addAttribute("title", "Create Meeting");
            model.addAttribute("meetingRequest", meetingRequest);
            return "create-meeting";
        }
    }

    @GetMapping("/persons")
    public String persons(Model model) {
        var persons = personFinder.getAllPersons();
        model.addAttribute("title", "Persons");
        model.addAttribute("persons", persons);
        return "persons";
    }

    @GetMapping("/persons/{email}/agenda")
    public String personAgenda(@PathVariable String email, 
                              @RequestParam(required = false) String date,
                              Model model) {
        Person person = personFinder.findByEmail(email);
        if (person == null) {
            model.addAttribute("error", "Person not found");
            return "redirect:/persons";
        }
        
        LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        var agendaItems = person.agenda().getMeetings().stream()
                .filter(item -> item.day().equals(selectedDate))
                .sorted(Comparator.comparing(Agenda.AgendaItem::start))
                .toList();
        
        var availableSlots = person.availabilityForDay(selectedDate);
        
        model.addAttribute("title", person.name() + "'s Agenda");
        model.addAttribute("person", person);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("agendaItems", agendaItems);
        model.addAttribute("availableSlots", availableSlots);
        return "person-agenda";
    }
    
    /**
     * Handle login errors from OAuth2 authentication.
     */
    @GetMapping("/login")
    public String loginError(@RequestParam(value = "error", required = false) String error, Model model) {
        if ("true".equals(error)) {
            model.addAttribute("title", "Login Error");
            return "login-error";
        }
        // If no error, redirect to OAuth2 login
        return "redirect:/oauth2/authorization/meeting-planner";
    }

}
