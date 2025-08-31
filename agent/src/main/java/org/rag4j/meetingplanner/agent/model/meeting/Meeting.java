package org.rag4j.meetingplanner.agent.model.meeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a meeting
 */
public class Meeting {
    
    private String id;
    
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private LocalDateTime startTime;
    
    private Integer durationMinutes;
    
    
    private String location;
    
    private List<String> participants;
    
    private String agenda;
    
    private MeetingStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public Meeting() {}
    
    public Meeting(String title, String description, LocalDateTime startTime, Integer durationMinutes) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.status = MeetingStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    
    public String getAgenda() { return agenda; }
    public void setAgenda(String agenda) { this.agenda = agenda; }
    
    public MeetingStatus getStatus() { return status; }
    public void setStatus(MeetingStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getEndTime() {
        if (startTime != null && durationMinutes != null) {
            return startTime.plusMinutes(durationMinutes);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Meeting{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", durationMinutes=" + durationMinutes +
                ", status=" + status +
                '}';
    }
}
