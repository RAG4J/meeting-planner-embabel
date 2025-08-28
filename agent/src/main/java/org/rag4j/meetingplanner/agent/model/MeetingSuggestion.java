package org.rag4j.meetingplanner.agent.model;

import java.time.LocalDateTime;

/**
 * Represents a suggested meeting time slot
 */
public class MeetingSuggestion {
    
    private LocalDateTime suggestedStartTime;
    
    private LocalDateTime suggestedEndTime;
    
    private int availabilityScore; // 0-100, higher is better
    
    private String reasoning;
    
    // Constructors
    public MeetingSuggestion() {}
    
    public MeetingSuggestion(LocalDateTime suggestedStartTime, LocalDateTime suggestedEndTime, 
                           int availabilityScore, String reasoning) {
        this.suggestedStartTime = suggestedStartTime;
        this.suggestedEndTime = suggestedEndTime;
        this.availabilityScore = availabilityScore;
        this.reasoning = reasoning;
    }
    
    // Getters and Setters
    public LocalDateTime getSuggestedStartTime() { return suggestedStartTime; }
    public void setSuggestedStartTime(LocalDateTime suggestedStartTime) { this.suggestedStartTime = suggestedStartTime; }
    
    public LocalDateTime getSuggestedEndTime() { return suggestedEndTime; }
    public void setSuggestedEndTime(LocalDateTime suggestedEndTime) { this.suggestedEndTime = suggestedEndTime; }
    
    public int getAvailabilityScore() { return availabilityScore; }
    public void setAvailabilityScore(int availabilityScore) { this.availabilityScore = availabilityScore; }
    
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    
    @Override
    public String toString() {
        return "MeetingSuggestion{" +
                "suggestedStartTime=" + suggestedStartTime +
                ", suggestedEndTime=" + suggestedEndTime +
                ", availabilityScore=" + availabilityScore +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }
}
