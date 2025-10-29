package org.rag4j.meetingplanner.location.model;

public record LocationBookingStats(
        String locationId,
        String locationName,
        String locationDescription,
        int totalBookings,
        int roomCount
) {
}