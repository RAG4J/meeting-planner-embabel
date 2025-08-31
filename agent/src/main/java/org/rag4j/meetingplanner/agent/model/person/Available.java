package org.rag4j.meetingplanner.agent.model.person;

/**
 * Represents the availability status of a person
 */
public record Available(Person person, boolean available) {
}
