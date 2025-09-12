package org.rag4j.meetingplanner.location.model;

import java.util.List;

public record BookingStats(List<LocationBookingStats> locationBookingStats) {

    public int totalRooms() {
        return locationBookingStats.stream().mapToInt(LocationBookingStats::roomCount).sum();
    }
}
