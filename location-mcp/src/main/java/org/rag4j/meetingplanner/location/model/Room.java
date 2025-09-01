package org.rag4j.meetingplanner.location.model;

import org.rag4j.meetingplanner.common.model.Agenda;

public record Room(String locationId, String roomId, int capacity, Agenda agenda) {
}
