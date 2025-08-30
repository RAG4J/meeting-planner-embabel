package org.rag4j.meetingplanner.agent.model;

import java.util.Optional;

public record MeetingResponse(Optional<Meeting> meeting, String message) {
    public Optional<Meeting> getMeeting() {
        return meeting;
    }

    public String getMessage() {
        return message;
    }
}
