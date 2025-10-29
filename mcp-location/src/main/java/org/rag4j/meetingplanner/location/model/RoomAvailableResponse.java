package org.rag4j.meetingplanner.location.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RoomAvailableResponse(@NotNull String locationId, @NotNull boolean available, String roomId, LocalDate date, LocalTime startTime, int durationInMinutes) {
}
