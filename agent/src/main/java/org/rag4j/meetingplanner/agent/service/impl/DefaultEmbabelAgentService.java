package org.rag4j.meetingplanner.agent.service.impl;

import org.rag4j.meetingplanner.agent.model.Meeting;
import org.rag4j.meetingplanner.agent.model.MeetingRequest;
import org.rag4j.meetingplanner.agent.model.MeetingStatus;
import org.rag4j.meetingplanner.agent.model.MeetingSuggestion;
import org.rag4j.meetingplanner.agent.service.EmbabelAgentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of EmbabelAgentService with in-memory storage
 * This will be replaced with actual Embabel integration
 */
@Service
public class DefaultEmbabelAgentService implements EmbabelAgentService {
    
    private final Map<String, Meeting> meetings = new ConcurrentHashMap<>();
    
    @Override
    public Meeting createMeeting(MeetingRequest request) {
        String id = UUID.randomUUID().toString();
        
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setStartTime(LocalDateTime.of(request.getDate(), request.getStartTime()));
        meeting.setDurationMinutes(request.getDurationMinutes());
        meeting.setType(request.getType());
        meeting.setLocation(request.getLocation());
        meeting.setParticipants(request.getParticipants());
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        
        // AI-powered agenda generation
        if (request.isGenerateAgenda()) {
            String agenda = generateAgenda(request.getTitle(), request.getDescription());
            meeting.setAgenda(agenda);
        }
        
        meetings.put(id, meeting);
        
        // TODO: Implement actual Embabel agent calls for:
        // - Optimal time suggestions
        // - Automated reminders
        // - Calendar integration
        
        return meeting;
    }
    
    @Override
    public List<Meeting> getAllMeetings() {
        return new ArrayList<>(meetings.values());
    }
    
    @Override
    public Optional<Meeting> getMeetingById(String id) {
        return Optional.ofNullable(meetings.get(id));
    }
    
    @Override
    public List<MeetingSuggestion> suggestOptimalTimes(MeetingRequest request) {
        // Mock implementation - replace with actual Embabel AI logic
        List<MeetingSuggestion> suggestions = new ArrayList<>();
        
        LocalDateTime baseTime = LocalDateTime.of(request.getDate(), request.getStartTime());
        
        // Suggest 3 different time slots
        suggestions.add(new MeetingSuggestion(
                baseTime,
                baseTime.plusMinutes(request.getDurationMinutes()),
                90,
                "All participants are available and this is within optimal working hours"
        ));
        
        suggestions.add(new MeetingSuggestion(
                baseTime.plusHours(1),
                baseTime.plusHours(1).plusMinutes(request.getDurationMinutes()),
                75,
                "Most participants are available, slight preference variation"
        ));
        
        suggestions.add(new MeetingSuggestion(
                baseTime.minusHours(1),
                baseTime.minusHours(1).plusMinutes(request.getDurationMinutes()),
                85,
                "Good availability and avoids conflicting meetings"
        ));
        
        // Sort by availability score descending
        suggestions.sort((a, b) -> Integer.compare(b.getAvailabilityScore(), a.getAvailabilityScore()));
        
        return suggestions;
    }
    
    @Override
    public String generateAgenda(String title, String description) {
        // Mock implementation - replace with actual Embabel AI logic
        StringBuilder agenda = new StringBuilder();
        agenda.append("# ").append(title).append("\\n\\n");
        
        if (description != null && !description.trim().isEmpty()) {
            agenda.append("## Overview\\n");
            agenda.append(description).append("\\n\\n");
        }
        
        agenda.append("## Agenda Items\\n");
        agenda.append("1. Welcome and introductions (5 min)\\n");
        
        // Generate context-aware agenda based on title keywords
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("standup") || lowerTitle.contains("daily")) {
            agenda.append("2. What did you work on yesterday? (10 min)\\n");
            agenda.append("3. What are you working on today? (10 min)\\n");
            agenda.append("4. Any blockers or challenges? (5 min)\\n");
        } else if (lowerTitle.contains("planning") || lowerTitle.contains("sprint")) {
            agenda.append("2. Review previous sprint results (10 min)\\n");
            agenda.append("3. Discuss upcoming priorities (15 min)\\n");
            agenda.append("4. Resource allocation and timeline (10 min)\\n");
        } else if (lowerTitle.contains("review") || lowerTitle.contains("retrospective")) {
            agenda.append("2. What went well? (10 min)\\n");
            agenda.append("3. What could be improved? (10 min)\\n");
            agenda.append("4. Action items for next iteration (10 min)\\n");
        } else {
            agenda.append("2. Main discussion topics (20 min)\\n");
            agenda.append("3. Decision points (10 min)\\n");
        }
        
        agenda.append("5. Next steps and action items (5 min)\\n");
        agenda.append("6. Wrap-up and next meeting (5 min)\\n");
        
        return agenda.toString();
    }
    
    @Override
    public Optional<Meeting> updateMeeting(String id, MeetingRequest request) {
        Meeting existing = meetings.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setStartTime(LocalDateTime.of(request.getDate(), request.getStartTime()));
        existing.setDurationMinutes(request.getDurationMinutes());
        existing.setType(request.getType());
        existing.setLocation(request.getLocation());
        existing.setParticipants(request.getParticipants());
        existing.setUpdatedAt(LocalDateTime.now());
        
        if (request.isGenerateAgenda()) {
            String agenda = generateAgenda(request.getTitle(), request.getDescription());
            existing.setAgenda(agenda);
        }
        
        return Optional.of(existing);
    }
    
    @Override
    public boolean deleteMeeting(String id) {
        return meetings.remove(id) != null;
    }
}
