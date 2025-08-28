package org.rag4j.meetingplanner.agent.service;

import org.rag4j.meetingplanner.agent.model.Meeting;
import org.rag4j.meetingplanner.agent.model.MeetingRequest;
import org.rag4j.meetingplanner.agent.model.MeetingSuggestion;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Embabel agent operations
 */
public interface EmbabelAgentService {
    
    /**
     * Creates a new meeting with AI assistance
     * @param request the meeting creation request
     * @return the created meeting
     */
    Meeting createMeeting(MeetingRequest request);
    
    /**
     * Gets all meetings
     * @return list of meetings
     */
    List<Meeting> getAllMeetings();
    
    /**
     * Gets a meeting by ID
     * @param id the meeting ID
     * @return the meeting if found
     */
    Optional<Meeting> getMeetingById(String id);
    
    /**
     * Suggests optimal meeting times based on participants' availability
     * @param request the meeting request
     * @return list of suggested time slots
     */
    List<MeetingSuggestion> suggestOptimalTimes(MeetingRequest request);
    
    /**
     * Generates a meeting agenda based on title and description
     * @param title the meeting title
     * @param description the meeting description
     * @return generated agenda
     */
    String generateAgenda(String title, String description);
    
    /**
     * Updates a meeting
     * @param id the meeting ID
     * @param request the update request
     * @return the updated meeting
     */
    Optional<Meeting> updateMeeting(String id, MeetingRequest request);
    
    /**
     * Deletes a meeting
     * @param id the meeting ID
     * @return true if deleted, false if not found
     */
    boolean deleteMeeting(String id);
}
