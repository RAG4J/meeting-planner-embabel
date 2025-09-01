package org.rag4j.meetingplanner.location.model;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;

public record RoomAvailableResponse(@NotNull String locationId, @NotNull boolean available, String roomId, LocalTime startTime, int durationInMinutes) {
}
