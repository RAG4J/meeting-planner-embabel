package org.rag4j.meetingplanner.agent.model;

import java.util.List;

public record ParticipantAvailability(List<String> availableSlots, Person participant) {
}
