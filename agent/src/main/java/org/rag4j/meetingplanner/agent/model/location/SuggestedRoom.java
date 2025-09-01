package org.rag4j.meetingplanner.agent.model.location;

import java.time.LocalTime;

public record SuggestedRoom(Location location, Room room, LocalTime startTime, int durationInMinutes) {
}
