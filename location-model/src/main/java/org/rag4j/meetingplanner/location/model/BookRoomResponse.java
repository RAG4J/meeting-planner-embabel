package org.rag4j.meetingplanner.location.model;

public record BookRoomResponse(String locationId, String roomId, boolean success, String message) {
}
