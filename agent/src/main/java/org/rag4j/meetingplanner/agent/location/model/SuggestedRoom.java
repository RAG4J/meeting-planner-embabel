package org.rag4j.meetingplanner.agent.location.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record SuggestedRoom(Location location, Room room, LocalDate date, LocalTime startTime, int durationInMinutes) {
}
