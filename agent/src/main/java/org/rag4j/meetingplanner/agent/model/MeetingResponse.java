package org.rag4j.meetingplanner.agent.model;

import java.util.Optional;

/**
 * Response for a meeting request. Includes the meeting details if scheduled, or a message if not.
 * @param meeting the scheduled meeting, if any
 * @param message a message indicating success or failure
 */
public record MeetingResponse(Optional<Meeting> meeting, String message) {
    public Optional<Meeting> getMeeting() {
        return meeting;
    }

    public String getMessage() {
        return message;
    }
}
