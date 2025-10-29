package org.rag4j.meetingplanner.location.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingInfo(
        String locationId,
        String locationName,
        String roomId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String title
) {
}