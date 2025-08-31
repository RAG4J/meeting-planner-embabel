package org.rag4j.meetingplanner.agent.model.person;

import java.util.List;

/**
 * Represents the slots of time for a specific day a participant is available for a meeting.
 * @param availableSlots list of available time slots
 * @param participant the participant whose availability is represented
 */
public record AvailabilityOfPerson(List<String> availableSlots, Person participant) {
}
