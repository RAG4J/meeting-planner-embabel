package org.rag4j.meetingplanner.agent.location.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record RoomRequest(String locationDescription, int numberOfParticipants, LocalDate date, LocalTime startTime, int numberOfMinutes) {
}
