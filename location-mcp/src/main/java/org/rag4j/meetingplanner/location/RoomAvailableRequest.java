package org.rag4j.meetingplanner.location;

import java.time.LocalDate;
import java.time.LocalTime;

public record RoomAvailableRequest(String locationId, int requestedNumberOfPeople, LocalDate date, LocalTime startTime, int durationInMinutes) {
}
