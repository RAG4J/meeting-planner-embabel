package org.rag4j.meetingplanner.location.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookRoomRequest(String locationId, String roomId, LocalDate date, LocalTime startTime, int durationInMinutes, String reference, String description) {
}
