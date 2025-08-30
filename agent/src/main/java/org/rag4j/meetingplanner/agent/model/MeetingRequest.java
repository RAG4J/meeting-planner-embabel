package org.rag4j.meetingplanner.agent.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request object for creating or updating meetings
 */
public class MeetingRequest {
    
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private LocalDate date;
    
    @NotNull
    private LocalTime startTime;
    
    private Integer durationMinutes = 30; // Default 30 minutes
    
    private String location;
    
    private List<String> participants;
    
    // AI assistance flags
    private boolean suggestOptimalTime = false;
    
    private boolean generateAgenda = false;
    
    private boolean sendReminders = true;
    
    // Constructors
    public MeetingRequest() {}
    
    public MeetingRequest(String title, String description, LocalDate date, LocalTime startTime) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    
    public boolean isSuggestOptimalTime() { return suggestOptimalTime; }
    public void setSuggestOptimalTime(boolean suggestOptimalTime) { this.suggestOptimalTime = suggestOptimalTime; }
    
    public boolean isGenerateAgenda() { return generateAgenda; }
    public void setGenerateAgenda(boolean generateAgenda) { this.generateAgenda = generateAgenda; }
    
    public boolean isSendReminders() { return sendReminders; }
    public void setSendReminders(boolean sendReminders) { this.sendReminders = sendReminders; }
    
    @Override
    public String toString() {
        return "MeetingRequest{" +
                "title='" + title + '\'' +
                ", date=" + date +
                ", startTime=" + startTime +
                ", durationMinutes=" + durationMinutes +
                ", participants=" + String.join(", ", participants) +
                '}';
    }
}
